var mongoose = require('mongoose');
var Schema   = mongoose.Schema;
const { CoordinatesSchema } = require('./attractionModel');
var regionSchema = new Schema({
	'name' : String,
	'location': [CoordinatesSchema],
});

module.exports = mongoose.model('region', regionSchema);