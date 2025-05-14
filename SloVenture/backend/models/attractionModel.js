var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var attractionSchema = new Schema({
	'name' : String,
	'regionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'region'
	},
	'location' : String,
	'address' : String,
	'description' : String,
	'classification' : String,
	'locationType' : String,
	'elevation' : Number,
	'accessibilityOptions' : String,
	'ratingFamilyFriendly' : Number,
	'ratingElderlyFriendly' : Number,
	'ratingAccessible' : Number,
	'rating' : Number,
	'parkingInfo' : String,
	'requiresReservation' : Boolean,
	'openingHours' : String,
	'entryFee' : Number,
	'hikingInfo' : String,
	'googleMapsLink' : String,
	'createdAt' : Date,
	'verified' : Boolean
});

module.exports = mongoose.model('attraction', attractionSchema);
