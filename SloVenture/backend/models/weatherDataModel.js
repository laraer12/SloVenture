var mongoose = require('mongoose');
var Schema   = mongoose.Schema;
var CoordinatesSchema = require('./attractionModel.js').schema;

var weatherSchema = new Schema({
	'date' : Date,
	'temperature' : Number,
	'maxTemperature' : Number,
	'minTemperature' : Number,
	'condition' : String,
	'precipationProbability' : Number
});

var weatherForecastSchema = new Schema({
	'daily' : [weatherSchema],
});

var weatherDataSchema = new Schema({
	'location' : CoordinatesSchema,
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'currentWeather' : weatherSchema,
	'forecast' : weatherForecastSchema,
	'lastUpdated' : Date
});

module.exports = mongoose.model('weatherData', weatherDataSchema);