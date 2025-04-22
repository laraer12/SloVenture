var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var tripSchema = new Schema({
	'userId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'name' : String,
	'description' : String,
	'startDate' : Date,
	'endDate' : Date,
	'isPublic' : Boolean,
	'createdAt' : Date
});

module.exports = mongoose.model('trip', tripSchema);
