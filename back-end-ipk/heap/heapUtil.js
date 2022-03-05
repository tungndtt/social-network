const heap = require('./heap');

var users_table = {};

/**
 * initialise the heap with provided users
 * @param {*} users 
 * @returns void
 */
function init(scores) {
    for(let id in scores){
        users_table[id] = new heap.Heap();
        scores[id].forEach(e => {
            if(e) users_table[id].add(e);
        });
        showHeapOfUser(id);
    }
}

function getSimilarity(u1,u2){
    const common = (arr1, arr2) => {
        let dict = {};
        arr1.forEach(e => dict[e] = true);
        let ans = 0;
        arr2.forEach(e => ans += dict[e]? 1 : 0);
        return ans;
    };
    return common(u1.interests, u2.interests)*0.5;
}

/**
 * add a new user
 * @param {*} users 
 * @param {*} new_user 
 * @returns void
 */
function addNewUser(users, new_user){
    let new_id = new_user._id.toString();
    users_table[new_id] = new heap.Heap();
    users.forEach(user => {
        let id = user._id.toString();
        if(new_id !== id){
            let similarity = getSimilarity(new_user, user);
            users_table[id].add([new_id, similarity]);
            users_table[new_id].add([id, similarity]);
        }
    });
}

/**
 * updates score of user_1 to user_2
 * @param {*} user_1 
 * @param {*} user_2 
 * @param {*} score 
 * @returns void
 */
function updateScore(user_1, user_2, score){
    if(user_1 !== user_2){
        console.log("Add Score of user_2 in heap of user_1");
        console.log("user_1: ", user_1);
        console.log("user_2: ", user_2);
        users_table[user_1].update(user_2, score, 1);
    }
}

function setScore(user_1, user_2, score){
    if(user_1 !== user_2){
        console.log("Set new score of user_2 in heap of user_1");
        console.log("user_1: ", user_1);
        console.log("user_2: ", user_2);
        users_table[user_1].update(user_2, score, 0);
    }
}

/**
 * remove user_2 from heap of user_1 
 * @param {*} user_1 
 * @param {*} user_2 
 * @returns void
 */
function removeFrom(user_1, user_2){
    if(user_1 !== user_2){
        console.log("Remove user_2 from heap of user_1");
        console.log("user_1: ", user_1);
        console.log("user_2: ", user_2);
        users_table[user_1].remove(user_2);
    }
}

/**
 * add user_2 to heap of user_1
 * @param {*} user_1 
 * @param {*} user_2 
 * @returns void
 */
function addTo(user_1, user_2){
    let id_1 = user_1._id.toString(),
        id_2 = user_2._id.toString();
    console.log(users_table[id_1].getScoreOf(id_2));
    if(id_1 !== id_2 && users_table[id_1].getScoreOf(id_2) === null){
        console.log("Set score user_2 to heap of user_1");
        console.log("user_1: ", id_1);
        console.log("user_2: ", id_2);
        let similarity = getSimilarity(user_1, user_2);
        users_table[id_1].add([id_2, similarity]);
    }
}

/**
 * get k users with maximal scores
 * @param {*} user 
 * @param {*} k 
 * @returns array
 */
function getKMaxFrom(user, k){
    let users = users_table[user].getKMax(k);
    let ans = [];
    users.forEach(e => ans.push(e[0]));
    return ans;
}

/**
 * show heap score of given user
 * @param {*} user_id 
 * @returns void
 */
function showHeapOfUser(user_id){
    if(users_table[user_id])
        console.log(user_id ,users_table[user_id].heap);
    else
        console.log(user_id + ' is not existing!');
}

/**
 * get heap score of given user
 * @param {*} user_id 
 * @returns Array
 */
 function getHeapOfUsers(){
    let obj = {};
    for(let id in users_table){
        obj[id] = users_table[id].heap;
    }
    return obj;
}

module.exports = {
    init: init,
    addNewUser: addNewUser,
    removeFrom: removeFrom,
    addTo: addTo,
    updateScore: updateScore,
    setScore: setScore,
    getKMaxFrom: getKMaxFrom,
    showHeapOfUser: showHeapOfUser,
    getHeapOfUsers: getHeapOfUsers,
};