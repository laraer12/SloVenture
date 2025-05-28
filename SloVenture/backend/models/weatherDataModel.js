var mongoose = require('mongoose');
var Schema   = mongoose.Schema;
var {CoordinatesSchema} = require('./attractionModel.js');

var weatherSchema = new Schema({
	'date' : Date,
	'maxTemperature' : Number,
	'minTemperature' : Number,
	'condition' : String,
	'precipitationProbabilityMax' : Number
});

var weatherDataSchema = new Schema({
	'location' : CoordinatesSchema,
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'forecast' :[weatherSchema],
	'lastUpdated' : Date
});

module.exports = mongoose.model('weatherData', weatherDataSchema);
