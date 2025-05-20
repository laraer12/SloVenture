var mongoose = require('mongoose');
var Schema   = mongoose.Schema;
var CoordinatesSchema = require('./attractionModel.js').schema;

var regionSchema = new Schema({
	'name' : String,
	'location': [CoordinatesSchema],
});

module.exports = mongoose.model('region', regionSchema);
