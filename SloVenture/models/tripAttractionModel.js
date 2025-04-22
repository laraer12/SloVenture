var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var tripAttractionSchema = new Schema({
	'tripId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'trip'
	},
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'order' : Number,
	'description' : String,
	'plannedVisitTime' : Date
});

module.exports = mongoose.model('tripAttraction', tripAttractionSchema);
