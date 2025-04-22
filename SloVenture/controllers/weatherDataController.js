var WeatherdataModel = require('../models/weatherDataModel.js');

/**
 * weatherDataController.js
 *
 * @description :: Server-side logic for managing weatherDatas.
 */
module.exports = {

    /**
     * weatherDataController.list()
     */
    list: function (req, res) {
        WeatherdataModel.find(function (err, weatherDatas) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting weatherData.',
                    error: err
                });
            }

            return res.json(weatherDatas);
        });
    },

    /**
     * weatherDataController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        WeatherdataModel.findOne({_id: id}, function (err, weatherData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting weatherData.',
                    error: err
                });
            }

            if (!weatherData) {
                return res.status(404).json({
                    message: 'No such weatherData'
                });
            }

            return res.json(weatherData);
        });
    },

    /**
     * weatherDataController.create()
     */
    create: function (req, res) {
        var weatherData = new WeatherdataModel({
			attractionId : req.body.attractionId,
			currentWeather : req.body.currentWeather,
			forecast : req.body.forecast,
			lastUpdated : req.body.lastUpdated
        });

        weatherData.save(function (err, weatherData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating weatherData',
                    error: err
                });
            }

            return res.status(201).json(weatherData);
        });
    },

    /**
     * weatherDataController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        WeatherdataModel.findOne({_id: id}, function (err, weatherData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting weatherData',
                    error: err
                });
            }

            if (!weatherData) {
                return res.status(404).json({
                    message: 'No such weatherData'
                });
            }

            weatherData.attractionId = req.body.attractionId ? req.body.attractionId : weatherData.attractionId;
			weatherData.currentWeather = req.body.currentWeather ? req.body.currentWeather : weatherData.currentWeather;
			weatherData.forecast = req.body.forecast ? req.body.forecast : weatherData.forecast;
			weatherData.lastUpdated = req.body.lastUpdated ? req.body.lastUpdated : weatherData.lastUpdated;
			
            weatherData.save(function (err, weatherData) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating weatherData.',
                        error: err
                    });
                }

                return res.json(weatherData);
            });
        });
    },

    /**
     * weatherDataController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        WeatherdataModel.findByIdAndRemove(id, function (err, weatherData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the weatherData.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
