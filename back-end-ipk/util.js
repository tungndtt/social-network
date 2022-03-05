const process = require('process');
const storage = require('./storage');

const allInterests = ['GAMING', 'READING', 'TRAVELLING', 'SPORT', 'SHOPPING','LEARNING', 'GOSSIP'];

function getToken(s){
    return s.split(' ')[1];
}

function updateScore(heap, score, id, ...args){
    let available = {};
    args.forEach(arg => {
        arg.forEach(user_id => {
            if(!available[user_id] && user_id !== id){
                available[user_id] = true;
                heap.updateScore(user_id, id, score);
                heap.updateScore(id, user_id, score);

                // show heap score of user_id
                heap.showHeapOfUser(user_id);
            }
        });
    });
}

function isDifferent(arr1, arr2){
    if(arr1.length !== arr2.length) return true;
    for(let i=0; i<arr1.length; i++){
        if(arr1[i] !== arr2[i]) return true;
    }
    return false;
}

function getScoreToUpdate(new_interests, other_interests, old_interests){
    let tmp = [];
    let score = 0;
    for(let i=0; i<allInterests.length; i++) tmp.push(0);
    other_interests.forEach(interest => tmp[interest] = 1);
    old_interests.forEach(interest => {
        score -= tmp[interest]>0 ? 1 : 0;
        tmp[interest] -= 0.5
    });
    new_interests.forEach(interest => score += tmp[interest]>0 ? 1 : 0);
    return score;
}

function saveScoresBeforeExit(heap){
    let cb = () => {
        let obj = heap.getHeapOfUsers();
        storage.writeScoresToFile(obj);
    };
    process.on('exit', code => {
        console.log("exit the process with code " + code);
        cb();
    });

    process.on('SIGINT', () => process.exit());
}

module.exports = {
    getToken: getToken,
    updateScore: updateScore,
    isDifferent: isDifferent,
    saveScoresBeforeExit: saveScoresBeforeExit,
    getScoreToUpdate: getScoreToUpdate,
}