var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var weatherDataSchema = new Schema({
	'lat': Number,
	'lon': Number,
	'attractionId' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'attraction'
	},
	'currentWeather' : String,
	'forecast' : String,
	'lastUpdated' : Date
});

module.exports = mongoose.model('weatherData', weatherDataSchema);
