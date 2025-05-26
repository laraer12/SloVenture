var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var reviewSchema = new Schema({
	'userId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'rating' : Number,
	'ratingFamilyFriendly' : Number,
	'ratingElderlyFriendly' : Number,
	'ratingAccessible' : Number,
	'createdAt' : Date
});

module.exports = mongoose.model('review', reviewSchema);