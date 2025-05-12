var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var userSavedSchema = new Schema({
	'userId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	}
});

module.exports = mongoose.model('userSaved', userSavedSchema);
