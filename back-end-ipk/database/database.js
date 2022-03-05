var mongoose = require('mongoose');
const info = require('../key').info;
const url = `mongodb+srv://${info.DATABASE_USERNAME}:${info.DATABASE_PASSWORD}@myfirstdb.fy59c.mongodb.net/${info.DATABASE_NAME}?retryWrites=true&w=majority`;
//const url = `mongodb+srv://${info.DATABASE_USERNAME}:${info.DATABASE_PASSWORD}@iptk-cluster.bk96u.mongodb.net/${info.DATABASE_NAME}?retryWrites=true&w=majority`;
const models = require('./models');

mongoose.connect(url, {useNewUrlParser: true, useUnifiedTopology: true})
        .then(res => { console.log('connected to the DB'); })
        .catch(err =>{ console.log(err); });

models.User.createIndexes({_id: 1,  email: 1})
        .then(res => {
                console.log("Created index for id User model");
        })
        .catch(err => {
                console.log(err);
        });

models.Post.createIndexes({_id: 1})
        .then(res => {
                console.log("Created index for Post model");
        })
        .catch(err => {
                console.log(err);
        });

models.Comment.createIndexes({_id: 1})
        .then(res => {
                console.log("Created index for Comment model");
        })
        .catch(err => {
                console.log(err);
        });

models.ChatRoom.createIndexes({_id: 1, 'users.user_id.0': 1, 'users.user_id.1': 1})
        .then(res => {
                console.log("Created index for ChatRoom model");
        })
        .catch(err => {
                console.log(err);
        });

// create a new user based on provided info
function creatNewUser(token) {
        return new models.User({
                name: token.username,
                email: token.email,  
                password: token.password,
                birthday: token.birthday,
                sex: token.sex,
                location: "",
                description: "",
                profile_path:"",
                interests: token.interests,
        }).save();
}

// get user based on provided id
function getUser(id) {
        return models.User.findById(id).exec();
}

// get user basic info based on provided id
function getUserBasic(id){
        return models.User.aggregate([
                {$project:
                        {
                                email: '$email',
                                name: '$name',
                                password: '$password',
                                location: '$location',
                                sex: '$sex',
                                birthday: '$birthday',
                        }
                },
                {$match: {_id: mongoose.Types.ObjectId(id)}}
        ]).exec();
}

// update user basic infos (name, password, birthday, location, age, sex, password)
function updateUserBasicInfo(id, new_user){
        return models.User.findByIdAndUpdate(
                {_id: id}, 
                {       
                        name: new_user.name,
                        birthday: new_user.birthday,
                        location: new_user.location,
                        sex: new_user.sex, 
                        description: new_user.description,
                }, {new: true, useFindAndModify: false}
        ).exec();
}

// update user with given id profile image
function updateUserProfile(id, profile_path){
        return models.User.updateOne(
                {_id: id},
                {profile_path: profile_path},
        ).exec();
}
function updatePostsAuthorName(id, name){
        return models.Post.updateMany({author_id: id}, {author_name: name}).exec();     
}
function updateCommentsAuthorName(id, name){
        return models.Comment.updateMany({author_id: id}, {author_name: name}).exec();
}

// update interests of user with provided user_id
function updateUserInterests(user_id, new_interests){
        return models.User.findByIdAndUpdate(
                {_id: user_id}, 
                {interests: new_interests},
                {useFindAndModify: false},
        ).exec();
}

// update user with given user_id last time logout
function updateUserLastLogout(user_id, time){
        return models.User.updateOne(
                {_id: user_id},
                {lastLogout: time},
        ).exec();
}

// get user simples based on provided user_id list 'users'
function getUsers(users){
        users = users.map(mongoose.Types.ObjectId);
        return models.User.aggregate([
                {$project:
                        {
                                name: '$name',
                                sex: '$sex',
                                birthday: '$birthday',
                                location: '$location',
                                description: '$description',
                                profile_path: '$profile_path',
                                interests: '$interests',
                        }
                },
                {$match:
                        {_id: {$in: users}},
                }
        ]).exec();
}

// get all users and their interests
function getAllUserInterests(){
        return models.User.aggregate([
                {$project:
                        {interests: '$interests'}
                },
        ]).exec();
}

// check whether provided email was already registered
function checkExistingUser(email){
        return models.User.findOne({email: email}).exec();
}

// filter based on given interest
function getUsersBasedOnInfo(name, date, sex, interests){
        return models.User.aggregate([
                {$project:  
                        {       
                                name: '$name',
                                sex: '$sex',
                                birthday: '$birthday',
                                location: '$location',
                                description: '$description',
                                profile_path: '$profile_path',
                                size: {$size: {$setIntersection: ['$interests', interests]}},
                                interest: '$interests',
                        }
                },
                {$match: 
                        {
                                size: interests.length,
                                name: {
                                        $regex: name,
                                        $options: "i",
                                },
                                sex: {$in: sex},
                                $and: [{birthday: {$gte: date[0]}}, {birthday: {$lte: date[1]}}],
                        },
                        
                },
        ]).exec();
}

// check for existance user with provided email
function checkUserForLogin(email){
        return models.User.findOne({email: email}).exec();
}

// add new post of user
function addNewPost(author_id, status, image_path){
        return new models.Post({
                        author_id: author_id, 
                        status: status, 
                        image_path: image_path,
                        followers: [author_id],
                        likes: [],
                        comments: [],
                }).save();
}

// find all posts of user with provided author_id
function findAllPosts(author_id){
        return models.Post.find({author_id: author_id}).sort({updatedAt: 'desc'}).exec();
}

// add user with given user_id like to post with provided post_id
function addUserLikeToPost(user_id, post_id, cb){
        models.Post.findByIdAndUpdate(
                {_id: post_id},
                {$push: {likes: user_id}},
        ).exec()
        .then(post => cb(post));

        models.User.findByIdAndUpdate(
                {_id: user_id},
                {$push: {liked_posts: post_id}},
        ).exec()
        .catch(err => console.log(err));
}

// remove user with given user_id like from post with provided post_id
function removeUserLikeFromPost(user_id, post_id, cb){
        models.Post.findByIdAndUpdate(
                {_id: post_id},
                {$pull : {likes: user_id}},
        ).exec()
        .then(post => cb(post));

        models.User.findByIdAndUpdate(
                {_id: user_id},
                {$pull : {liked_posts: post_id}},
        ).exec()
        .catch(err => console.log(err));
}

// user follows a post
function addUserToPost(post_id,user_id, cb){
        models.Post.findByIdAndUpdate(
                {_id: post_id}, 
                {$push : {followers: user_id}}, 
        ).exec()
        .then(post => cb(post));

        models.User.findByIdAndUpdate(
                {_id: user_id},
                {$push: {following_posts: post_id}},
        ).exec()
        .catch(err => console.log(err));
}


// user unfollows a post
function removeUserFromPost(post_id,user_id, cb){
        models.User.findByIdAndUpdate(
                {_id: user_id},
                {$pull: {following_posts: post_id}},
        ).exec();
        
        models.Post.findByIdAndUpdate(
                {_id: post_id},
                {$pull: {followers: user_id}},
        ).exec()
        .then(post => cb(post))
        .catch(err => console.log(err));
}

// change the password based on provided email
function changePassword(email, new_password){
        return models.User.findOneAndUpdate({email:email}, {password: new_password}, {useFindAndModify: false}).exec();
}

// add new image comment from author_id to post_id
function addNewComment(post_id, author_id, comment, image_path){
        return new models.Comment({
                        post_id: post_id, 
                        author_id: author_id, 
                        comment: comment, 
                        image_path: image_path
                }).save();
}

// add new comment from author_id to post_id
function addCommentToPost(comment_id, post_id, author_id){
        return models.Post.findByIdAndUpdate(
                        {_id: post_id}, 
                        {$push : 
                                {comments: 
                                        {
                                                comment_id: comment_id,
                                                author_id: author_id,
                                        }
                                }
                        }, 
                ).exec();
}

// get post based on provided post_id
function getPost(post_id){
        return models.Post.findById(post_id).exec();
}

// get posts given in list
function getPosts(list){
        return models.Post.find({_id: {$in: list}}).exec();
}

// get all posts based on recommended users id and date
function getPostsForUser(recommended_users, date, seenPosts){
        seenPosts = seenPosts.map(mongoose.Types.ObjectId);
        if(seenPosts.length > 0){
                console.log('aaaaaaaaaaaaaaa');
                return models.Post.aggregate([
                        {$project:
                                {
                                        author_id: '$author_id',
                                        status: '$status',
                                        image_path: '$image_path',
                                        likes: '$likes',
                                        nComments: {$size: '$comments'},
                                        updatedAt: '$updatedAt',
                                }
                        },
                        {$match:
                                {
                                        '_id': {$nin: seenPosts},
                                        'author_id': {$in: recommended_users},
                                        'updatedAt': {$gte: date},
                                }
                        },
                        {$sort: {updatedAt: -1}},
                ]).exec();
        }
        else {
                return models.Post.aggregate([
                        {$project:
                                {
                                        author_id: '$author_id',
                                        status: '$status',
                                        image_path: '$image_path',
                                        likes: '$likes',
                                        nComments: {$size: '$comments'},
                                        updatedAt: '$updatedAt',
                                }
                        },
                        {$match:
                                {author_id: {'$in': recommended_users}},
                        },
                        {$sort: {updatedAt: -1}},
                ]).exec();
        }
}

// get all comments from the comment id array 'comments'
function getAllComments(comments){
        return models.Comment.find({_id: {$in: comments}}).sort({createdAt: 'desc'}).exec();
}

// get all likes from post with provided post_id
function getAllLikesFrom(post_id){
        return models.Post.aggregate([
                {$project: {likes: '$likes'}},
                {$match: {_id: mongoose.Types.ObjectId(post_id)}},
        ]).exec();
}

// get chat room based on provided roomId
function getChatRoom(roomId){
        return models.ChatRoom.findById(roomId).exec();
}

// get chatroom with provided users id and add new msg to room
function getChatRoomWith(user_1,user_2, msg){
        let u1 = user_1,
            u2 = user_2;
        if(user_1 > user_2){
                u1 = user_2;
                u2 = user_1;
        }
        return models.ChatRoom.findOneAndUpdate(
                        {'users.0.user_id': u1, 'users.1.user_id': u2},
                        {$push: 
                                {messages:
                                        {
                                                author_id: msg.author_id,
                                                message: msg.message,
                                                image_path: msg.image_path,
                                        }
                                }
                        }
                ).exec();
}

// get all chat rooms for user_id
function getAllChatRooms(user_id){
        return models.ChatRoom.find({'users.user_id': user_id}).exec();
}

// update the last time user with provided user_id in chatroom with given roomId
function updateLastSeen(roomId, user_id, lastseen){
        return models.ChatRoom
                .updateOne(
                        {_id: roomId, 'users.user_id': user_id}, 
                        {$set: {'users.$.lastseen': lastseen}},
                )
                .exec();
}

// create chat room for user_1 and user_2
function createNewChatRoom(user_1,user_2, msg){
        let date = new Date(),
            u1 = user_1,
            u2 = user_2;
        if(user_1 > user_2){
                u1 = user_2;
                u2 = user_1;
        }
        return new models.ChatRoom({
                        users: [
                                {user_id: u1, lastseen: date},
                                {user_id: u2, lastseen: date},
                        ], 
                        messages: [{
                                author_id: msg.author_id,
                                message: msg.message,
                                image_path: msg.image_path,
                        }],
                }).save();
}

// add a new msg to chat room
function addNewMessage(msg){
        return models.ChatRoom
                .findByIdAndUpdate(
                        {_id: msg.roomId}, 
                        {$push : 
                                {messages: 
                                        {
                                                author_id: msg.author_id, 
                                                message: msg.message,
                                                image_path: msg.image_path
                                        }
                                }
                        }, 
                ).exec();
}

// pushing notification when user is offline
// Push notification to user with user_id. 
//Notification contains information of: post (post_id), the author of the post (post_author_id), person who comments/likes (comment_like_author_id), type of noti (comment/like)
function pushNotification(user_id, post_id, comment_id,comment_like_author_id, kind){
        return models.User.findByIdAndUpdate(
                        {_id: user_id},
                        {$push: 
                                {notifications: 
                                        {
                                                post_id: post_id,
                                                comment_id: comment_id,
                                                comment_like_author_id: comment_like_author_id,
                                                kind: kind,
                                        }
                                }
                        },
                ).exec();
}


// add a follower with given follower_id to user with provided user_id (follow)
function addNewFollower(user_id, follower_id, userFinish, followerFinish){
        models.User.findByIdAndUpdate(
                {_id: follower_id},
                {$push: {followees: user_id}},
        ).exec()
        .then(follower => followerFinish())
        .catch(err => {
                followerFinish();
                console.log(err);
        });

        models.User.findByIdAndUpdate(
                {_id: user_id},
                {$push: {followers: follower_id}},
        ).exec()
        .then(user => userFinish(user))
        .catch(err => console.log(err));
}

// remove a follower with given follower_id from user with provided user_id (unfollow)
function removeFollower(user_id, follower_id, userFinish, followerFinish, users){
        models.User.findByIdAndUpdate(
                {_id: follower_id},
                {
                        $pull: {followees: user_id},
                },
        ).exec()
        .then(follower => {
                users[0] = follower
                followerFinish();
        })
        .catch(err => {
                users[0] = null;
                followerFinish();
                console.log(err);
        });

        models.User.findByIdAndUpdate(
                {_id: user_id},
                {
                        $pull: {followers: follower_id},
                },
        ).exec()
        .then(user => {
                users[1] = user;
                userFinish(user);
        })
        .catch(err => {
                users[1] = null;
                console.log(err)
        });
}

// just for testing
function getAllPosts(){
        return models.Post.find();
}

// just for testing
function getAllUsers(){
        return models.User.find();
}

module.exports = {
        creatNewUser: creatNewUser,
        getUser: getUser,
        getUserBasic: getUserBasic,
        getUsers: getUsers,
        getAllUserInterests: getAllUserInterests,
        getUsersBasedOnInfo: getUsersBasedOnInfo,
        checkExistingUser: checkExistingUser,
        checkUserForLogin: checkUserForLogin,
        changePassword: changePassword,
        updateUserBasicInfo: updateUserBasicInfo,
        updateUserInterests: updateUserInterests,
        updateUserProfile: updateUserProfile,
        updateUserLastLogout: updateUserLastLogout,

        addNewPost: addNewPost,
        findAllPosts: findAllPosts,
        getPost: getPost,
        getPosts: getPosts,
        getPostsForUser: getPostsForUser,
        updatePostsAuthorName: updatePostsAuthorName,

        addUserToPost: addUserToPost,
        removeUserFromPost: removeUserFromPost,

        addUserLikeToPost: addUserLikeToPost,
        removeUserLikeFromPost: removeUserLikeFromPost,

        updateCommentsAuthorName: updateCommentsAuthorName,
        addNewComment: addNewComment,
        addCommentToPost: addCommentToPost,
        getAllComments: getAllComments,

        getAllLikesFrom: getAllLikesFrom,

        getChatRoomWith: getChatRoomWith,
        getChatRoom: getChatRoom,
        getAllChatRooms: getAllChatRooms,
        createNewChatRoom: createNewChatRoom,
        addNewMessage: addNewMessage,
        updateLastSeen: updateLastSeen,

        pushNotification: pushNotification,

        addNewFollower: addNewFollower,
        removeFollower: removeFollower,

        getAllPosts: getAllPosts,
        getAllUsers: getAllUsers,
}