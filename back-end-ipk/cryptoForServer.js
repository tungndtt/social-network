const 
    crypto = require('crypto'),
    fs = require('fs'),
    publicKey = fs.readFileSync(__dirname+'\\key\\public-key.pem'),
    privateKey = fs.readFileSync(__dirname+'\\key\\private-key.pem');
 
function decryptPacket(data, mKey){
    var myStr = Buffer.from(data, 'base64');
    return crypto.privateDecrypt({key: mKey, padding:crypto.constants.RSA_PKCS1_PADDING},myStr).toString('utf-8');
};

function encryptPacket(data, mKey){
    var myStr = Buffer.from(data,"utf-8");
    var encoded = crypto.publicEncrypt({key: mKey, padding:crypto.constants.RSA_PKCS1_PADDING},myStr).toString("base64");
    return encoded;
};

module.exports = {
    publicKey: publicKey,
    privateKey: privateKey,
    encrypt: encryptPacket,
    decrypt: decryptPacket,
};