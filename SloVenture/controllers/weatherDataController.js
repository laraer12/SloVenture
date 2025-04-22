const axios = require('axios');
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
    list: async function (req, res) {
        try {
            const weatherDatas = await WeatherdataModel.find();
            return res.json(weatherDatas);
        }
        catch (err) {
            return res.status(500).json({
                message: 'Error when getting weatherData',
                error: err
            });
        }
    },

    /**
     * weatherDataController.show()
     */
    show: async function (req, res) {
        const id = req.params.id;

        try {
            const weatherData = await WeatherdataModel.findOne({ _id: id });

            if (!weatherData) {
                return res.status(404).json({
                    message: 'No such weatherData'
                });
            }
            return res.json(weatherData);
        }
        catch (err) {
            return res.status(500).json({
                message: 'Error when getting weatherData',
                error: err
            });
        }
    },

    /**
     * weatherDataController.create()
     */
    create: async function (req, res) {
        const weatherData = new WeatherdataModel({
            attractionId: req.body.attractionId,
            currentWeather: req.body.currentWeather,
            forecast: req.body.forecast,
            lastUpdated: req.body.lastUpdated
        });

        try {
            const savedWeatherData = await weatherData.save();
            return res.status(201).json(savedWeatherData);
        }
        catch (err) {
            return res.status(500).json({
                message: 'Error when creating weatherData',
                error: err
            });
        }
    },

    /**
     * weatherDataController.update()
     */
    update: async function (req, res) {
        const id = req.params.id;

        try {
            const weatherData = await WeatherdataModel.findOne({ _id: id });

            if (!weatherData) {
                return res.status(404).json({
                    message: 'No such weatherData'
                });
            }
            weatherData.attractionId = req.body.attractionId || weatherData.attractionId;
            weatherData.currentWeather = req.body.currentWeather || weatherData.currentWeather;
            weatherData.forecast = req.body.forecast || weatherData.forecast;
            weatherData.lastUpdated = req.body.lastUpdated || weatherData.lastUpdated;

            const updatedWeatherData = await weatherData.save();
            return res.json(updatedWeatherData);
        }
        catch (err) {
            return res.status(500).json({
                message: 'Error when updating weatherData',
                error: err
            });
        }
    },

    /**
     * weatherDataController.remove()
     */
    remove: async function (req, res) {
        const id = req.params.id;

        try {
            await WeatherdataModel.findByIdAndRemove(id);
            return res.status(204).json();
        }
        catch (err) {
            return res.status(500).json({
                message: 'Error when deleting the weatherData',
                error: err
            });
        }
    },

    // pridobim vreme in napoved glede na lat in lon
    getWeatherByCoordinates: async function (req, res) {
        try {
            const { lat, lon, cnt, attractionId } = req.query;

            if (!lat || !lon || !cnt || !attractionId) {
                return res.status(400).json({
                    message: 'Latitude, longitude,number of days and attraction ID are required'
                });
            }
            const apiKey = process.env.OPENWEATHER_API_KEY;
    
            if (!apiKey) {
                return res.status(500).json({
                    message: 'OpenWeather API key is not defined.'
                });
            }
            const currentWeatherUrl  = `https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiKey}`;
            const currentResponse  = await axios.get(currentWeatherUrl);
            const currentWeather = currentResponse.data.weather[0].description;
            
            const forecastUrl  = `https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&cnt=${cnt}&appid=${apiKey}`;
            const forecastResponse  = await axios.get(forecastUrl);
            const forecastList = forecastResponse.data.list.slice(0, cnt || 5);
            
            return res.status(200).json({
                lat: lat,
                lon: lon,
                attractionId: attractionId,
                currentWeather: currentWeather,
                forecast: forecastList,
                lastUpdated: new Date()
            });
        }
        catch (error) {
            return res.status(500).json({
                message: 'Error fetching weather from OpenWeather API',
                error: error.message
            });
        }
    }
};