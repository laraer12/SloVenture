var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var userViewHistorySchema = new Schema({
	'userId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'viewedAt' : Date
});

module.exports = mongoose.model('userViewHistory', userViewHistorySchema);
