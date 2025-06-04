var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var userVisitSchema = new Schema({
	'userId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'visitDate' : Date,
	'isFakeData': Boolean
});

module.exports = mongoose.model('userVisit', userVisitSchema);
