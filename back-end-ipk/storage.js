// File storage engine
const multer = require('multer');
const path = require('path');
const crypto = require('crypto');

// File system reader engine
const fs = require('fs');

const storage = multer.diskStorage({
    destination: __dirname+'\\public\\images\\',
    filename: function(req, file, cb) {
        if(file){
            return crypto.pseudoRandomBytes(16, function(err, raw) {
                if (err) {
                    return cb(err);
                }
                return cb(null, "" + (raw.toString('hex')) + (path.extname(file.originalname)));
            });
        } else return null;
    }
});

function urlParser(hostUrl, filename){
    return "https://192.168.1.116:3000/images/"+filename
}

function getCertificate(file){
    return fs.readFileSync(__dirname+"\\certificate\\"+file);
};

// write scores to save into heap/score.txt file
function writeScoresToFile(score_table){
    fs.writeFileSync(__dirname+"\\heap\\score.txt", JSON.stringify(score_table));
}

// read scores from heap/score.txt file
// return json object 
function readScoresFromFile(){
    return JSON.parse(fs.readFileSync(__dirname + "\\heap\\score.txt").toString());
};

module.exports = {
    handleFile: multer({ storage: storage }).single('upload'),
    getCertificate: getCertificate,
    writeScoresToFile: writeScoresToFile,
    readScoresFromFile: readScoresFromFile,
    urlParser: urlParser
};   