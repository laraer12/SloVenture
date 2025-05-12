var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var nearbyAttractionSchema = new Schema({
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'nearbyAttractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'distance' : Number
});

module.exports = mongoose.model('nearbyAttraction', nearbyAttractionSchema);
