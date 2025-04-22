var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var nearbyAccomodationSchema = new Schema({
	'name' : String,
	'linkToBooking' : String,
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'distance' : Number
});

module.exports = mongoose.model('nearbyAccomodation', nearbyAccomodationSchema);
