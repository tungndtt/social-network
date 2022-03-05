const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const User = new Schema({
    name:{
        type: String,
        required: true
    },
    email:{
        type:String,
        unique:true,
        required:true
    },
    birthday: {
        type: Date,
    },
    sex: {
        type: Number,
    },
    location: {
        type: String,
    },
    interests: [{
        type: Number,
    }],
    description: {
        type: String,
    },
    password:{
        type:String,
        required:true
    },
    profile_path: {
        type: String,
    },
    following_posts: [{
        type: String,
    }],
    liked_posts: [{
        type: String,
    }],
    followers: [{
        type: String,
    }],
    followees: [{
        type: String,
    }],
    notifications: [new Schema({
        post_id: {
            type: String,
        },
        comment_id: {
            type: String,
        },
        comment_like_author_id: {
            type: String,
        },
        kind: {
            type: String,
        },
    }, {timestamps: true})],
    lastLogout: {
        type: Date,
    },
});

const Post = new Schema({
    author_id: {
        type: String,
        required: true
    },
    status: {
        type: String,
    }, 
    image_path: {
        type: String,
    },
    followers: [{
        type: String,
    }],
    likes: [{
        type: String,
    }],
    comments: [new Schema({
        comment_id: {
            type: String,
        },
        author_id: {
            type: String,
        }
    })],
}, {timestamps: true});

const Comment = new Schema({
    post_id: {
        type: String,
        required: true
    },
    author_id: {
        type: String,
        required: true
    },
    comment: {
        type: String,
    },
    image_path: {
        type: String,
    }
}, {timestamps: true});

const ChatRoom = new Schema({
    users: [new Schema({
        user_id: {
            type: String,
            required: true,
        },
        lastseen: {
            type: Date,
        }
    })],
    messages: [new Schema({
        author_id:{
            type: String,
            required: true
        },
        message: {
            type: String,
        },
        image_path: {
            type: String,
        },
    }, {timestamps: true})]
}, {timestamps: true});

module.exports = {
    User: mongoose.model('User',User,'User'),
    Post: mongoose.model('Post', Post, 'Post'),
    Comment: mongoose.model('Comment', Comment, 'Comment'),
    ChatRoom: mongoose.model('ChatRoom', ChatRoom, 'ChatRoom'),
};