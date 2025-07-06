var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var attractionImageSchema = new Schema({
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'url' : String,
	'source' : String,
	'uploadedBy' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'createdAt' : Date
});

module.exports = mongoose.model('attractionImage', attractionImageSchema);
