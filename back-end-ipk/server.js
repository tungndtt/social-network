// json web token
const jwt = require('jsonwebtoken');

// node mail
const nodemailer = require("nodemailer");

// database api
const db = require('./database/database');

// crypton api
const crypto = require('./cryptoForServer');

// body parse
const bodyParser = require('body-parser');

// enviroment variables and events
const info = require('./key').info;
const events = require('./events').events;

// storage
const storage = require('./storage');

const util = require('./util');

// heap structure
const heap = require('./heap/heapUtil');
const score = require('./heap/score').scores;


heap.init(storage.readScoresFromFile());
util.saveScoresBeforeExit(heap);


// initialze server
const express = require('express');
const { getToken } = require('./util');
const { Console } = require('console');
const { userInfo } = require('os');
const { json } = require('body-parser');
const { scores } = require('./heap/score');
const app = express();
const PORT = 3000;
const server = require('https').createServer({
    key: storage.getCertificate('/key.pem'),
    cert: storage.getCertificate('/cert.pem'),
    rejectUnauthorized: true,
    secure : true
}, app);

// Server socket for communication with clients
const io = require('socket.io')(server, {
    allowRequest: function(req, fn){
        console.log("Called ", req.headers);
        //console.log("Req info ", req.url);
        const headers = {
            "Access-Control-Allow-Headers": "Content-Type, Authorization",
            "Access-Control-Allow-Origin": req.headers.origin, //or the specific origin you want to give access to,
            "Access-Control-Allow-Credentials": true
        };
    },
    cors: {
        origin: '*',
    },
    allowUpgrades:true,
    serveClient: true,
    transports : ['websocket'],
    rejectUnauthorized: true
});

io.listen(server);

app.use(express.static('public'));



server.listen(PORT , () => {
    console.log("listening to port " + PORT);
});

// logged in clients online and saves chatroom, where users are currently in
var clients = {};

// clients, whose followings are in process
var currentInFollowProcess  = {};

// temporary save users, who are in process creating chatroom with other user
var creatingChatRoom = {};  

// create reusable transporter object
let transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: info.EMAIL,
        pass: info.EMAIL_PASSWORD,
    },
});

// server listens to client events
io.on(events.CONNECT, socket => {
    console.log("Some user is connecting...");
    let token = util.getToken(socket.handshake.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, id) => {
        if(err) console.log(err);
        else{
            socket.join(id.id);
            currentInFollowProcess[id.id] = null;
            console.log("join server");
        }
    });

    //#region handle JOIN_POST event from user
    // user joins a post
    socket.on(events.JOIN_POST, post_id => {
        socket.join(post_id);
    });
    //#endregion

    //#region handle LEAVE_POST event from user
    // user leaves the current post
    socket.on(events.LEAVE_POST, post_id => {
        socket.leave(post_id);
    });
    //#endregion

    //#region handle JOIN_CHAT_ROOM event
    // user enters a chatroom
    socket.on(events.JOIN_CHAT_ROOM, (user_id, roomId) => {
        if(roomId){
            clients[user_id] = roomId;
            console.log('JOIN_CHAT_ROOM event: ', roomId);
        }
        
    });
    //#endregion

    //#region handle LEAVE_CHAT_ROOM event from user
    // user leaves a chat room
    socket.on(events.LEAVE_CHAT_ROOM, user_id => {
        let roomId = clients[user_id]
        if(roomId){
            clients[user_id] = null;
            console.log('LEAVE_CHAT_ROOM event: ', roomId);
            db.updateLastSeen(roomId, user_id, new Date());
        }
    });
    //#endregion

    //#region handle NEW_MESSAGE event from user
    // user sends a new message
    socket.on(events.NEW_MESSAGE, (roomId, sender,receiver, msg) => {
        let new_msg = {
            roomId: roomId,
            author_id: sender,
            message: msg,
            image_path: "",
        };
        if(roomId.length > 0){
            if(clients[sender] !== undefined)
                io.to(sender).emit(events.NEW_MESSAGE,roomId, sender, "", msg);
            else console.log("NEW_MESSAGE event: currently offline");

            if(clients[receiver] !== undefined)
                io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, "", msg);
            else console.log("NEW_MESSAGE event: currently offline");

            db.addNewMessage(new_msg)
            .then(r => {
                console.log("NEW_MESSAGE event: added new msg");
            })
            .catch(err => {
                console.log(err);
            });
        }
        else {
            if(creatingChatRoom[receiver] === sender){
                let count = 0;
                const interval = setInterval(() => {
                    db.getChatRoomWith(sender, receiver, new_msg)
                    .then(room => {
                        if(room){
                            roomId = room._id.toString();
                            if(clients[sender] !== undefined)
                                io.to(sender).emit(events.NEW_MESSAGE, roomId, sender, "", msg);
                            else console.log("NEW_MESSAGE event: currently offline");
    
                            if(clients[receiver] !== undefined)
                                io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, "", msg);
                            else console.log("NEW_MESSAGE event: currently offline");
                            clearInterval(interval);
                        }
                        if(count > 4) clearInterval(interval);
                        count++;
                    })
                    .catch(err => {
                        console.log(err);
                        clearInterval(interval);
                    })
                }, 300);
            } else {
                if(creatingChatRoom[sender]){
                    let count = 0;
                    const interval = setInterval(() => {
                        if(!creatingChatRoom[sender]){
                            creatingChatRoom[sender] = receiver;
                            db.createNewChatRoom(sender, receiver, new_msg)
                            .then(room => {
                                roomId = room._id.toString();
                                if(clients[sender] !== undefined)
                                    io.to(sender).emit(events.NEW_MESSAGE, roomId, sender, "", msg);
                                else console.log("NEW_MESSAGE event: currently offline");
                
                                if(clients[receiver] !== undefined)
                                    io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, "", msg);
                                else console.log("NEW_MESSAGE event: currently offline");
                                delete creatingChatRoom[sender];
                            })
                            .catch(err => {
                                delete creatingChatRoom[sender];
                                console.log(err);
                            });
                            clearInterval(interval);
                        }
                        if(count > 4) clearInterval(interval);
                        count++;
                    }, 300);
                }
                else {
                    creatingChatRoom[sender] = receiver;
                    db.createNewChatRoom(sender, receiver, new_msg)
                    .then(room => {
                        roomId = room._id.toString();
                        if(clients[sender] !== undefined)
                            io.to(sender).emit(events.NEW_MESSAGE, roomId, sender, "", msg);
                        else console.log("NEW_MESSAGE event: currently offline");
        
                        if(clients[receiver] !== undefined)
                            io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, "", msg);
                        else console.log("NEW_MESSAGE event: currently offline");
                        delete creatingChatRoom[sender];
                    })
                    .catch(err => {
                        delete creatingChatRoom[sender];
                        console.log(err);
                    })
                }
            }
        }
    });
    //#endregion

    //#region handle SEND_COMMENT event from user
    // user sends a new comment
    socket.on(events.SEND_COMMENT, (post_id, author_id, comment)=>{
        db.addNewComment(post_id,author_id,comment, "")
            .then(cmt => {

                // broadcast new comment to all users in post
                io.to(post_id).emit(
                    events.NEW_COMMENT, 
                    author_id, 
                    comment, 
                    post_id, 
                    "",
                );
                db.getPost(post_id)
                .then(post => {

                    // notify new comment for all post followers
                    post.followers.forEach(follower => {
                        if(follower !== author_id){
                            // notify user directly when user is currently online
                            if(clients[follower] !== undefined){
                                io.to(follower).emit(
                                    events.COMMENT_NOTIFICATION, 
                                    author_id, 
                                    post_id, 
                                    post.author_id,
                                    post.status,
                                    storage.urlParser("",post.image_path),
                                );
                            }

                            // push notification in database, so that user will receive when notifications are loaded
                            // add to notification
                            db.pushNotification(follower, post_id, cmt._id.toString(), author_id, "new_comment")
                                .then(r => console.log('SEND_COMMENT event: pushing noti'))
                                .catch(err => console.log(err));
                        }
                    });

                    // update score system of comment author to other users
                    heap.updateScore(author_id, post.author_id, score.COMMENT);
                    let likes = post.likes,
                        comments = [];
                    post.comments.forEach(cmt => {
                        if(cmt.author_id === author_id) return;
                        comments.push(cmt.author_id);
                    });
                    if(likes.includes(author_id)) return;
                    util.updateScore(heap, score.HAS_COMMON_COMMENT_LIKE, author_id, likes, comments);

                    // show heap score of author_id
                    heap.showHeapOfUser(author_id);
                })
                .catch(err => {
                    console.log(err);
                });
                db.addCommentToPost(cmt._id.toString(), post_id, author_id)
                .then(result => {
                    console.log("SEND_COMMENT event: added new comment to post");
                })
                .catch(err => {
                    console.log(err);
                });
            })
            .catch(err => {
                console.log(err);
            });
    });
    //#endregion

    //#region handle LIKE_POST event from user
    // user likes a post
    socket.on(events.LIKE_POST, (user_id, post_id) => {
        db.addUserLikeToPost(user_id, post_id, post => {
            let post_author_id = post.author_id;
            if(clients[post_author_id] !== undefined){
                io.emit(post_author_id).emit(
                    events.LIKE_NOTIFICATION, 
                    user_id, 
                    post_id,
                    post.author_id,
                    post.status,
                    storage.urlParser("", post.image_path),
                );
            }
            if(post_author_id !== user_id){
                db.pushNotification(post_author_id, post_id, '', user_id, "new_reaction")
                    .then(result => console.log("LIKE_POST event: push like-noti successfully"))
                    .catch(err => console.log(err));
            }
            heap.updateScore(user_id, post.author_id, score.LIKE);
            let likes = post.likes,
                comments = [];
            post.comments.forEach(cmt => {
                if(cmt.author_id === user_id) return;
                comments.push(cmt.author_id);
            });
            if(likes.includes(user_id)) return;
            util.updateScore(heap, score.HAS_COMMON_COMMENT_LIKE, user_id, likes, comments);

            // show heap score of user_id
            heap.showHeapOfUser(user_id);
        });
    });
    //#endregion

    //#region handle UNLIKE_POST event from user
    // user unlikes a post
    socket.on(events.UNLIKE_POST, (user_id, post_id) => {
        db.removeUserLikeFromPost(user_id, post_id, post => {
            heap.updateScore(user_id, post.author_id, -score.LIKE);
            let likes = post.likes,
                comments = [];
            post.comments.forEach(cmt => {
                comments.push(cmt.author_id);
            });
            util.updateScore(heap, -score.HAS_COMMON_COMMENT_LIKE, user_id, likes, comments);

            // show heap score of user_id
            heap.showHeapOfUser(user_id);
        });
    });
    //#endregion

    //#region handle FOLLOW_POST event from user
    // user follows a post
    socket.on(events.FOLLOW_POST, (user_id, post_id) => {
        db.addUserToPost(post_id, user_id, post => {
            heap.updateScore(user_id, post.author_id, score.FOLLOW_POST);
            util.updateScore(heap, score.HAS_COMMON_FOLLOWING_POST, user_id, post.followers);

            // show heap score of user_id
            heap.showHeapOfUser(user_id);
        });
    });
    //#endregion

    //#region handle UNFOLLOW_POST event from user
    // user unfollow a post
    socket.on(events.UNFOLLOW_POST, (user_id, post_id) => {
        db.removeUserFromPost(post_id, user_id, post => {
            heap.updateScore(user_id, post.author_id, -score.FOLLOW_POST);
            util.updateScore(heap, -score.HAS_COMMON_FOLLOWING_POST, user_id, post.followers);

            // show heap score of user_id
            heap.showHeapOfUser(user_id);
        });
    });
    //#endregion

    //#region handle FOLLOW_USER event from user
    socket.on(events.FOLLOW_USER, (user_id, follower_id) => {
        let isFriend = false;
        const becomeFriend = () => {
            if(clients[user_id] !== undefined){
                console.log('user id: ', user_id);
                io.to(user_id).emit(events.FRIEND, follower_id);
            }
            if(clients[follower_id] !== undefined){
                console.log('follower id: ', follower_id);
                io.to(follower_id).emit(events.FRIEND, user_id);
            }

            db.pushNotification(user_id, '', '', follower_id, 'new_friend')
            .then(success => console.log('FOLLOW_USER event: push friend-noti for ', user_id))
            .catch(err => console.log(err));

            db.pushNotification(follower_id, '', '', user_id, 'new_friend')
            .then(success => console.log('FOLLOW_USER event: push friend-noti for ', follower_id))
            .catch(err => console.log(err));

            heap.removeFrom(user_id, follower_id);
            heap.removeFrom(follower_id, user_id);

            // show heap score of user_id
            heap.showHeapOfUser(user_id);
            // show heap score of follower_id
            heap.showHeapOfUser(follower_id);
        };
        const addFollower = () => {
            currentInFollowProcess[follower_id] = [user_id, 1];
            db.addNewFollower(user_id, follower_id, 
                user => {
                    console.log("FOLLOW_USER event");

                    if(user.followees.includes(follower_id) && !isFriend){
                        isFriend = true;
                        becomeFriend();
                    } 
                    else heap.removeFrom(follower_id, user_id);

                    util.updateScore(heap, score.HAS_COMMON_FOLLOWING_PERSON, follower_id, user.followers);
                    // show heap score of user
                    heap.showHeapOfUser(follower_id);
                }, 
                () => {currentInFollowProcess[follower_id] = null},
            );
        }; 
        if(currentInFollowProcess[follower_id] === null) addFollower();
        else{
            const interval = setInterval(() => {
                if(currentInFollowProcess[follower_id] === null) {
                    addFollower();
                    clearInterval(interval);
                }
            }, 300);
        }
        let userFollowProcess = currentInFollowProcess[user_id];
        if(userFollowProcess && userFollowProcess[0] === follower_id && userFollowProcess[1] === 1) {
            isFriend = true;
            becomeFriend();
        }
    });
    //#endregion

    //#region handle UNFOLLOW_USER event from user
    socket.on(events.UNFOLLOW_USER, (user_id, follower_id) => {
        const myInterval = setInterval(() => {
            if(currentInFollowProcess[follower_id] === null){
                currentInFollowProcess[follower_id] = [user_id, 0];
                let users = [undefined, undefined];
                db.removeFollower(user_id, follower_id, 
                    user => {
                        console.log("UNFOLLOW_USER event: remove follower done");
                        
                        util.updateScore(heap, -score.HAS_COMMON_FOLLOWING_PERSON, follower_id, user.followers);

                        // show heap score of user_id
                        heap.showHeapOfUser(follower_id);
                    },
                    () => {currentInFollowProcess[follower_id] = null}, 
                    users,
                );

                let interval = setInterval(() => {
                    if(users[0] !== undefined && users[1] !== undefined){
                        if(users[0] === null || users[1] === null) console.log('UNFOLLOW_USER event: user is not found');
                        else{
                            heap.addTo(users[0], users[1]);

                            // show heap score of user_id
                            heap.showHeapOfUser(users[0]._id.toString());
                        }
                        clearInterval(interval);
                    }
                }, 300);
                clearInterval(myInterval);
            }
        }, 300);
    });
    //#endregion

    //#region user ignores the recommended user
    socket.on(events.IGNORE, (user_id, ignored_id) => {
        console.log("IGNORE event: reset the caring-score");
        heap.setScore(user_id, ignored_id, -1);

        // show heap score of user_id
        heap.showHeapOfUser(user_id);
    });
    //#endregion

    //#region handle DISCONNECT event
    // client disconnects
    socket.on(events.DISCONNECT, () =>{
        console.log("some user disconneted");
        let token = util.getToken(socket.handshake.headers.cookie);
        console.log(token);
        jwt.verify(token, info.SECRET_KEY, (err, val) => {
            if(err) console.log(err);
            else{
                db.updateUserLastLogout(val.id, new Date())
                .then(success => console.log('DISCONNECT event: updated last time logout'))
                .catch(err => console.log(err));

                let roomId = clients[val.id];
                if(roomId){
                    db.updateLastSeen(roomId, val.id, new Date());
                    console.log('DISCONNECT event: update last time ' + val.id + ' in room ' + roomId);
                }
                delete clients[val.id];
                delete currentInFollowProcess[val.id];
            };
        });
    });
    //#endregion
});

//#region get comments from a post
// get all comments from post with provided post_id
app.get('/get_comments/:post_id', (req,res) => {
    let post_id = req.params.post_id;
    console.log(post_id);
    db.getPost(post_id)
    .then(post => {
        let cmts = [];
        post.comments.forEach(cmt => cmts.push(cmt.comment_id));
        db.getAllComments(cmts)
        .then(comments => {

            // adapt the result form and send back to user
            let result = [];
            comments.forEach(c => {
                let tmp = "";
                if(c.image_path) tmp = storage.urlParser("",c.image_path);
                result.push({
                    id: c._id.toString(),
                    author_id: c.author_id,
                    image_path: tmp,
                    post_id: post_id,
                    comment: c.comment,
                    date: c.updatedAt.getTime(),
                });
            });
            res.status(200).end(JSON.stringify({
                author_id: post.author_id,
                date: post.createdAt.getTime(),
                status: post.status, 
                comments: result, 
                imageUrl: storage.urlParser("", post.image_path),
                likes: post.likes,
            }));
        })
        .catch(err => {
            res.status(400).end();
            console.log(err);
        });
    })
    .catch(err => {
        res.status(400).end();
        console.log(err);
    });
});
//#endregion

//#region upload file along with comment handle
// upload a file (image) along with comment
app.post('/upload_file_from_comment', storage.handleFile, (req,res) => {
    res.status(200).end();
    let post_id = req.body.post_id,
        sender_id = req.body.sender_id,
        comment = req.body.comment,
        image_path = req.file.filename;
    db.addNewComment(post_id,sender_id,comment,image_path)
    .then(cmt => {
        db.getPost(post_id)
        .then(post => {

            // broadcast new comment to all users in post 
            io.to(post_id).emit(
                events.NEW_COMMENT, 
                sender_id, 
                comment, 
                post_id, 
                storage.urlParser("",image_path),
            );

            // push notification
            post.followers.forEach(follower => {
                if(follower !== sender_id){
                    // send notification directly when user is currently online
                    if(clients[follower] !== undefined){
                        io.to(follower).emit(
                            events.COMMENT_NOTIFICATION, 
                            sender_id,  
                            post_id, 
                            post.author_id,
                            post.status,
                            storage.urlParser("",post.image_path),
                        );
                    }

                    // push notification into database. User will receive when notifications are loaded
                    // add to notification
                    db.pushNotification(follower, post_id, post.author_id, sender_id, "new_comment")
                        .then(r => console.log('upload_file_from_comment: pushing noti'))
                        .catch(err => console.log(err));
                }
            });

            // update the score system of sender to other users
            heap.updateScore(sender_id, post.author_id, score.COMMENT);

            let likes = post.likes,
                comments = [];
            post.comments.forEach(cmt => {
                if(cmt.author_id === sender_id) return;
                comments.push(cmt.author_id);
            });
            if(likes.includes(sender_id)) return;
            util.updateScore(heap, score.HAS_COMMON_COMMENT_LIKE, sender_id, likes, comments); 

            // show heap score of comment sender
            heap.showHeapOfUser(sender_id);
        })
        .catch(err => {
            console.log(err);
        });

        // add the new comment to post
        db.addCommentToPost(cmt._id.toString(), post_id, sender_id)
        .then(result => {
            console.log("upload_file_from_comment: added new comment to post");
        })
        .catch(err => {
            console.log(err);
        });
    })
    .catch(err => {
        console.log(err);
    });
});
//#endregion

//#region verify token to create a new user
// url for verify and create new user
app.get("/verify/:token", (req,res) => {
    let token = req.params.token;

    // verify token
    jwt.verify(token, info.SECRET_KEY, (err, ans) => {
        if(err) res.send(err);
        else {
            // check case user click on the url twice (it should not create user for second click)
            db.checkExistingUser(ans.email)
            .then(user => {
                if(user == null){

                    // birthday must in in form YYYY-MM-DD
                    ans.birthday = new Date(ans.birthday);
                   
                    db.creatNewUser(ans)
                    .then(r => {
                        res.send("New user created !");

                        // initialize the new user score
                        db.getAllUserInterests()
                        .then(users => heap.addNewUser(users, r))
                        .catch(err => console.log(err));
                    })
                    .catch(err => {
                        res.send(err);
                    });
                } else res.send("User already created");
            })
            .catch(err => {
                res.send(err);
            });
        }
    });
});
//#endregion

//#region 
// check url whether server is running
app.get('/', (req,res) => {
    res.send("hello from server...")
});
//#endregion

//#region register handle
// client sends registration request
app.post('/register', bodyParser.json(), (req,res) => {
    console.log("received")
    console.log(req.body);
    let data = req.body;
    let name = data.username,
        mail = data.email,
        birthday = data.birthday, // birthday must be in form YYYY-MM-DD
        sex = data.sex,
        interests = data.interests;
        pass = crypto.encrypt(data.password, crypto.publicKey);

    birthday = birthday.split('/');
    if(birthday[1].length == 1) birthday[1] = '0'+birthday[1];
    if(birthday[0].length == 1) birthday[0] = '0'+birthday[0];
    birthday = new Date(new Date(birthday[2]+'-'+birthday[1]+'-'+birthday[0]));

    console.log(interests);
    console.log(name,mail,pass);
    
    // check whether registered email exists
    db.checkExistingUser(mail)
        .then(us => {
            if(us != null){
                console.log("register: A user has already signed with emai: " + mail);
                res.status(403).end(); // forbidden
            }
            else {
                // email is valid for registration
                console.log("register: User is available ...");
                res.status(200).end();

                // generates token for user given info
                let token = jwt.sign(
                    {
                        username: name, 
                        password: pass,
                        email: mail, 
                        birthday: birthday, 
                        sex: sex,
                        interests: interests,
                    }, 
                    info.SECRET_KEY, 
                    {expiresIn: 4*60}
                );

                // sends verification email to registered email
                transporter.sendMail({
                    from: `Server mail <${info.EMAIL}>`, // sender address
                    to: mail, // list of receivers
                    subject: "Verification", // Subject line
                    text: "Confirm your new created account", // plain text body
                    html: `<a href="https://localhost:${PORT}/verify/${token}">Confirmation</a>`, // html body
                }, (err, info) => {
                    if (err) {
                        console.log(err);
                    } else {
                        console.log('Verification email sent ...');
                    }
                });
            }
        })
        .catch(err => {
            console.log(err);
            res.status(400).end();
        });
});
//#endregion

//#region login
// client sends login request
app.post('/login', bodyParser.json(), (req,res) => {
    let data = req.body;
    let name = data.email,
        pass = data.password;

        db.checkUserForLogin(name)
        .then(user => {
            if(user != null && clients[user._id.toString()] === undefined){
                let id = user._id.toString();
                if(pass === crypto.decrypt(user.password, crypto.privateKey)){
                    clients[id] = null;
                    let token = jwt.sign({id: id}, info.SECRET_KEY);
                    res.status(200).end(JSON.stringify({token: token}));
                    /*
                    db.findAllPosts(id)
                    .then(posts => {
                        let allPosts = [];
                        posts.forEach(post => {
                            let e = {
                                id: post._id.toString(),
                                status: post.status,
                            };
                            if(post.image_path){
                                e.image = storage.getFile(post.image_path);
                            }
                            allPosts.push(e);
                        });
                        res.status(200).end(JSON.stringify({username: user.name, id: id, posts: allPosts}));
                    })
                    .catch(err => {
                        console.log(err);
                        res.status(400).end();
                    });
                    */
                } else res.status(400).end(JSON.stringify({message: "wrong password!"})); 
            } else res.status(400).end(JSON.stringify({message: "No user with given email!"})); 
        })
        .catch(err => {
            console.log(err);
            res.status(400).end(JSON.stringify({message: "No user with given email!"})); 
        });

});
//#endregion

//#region handle upload post
// Post files
app.post("/upload_post", storage.handleFile, function(req, res) {
    let id = req.body.id,
        status = req.body.status;
        console.log(req.file.filename);
    db.addNewPost(id, status, req.file.filename)
        .then(val => {
            console.log(storage.urlParser("", val.image_path));
            res.status(200).end(JSON.stringify({id: val._id.toString(), imageUrl: storage.urlParser("", val.image_path)}));
        })
        .catch(err => {
            console.log(err);
            res.status(400).end();
        });
});
//#endregion

//#region handle get user profile based on id
// sends posts back to user
app.get("/get_user_profile/:author_id", function(req, res) {
    db.getUser(req.params.author_id)
    .then(user => {
        db.findAllPosts(req.params.author_id)
        .then(posts => {
            let result = {
                username: user.name,
                location: user.location,
                sex: user.sex,
                birthday: user.birthday,
                interests: user.interests,
                description: user.description,
                profile_pic: storage.urlParser("",user.profile_path),
                followers: user.followers,
                followees: user.followees,
            },
            new_posts = [];
            posts.forEach(e => {
                let tmp = {
                    author_id: e.author_id,
                    id: e._id.toString(),
                    status: e.status,
                    imageUrl: storage.urlParser("",e.image_path), 
                    date: e.updatedAt.getTime(),
                }
                new_posts.push(tmp);
            });
            result.posts = new_posts;
            console.log("get_user_profile:", result);
            res.status(200).end(JSON.stringify(result));
        })
        .catch(err => {
            console.log(err);
            res.status(400).end();
        });
        let token = util.getToken(req.headers.cookie);
        jwt.verify(token, info.SECRET_KEY, (err, val) => {
            if(err) console.log(err);
            else{
                heap.updateScore(val.id, req.params.author_id, score.VISIT_PROFILE);
                heap.showHeapOfUser(val.id);
            }
        });
    })
    .catch(err => {
        console.log(err);
        res.status(400).end();
    })
});
//#endregion

//#region forget password handle
// user posts request to reset password
app.post('/password_forget', bodyParser.json(),(req,res) =>{
    let email = req.body.email,
        new_password = crypto.encrypt(req.body.password, crypto.publicKey);
    db.checkExistingUser(email)
    .then(user => {
        if(user){
            // generates token for user given info
            let token = jwt.sign({email: email, password: new_password}, info.SECRET_KEY, {expiresIn: 4*60});
            res.status(201).end();
            // sends verification email to registered email
            transporter.sendMail({
                from: `Server mail <${info.EMAIL}>`, // sender address
                to: email, // list of receivers
                subject: "Reset password", // Subject line
                text: "Confirm your password-reset request", // plain text body
                html: `<a href="https://localhost:${PORT}/reset/${token}">Confirm to reset password</a>`, // html body
            }, (err, info) => {
                if (err) {
                    console.log(err);
                } else {
                    console.log('Sending to email ...');
                }
            });
        } else {
            console.log("No user with given email available!");
            res.status(400).end();
        }
    })
    .catch(err => {
        console.log(err);
    });
});
//#endregion

//#region confirm via email the reset password action
// reset password
app.get('/reset/:token', (req,res) => {
    jwt.verify(req.params.token, info.SECRET_KEY, (err,ans) => {
        if(err) res.send(err);
        else {
            db.changePassword(ans.email, ans.password)
            .then(user => {
                if(user)
                    res.send("Password changed!");
                else
                    res.send("No user available");
            })
            .catch(err => {
                res.send(err);
            })
        }
    });
});
//#endregion

//#region get users relevant information after login
// get the information for user after login
app.post('/get_user', (req,res) => {
    let token = util.getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if(err) {
            console.log(err);
            res.status(400).end();
        } else {
            db.getUser(val.id)
                .then(user => {
                    let result = {
                        id: val.id,
                        username: user.name,
                        email: user.email,
                        location: user.location,
                        birthday: user.birthday,
                        sex: user.sex,
                        description: user.description,
                        interests: user.interests,
                        profile_pic: storage.urlParser("", user.profile_path),
                        posts: [],
                        following_posts: user.following_posts,
                        liked_posts: user.liked_posts,
                        followers: user.followers,
                        followees: user.followees,
                        lastLogout: user.lastLogout? user.lastLogout.getTime() : 0,
                    };
                    
                    db.findAllPosts(val.id)
                        .then(list_post => {
                            let allPosts = [];
                            list_post.forEach(post => {
                                allPosts.push({
                                    id: post._id.toString(),
                                    author_id: val.id,
                                    author_name: user.name,
                                    status: post.status,
                                    imageUrl: storage.urlParser("",post.image_path),
                                    date: post.updatedAt.getTime(),
                                });
                            });
                            
                            result.posts = allPosts;
                            res.status(200).end(JSON.stringify(result));
                        })
                        .catch(err => {
                            console.log(err);
                            res.status(250).end(JSON.stringify(result));
                        });
                })
                .catch(err => {
                    console.log(err);
                    res.status(400).end();
                })
        }
    });
});
//#endregion

//#region get chat room based on provided roomId
// get chat room based on provided chatroomId
app.post("/get_chat_room_byId",bodyParser.json(), (req,res) => {
    let roomId = req.body.id;
    db.getChatRoom(roomId)
        .then(room => {
            let result = {
                roomId: room._id.toString(),
                user_1: room.user_1,
                user_2: room.user_2,
                messages: [],
                lastseen_1: room.users[0].lastseen,
                lastseen_2: room.users[1].lastseen,
            };
            room.messages.forEach(msg => {
                let e = {author_id: msg.author_id, time: msg.updatedAt.getTime()};
                if(result.message) e.message = result.message;
                if(result.image_path) e.imageUrl = storage.urlParser(result.image_path);
                result.messages.push(e);
            });
            res.status(200).end(JSON.stringify(room));
        })
        .catch(err => {
            res.status(400).end();
            console.log(err);
        });
});
//#endregion

//#region get all chatrooms for user
// get all chat rooms for user
app.post("/get_all_chat_room_ofUser", (req,res) => {
    let token = util.getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, ans) => {
        if(err){
            console.log(err);
            res.status(400).end();
        } else {
            db.getAllChatRooms(ans.id)
            .then(rooms => {
                let tmp = [];
                rooms.forEach(room => {
                    let result = {
                        roomId: room._id.toString(),
                        user_1: room.users[0].user_id,
                        user_2: room.users[1].user_id,
                        messages: [],
                        lastseen_1: room.users[0].lastseen,
                        lastseen_2: room.users[1].lastseen,
                    };
                    room.messages.forEach(msg => {
                        let e = {author_id: msg.author_id, time: msg.updatedAt.getTime()};
                        if(msg.message) e.message = msg.message;
                        if(msg.image_path) e.imageUrl = storage.urlParser("", msg.image_path);
                        result.messages.push(e);
                    });
                    tmp.push(result);
                });
                res.status(200).end("["+tmp.map(JSON.stringify)+"]");
            })
            .catch(er => {
                console.log(er);
                res.status(400).end();
            })
        }
    });
});
//#endregion

//#region upload file sent along with message handle
// upload a file (image) along with sent message 
app.post("/upload_file_from_message", storage.handleFile, (req,res) => {
    res.status(200).end();
    let url = storage.urlParser("", req.file.filename),
        roomId = req.body.room_id,
        sender = req.body.sender_id,
        receiver = req.body.receiver_id,
        msg = {
            roomId: roomId,
            author_id: sender,
            message: "",
            image_path: req.file.filename,
        };
    if(roomId.length > 0){
        if(clients[sender] !== undefined)
            io.to(sender).emit(events.NEW_MESSAGE,roomId, sender, url, "");
        else console.log("upload_file_from_message: currently offline");

        if(clients[receiver] !== undefined)
            io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, url, "");
        else console.log("upload_file_from_message: currently offline");
    
        // save the new message in chat room
        db.addNewMessage(msg)
        .then(r => {
            console.log("upload_file_from_message: added new msg");
        })
        .catch(err => {
            console.log(err);
        });
    }
    else {
        if(creatingChatRoom[receiver] === sender){
            let count = 0;
            const interval = setInterval(() => {
                db.getChatRoomWith(sender, receiver, msg)
                .then(room => {
                    if(room){
                        roomId = room._id.toString();
                        if(clients[sender] !== undefined)
                            io.to(sender).emit(events.NEW_MESSAGE, roomId, sender, url, "");
                        else console.log("upload_file_from_message: currently offline");

                        if(clients[receiver] !== undefined)
                            io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, url, "");
                        else console.log("upload_file_from_message: currently offline");
                        clearInterval(interval);
                    }
                    if(count > 4) clearInterval(interval);
                    count++;
                })
                .catch(err => {
                    console.log(err);
                    clearInterval(interval);
                })
            }, 300);
        } else {
            if(!creatingChatRoom[sender]){
                creatingChatRoom[sender] = receiver;
                db.createNewChatRoom(sender, receiver, msg)
                .then(room => {
                    roomId = room._id.toString();
                    if(clients[sender] !== undefined)
                        io.to(sender).emit(events.NEW_MESSAGE, roomId, sender, url, "");
                    else console.log("upload_file_from_message: currently offline");
    
                    if(clients[receiver] !== undefined)
                        io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, url, "");
                    else console.log("upload_file_from_message: currently offline");
                    delete creatingChatRoom[sender];
                })
                .catch(err => {
                    delete creatingChatRoom[sender];
                    console.log(err);
                })
            }
            else {
                let count = 0;
                const interval = setInterval(() => {
                    if(!creatingChatRoom[sender]){
                        creatingChatRoom[sender] = receiver;
                        db.createNewChatRoom(sender, receiver, msg)
                        .then(room => {
                            roomId = room._id.toString();
                            if(clients[sender] !== undefined)
                                io.to(sender).emit(events.NEW_MESSAGE, roomId, sender, url, "");
                            else console.log("upload_file_from_message: currently offline");
            
                            if(clients[receiver] !== undefined)
                                io.to(receiver).emit(events.NEW_MESSAGE ,roomId, sender, url, "");
                            else console.log("upload_file_from_message: currently offline");
                            delete creatingChatRoom[sender];
                        })
                        .catch(err => {
                            delete creatingChatRoom[sender];
                            console.log(err);
                        });
                        clearInterval(interval);
                    }
                    if(count > 4) clearInterval(interval);
                    count++;
                }, 300);
            }
        }
    }
});
//#endregion

//#region get all user simples
// get a list of user simple based on provided user_id list in request body
app.post('/get_users_byId',bodyParser.json(), (req,res) => {
    let users = req.body.users;
    console.log("request user:",users);
    db.getUsers(users)
    .then(listUsers => {
        let result = [];
        listUsers.forEach(user => {
            result.push({
                username: user.name,
                birthday: user.birthday,
                sex: user.sex,
                location: user.location,
                id: user._id.toString(),
                profile_pic: storage.urlParser("",user.profile_path),
                interests: user.interests, 
            });
        });
        res.status(200).end("["+result.map(JSON.stringify)+"]");
    })
    .catch(err => {
        console.log(err);
        res.status(400).end();
    });
});
//#endregion

//#region get k recommended users
// get k recommended users for user
app.get('/get_recommend_users', (req,res) => {
    let token = util.getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if(err){
            res.status(403).end();
            console.log(err);
        }
        else {
            let kmax = heap.getKMaxFrom(val.id, 3);
            console.log(kmax);
            let ans = [];
            db.getUsers(kmax)
                .then(users => {
                    users.forEach(user => {
                        if(true){
                            ans.push({
                                username: user.name,
                                birthday: user.birthday,
                                sex: user.sex,
                                description: user.description,
                                id: user._id.toString(),
                                profile_pic: storage.urlParser("",user.profile_path), 
                                interests: user.interests,
                            });
                        }
                    });
                    res.status(200).end("["+ans.map(JSON.stringify)+"]");
                })
                .catch(err => {
                    res.status(404).end();
                    console.log(err);
                });
        }
    });
});
//#endregion

//#region get notifications for given user
app.get('/get_all_notifications', (req, res) => {
    let token = util.getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if(err){
            res.status(404).end();
            console.log(err);
        }
        else{
            db.getUser(val.id)
            .then(user => {
                let users = [],
                    posts = [],
                    stack = [],
                    j = 0,
                    ans = [];
                user.notifications.forEach(noti => {
                    ans.push({
                        author_id: noti.comment_like_author_id,
                        time: noti.updatedAt.getTime(),
                        type: noti.kind, 
                    });
                    if(noti.kind != 'new_friend'){
                        posts.push(noti.post_id);
                        stack.push(j);
                    }
                    j++;
                });
                
                db.getPosts(posts)
                    .then(list_posts => {
                        let map_post = {};
                        list_posts.forEach(post => map_post[post._id.toString()] = post);
                        for(var i=0; i<stack.length; i++){
                            let post = map_post[posts[i]]
                                idx = stack[i];
                            ans[idx].post_img = storage.urlParser("", post.image_path);
                            ans[idx].post_status = post.status;
                            ans[idx].post_id = post._id.toString();
                            ans[idx].post_author_id = post.author_id;
                        }
                        console.log ("Notifcations:", ans);
                        res.status(201).end("["+ans.map(JSON.stringify)+"]");
                    })
                    .catch(err => {
                        res.status(404).end();
                        console.log(err);
                    });
            })
            .catch(err => {
                res.status(404).end();
                console.log(err);
            });
        }
    });
});
//#endregion

//#region get all posts for user based on his/her care
// request body contains: date (miliseconds) and list id of seen posts
app.post('/get_post_for_user', bodyParser.json(), (req, res) => {
    let token = util.getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if(err){
            res.status(404).end();
            console.log(err);
        }
        else {
            db.getUser(val.id)
            .then(user => {
                let date = req.body.date,
                    seenPosts = req.body.seenPosts,
                    k = 2;
                let recommended_users = heap.getKMaxFrom(val.id, k);
                user.followees.forEach(e => {
                    recommended_users.push(e);
                });
                db.getPostsForUser(recommended_users, new Date(date-1000*3600*24*30), seenPosts)
                .then(posts => {
                    let result = []
                        k = posts.length > 2 ? 2 : posts.length;   
                    for(let i=0; i<k; i++){
                        result.push({
                            status: posts[i].status,
                            id: posts[i]._id.toString(),
                            author_id: posts[i].author_id,
                            imageUrl: storage.urlParser("",posts[i].image_path),
                            date: posts[i].updatedAt.getTime(),
                        });
                    }
                    console.log(result);
                    res.status(201).end("["+result.map(JSON.stringify)+"]");
                })
                .catch(err => {
                    res.status(200).end("[]");
                    console.log(err);
                });
            })
            .catch(err => {
                res.status(401).end();
                console.log(err);
            })
        }
    });
});
//#endregion

//#region get users based filtering info
app.post('/get_filtered_users', bodyParser.json(), (req, res) => {
    let name = req.body.name,
        age = req.body.age,
        sex = req.body.sex,
        interests = req.body.interests;
    let currentTime = new Date(),
        yearInMilis = 1000*3600*24*365;
    let date = [new Date(currentTime.getTime() - age[1]*yearInMilis), new Date(currentTime.getTime() - age[0]*yearInMilis)];
    console.log(name,age,sex,date);
    db.getUsersBasedOnInfo(name, date, sex, interests)
    .then(users => {
        let ans = [];
        console.log(users);
        users.forEach(user => {
            if(true){
                ans.push({
                    username: user.name,
                    birthday: user.birthday, //.getYear() - currentTime.getYear(),
                    sex: user.sex,
                    description: user.description,
                    id: user._id.toString(),
                    profile_pic: storage.urlParser("",user.profile_path),
                    interests: user.interests, 
                });
            }
        });
        
        res.status(200).end("["+ans.map(JSON.stringify)+"]");
    })
    .catch(err => {
        res.status(400).end();
        console.log(err);
    });
});
//#endregion

//#region update basic user info and updates Posts, Comments
app.patch("/update_user_info", bodyParser.json(), (req,res) => { 
    console.log(req.body);
    let token = util.getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if(err){
            res.status(403).end();
            console.log(err);
        }
        else{
            console.log(req.body.sex);
            let new_user = {
                        name: req.body.username,
                        birthday: req.body.birthday,
                        location: req.body.location,
                        sex: req.body.sex, 
                        description: req.body.description
            }
            db.updateUserBasicInfo(val.id, new_user)
                .then(user => {
                    if(user != null){
                        console.log(user.sex);
                        let updated_user = {
                            username: user.name,
                            birthday: user.birthday,
                            sex: user.sex,
                            description: user.description,
                            location: user.location
                        }
                        //send updated user
                        console.log("update ok");
                        res.status(201).end(JSON.stringify(updated_user));
                        // update Posts
                        db.updatePostsAuthorName(user.id, user.name)
                            .then( () => {
                                console.log("updated posts")
                            })
                            .catch(err => console.log(err));
                        // update Comments
                        db.updateCommentsAuthorName(user.id, user.name)
                            .then( () => {
                                console.log("updated comments")
                            })
                            .catch(err => console.log(err));
                   
                    }else{
                        res.status(404).end();
                    }
                        

                }).catch(err => {
                    console.log(err);
                    res.status(503).end(); 
                    });
        }});   
});
//#endregion

//#region update new user's interests
app.post('/update_user_interests', bodyParser.json(), (req, res) => {
    let token = getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if(err){
            res.status(403).end();
            console.log(err);
        }
        else {
            let new_interests = req.body.interests;
            db.updateUserInterests(val.id, new_interests)
            .then(user => {
                res.status(201).end();
                let old_interests = user.interests;
                console.log(old_interests);
                db.getAllUserInterests()
                .then(others => {
                    others.forEach(other => {
                        let score = util.getScoreToUpdate(new_interests, other.interests, old_interests)*scores.HAS_COMMON_INTEREST,
                            other_id = other._id.toString();
                        heap.updateScore(val.id, other_id, score);
                        heap.updateScore(other_id, val.id, score);

                        // show heap score of other
                        heap.showHeapOfUser(other_id);
                    });
                    // show heap score of the user
                    heap.showHeapOfUser(val.id);
                })
                .catch(err => console.log(err));
            })
            .catch(err => {
                res.status(401).end();
                console.log(err);
            })
        }
    });
});
//#endregion

//#region update user password
app.post('/update_new_password', bodyParser.json(), (req, res) => {
    let token = getToken(req.headers.cookie);
    jwt.verify(token, info.SECRET_KEY, (err, val) => {
        if (err) {
            res.status(403).end();
            console.log(err);
        }
        else {
            console.log("req");
            console.log(req.body);
            let user_id = val.id,
            old_password = req.body.password;
            db.getUserBasic(user_id)
                .then(user => {
                    let currentPassword = crypto.decrypt(user[0].password, crypto.privateKey);
                    if (currentPassword === old_password) {
                        console.log(req.body.new_password);
                        let new_password = crypto.encrypt(req.body.new_password, crypto.publicKey);
                        db.changePassword(user[0].email, new_password)
                            .then(() => {
                                res.status(201).end();
                                console.log('update_new_password: updated new password');
                                
                                transporter.sendMail({
                                    from: `Server mail <${info.EMAIL}>`, // sender address
                                    to: user[0].email, // list of receivers
                                    subject: "Change of your password", // Subject line
                                    text: "Hello " + user[0].name.toString() + ",\n" +       // plain text body <br>
                                        "Your password was changed.\n" +
                                        "If that was you, you can just ignore this email.\n" +
                                        "If it wasn't you, secure your account." +
                                        "\n Greetings,\n Your security team",       // TODO: hmtl:....
                                }, (err) => {
                                    if (err)
                                        console.log(err);
                                    else console.log('change password email sent ...');
                                })
                            }).catch(err => {
                                res.status(401).end();  //Unauthorized
                                console.log(err);
                            });
                    } else {
                        res.status(403).end();      //Forbidden
                        console.log('update_new_password: Wrong current password');
                    }
                })
                .catch(err => {
                    res.status(403).end();
                    console.log(err);
                })
        };
    });
});

//#endregion

//#region update profile image
app.post('/update_profile_image', bodyParser.json(), (req, res) => {
    console.log(req.body);
    let user_id = req.body.id;
    let image_name = req.body.profile_pic;
    db.updateUserProfile(user_id, image_name)
    .then(result => {
        res.status(201).end();
        console.log('update_profile_image: updated profile');
    })
    .catch(err => {
        res.status(401).end();
        console.log(err);
    });
});
//#endregion
app.post("/upload_profile_image", storage.handleFile, function(req, res) {
    let id = req.body.id,
        status = req.body.status;
        console.log(req.file.filename);
    db.addNewPost(id, status, req.file.filename)
        .then(val => {
            console.log(storage.urlParser("", val.image_path));
            db.updateUserProfile(id, req.file.filename)
                .then(() => {
                    res.status(200).end(JSON.stringify({id: val._id.toString(), imageUrl:  storage.urlParser("", val.image_path)}));
                })
                .catch(err => {
                    console.log(err);
                    res.status(400).end();
                });   
        })
        .catch(err => {
            console.log(err);
            res.status(400).end();
        });
    
});
//#endregion

//#region function for testing purpose
//get all posts for testing ....
app.get('/posts', (req,res) => {
    db.getAllPosts()
        .then(posts => {
            let result = [];
            posts.forEach(e => {
                result.push({
                    status: e.status,
                    id: e._id.toString(),
                    author_id: e.author_id,
                    author_name: e.author_name,
                    imageUrl: storage.urlParser("",e.image_path),
                    likes: e.likes,
                });
            });
            res.status(200).end("["+(result.map(JSON.stringify)).toString()+"]");
        })
        .catch(err => {
            console.log(err);
            res.status(401).end();
        });
});



// get all users for testing ...
app.get("/get_all_users", (req,res) => {
    db.getAllUsers()
        .then(users => {
            let result = [];
            users.forEach(user => {
                result.push({
                    id: user._id.toString(),
                    username: user.name,
                    email: user.email,
                    profile_pic: storage.urlParser("",user.profile_path),
                });
            });
            res.status(200).end("["+(result.map(JSON.stringify)).toString()+"]");
        })
        .catch(err => {
            res.status(400).end();
            console.log(err);
        })
});
//#endregion