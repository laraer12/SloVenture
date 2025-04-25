const axios = require('axios');
var AttractionModel = require('../models/attractionModel.js');

/**
 * attractionController.js
 *
 * @description :: Server-side logic for managing attractions.
 */
module.exports = {

    /**
     * attractionController.list()
     */
    list: function (req, res) {
        AttractionModel.find(function (err, attractions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attraction.',
                    error: err
                });
            }

            return res.json(attractions);
        });
    },

    /**
     * attractionController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        AttractionModel.findOne({_id: id}, function (err, attraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attraction.',
                    error: err
                });
            }

            if (!attraction) {
                return res.status(404).json({
                    message: 'No such attraction'
                });
            }

            return res.json(attraction);
        });
    },

    /**
     * attractionController.create()
     */
    create: function (req, res) {
        var attraction = new AttractionModel({
			name : req.body.name,
			regionId : req.body.regionId,
			location : req.body.location,
			address : req.body.address,
			description : req.body.description,
			locationType : req.body.locationType,
			elevation : req.body.elevation,
			accessibilityOptions : req.body.accessibilityOptions,
			ratingFamilyFriendly : req.body.ratingFamilyFriendly,
			ratingElderlyFriendly : req.body.ratingElderlyFriendly,
			ratingAccessible : req.body.ratingAccessible,
			rating : req.body.rating,
			parkingInfo : req.body.parkingInfo,
			requiresReservation : req.body.requiresReservation,
			openingHours : req.body.openingHours,
			entryFee : req.body.entryFee,
			hikingInfo : req.body.hikingInfo,
			googleMapsLink : req.body.googleMapsLink,
			createdAt : req.body.createdAt,
			verified : req.body.verified
        });

        attraction.save(function (err, attraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating attraction',
                    error: err
                });
            }

            return res.status(201).json(attraction);
        });
    },

    /**
     * attractionController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        AttractionModel.findOne({_id: id}, function (err, attraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attraction',
                    error: err
                });
            }

            if (!attraction) {
                return res.status(404).json({
                    message: 'No such attraction'
                });
            }

            attraction.name = req.body.name ? req.body.name : attraction.name;
			attraction.regionId = req.body.regionId ? req.body.regionId : attraction.regionId;
			attraction.location = req.body.location ? req.body.location : attraction.location;
			attraction.address = req.body.address ? req.body.address : attraction.address;
			attraction.description = req.body.description ? req.body.description : attraction.description;
			attraction.locationType = req.body.locationType ? req.body.locationType : attraction.locationType;
			attraction.elevation = req.body.elevation ? req.body.elevation : attraction.elevation;
			attraction.accessibilityOptions = req.body.accessibilityOptions ? req.body.accessibilityOptions : attraction.accessibilityOptions;
			attraction.ratingFamilyFriendly = req.body.ratingFamilyFriendly ? req.body.ratingFamilyFriendly : attraction.ratingFamilyFriendly;
			attraction.ratingElderlyFriendly = req.body.ratingElderlyFriendly ? req.body.ratingElderlyFriendly : attraction.ratingElderlyFriendly;
			attraction.ratingAccessible = req.body.ratingAccessible ? req.body.ratingAccessible : attraction.ratingAccessible;
			attraction.rating = req.body.rating ? req.body.rating : attraction.rating;
			attraction.parkingInfo = req.body.parkingInfo ? req.body.parkingInfo : attraction.parkingInfo;
			attraction.requiresReservation = req.body.requiresReservation ? req.body.requiresReservation : attraction.requiresReservation;
			attraction.openingHours = req.body.openingHours ? req.body.openingHours : attraction.openingHours;
			attraction.entryFee = req.body.entryFee ? req.body.entryFee : attraction.entryFee;
			attraction.hikingInfo = req.body.hikingInfo ? req.body.hikingInfo : attraction.hikingInfo;
			attraction.googleMapsLink = req.body.googleMapsLink ? req.body.googleMapsLink : attraction.googleMapsLink;
			attraction.createdAt = req.body.createdAt ? req.body.createdAt : attraction.createdAt;
			attraction.verified = req.body.verified ? req.body.verified : attraction.verified;
			
            attraction.save(function (err, attraction) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating attraction.',
                        error: err
                    });
                }

                return res.json(attraction);
            });
        });
    },

    /**
     * attractionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        AttractionModel.findByIdAndRemove(id, function (err, attraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the attraction.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    },

    /**
     * Iskanje znamenitosti po imenu / lokaciji z uporabo LocationIQ
    */
    search: async function (req, res) {
        const { attractionName, lat, lon, autocomplete } = req.query;
        const apiKey = process.env.LOCATIONIQ_API_KEY;
    
        // validiram vhodne parametre
        if (lat || lon) {
            const parsedLat = parseFloat(lat);
            const parsedLon = parseFloat(lon);
            
            if (isNaN(parsedLat) || isNaN(parsedLon)) {
                return res.status(400).json({
                    message: 'Invalid latitude or longitude. Please provide valid numeric values'
                });
            }
    
            // preverim, ali sta lat in lon v razponu
            if (parsedLat < -90 || parsedLat > 90) {
                return res.status(400).json({
                    message: 'Latitude must be between -90 and 90 degrees'
                });
            }
            if (parsedLon < -180 || parsedLon > 180) {
                return res.status(400).json({
                    message: 'Longitude must be between -180 and 180 degrees'
                });
            }
        }
        if (attractionName) {
            // preverim, če je attractionName tipa string
            if (typeof attractionName !== 'string' || attractionName.trim().length === 0) {
                return res.status(400).json({
                    message: 'Invalid attraction name. Please provide a valid non-empty string.'
                });
            }
        }
        try {
            // iskanje po lat in lon
            if (lat && lon) {
                const reverseResponse = await axios.get('https://us1.locationiq.com/v1/reverse.php', {
                    params: {
                        key: apiKey,
                        lat: parseFloat(lat),
                        lon: parseFloat(lon),
                        format: 'json'
                    }
                });
    
                return res.json({
                    data: reverseResponse.data
                });
            }
    
            // iskanje po imenu znamenitosti
            if (attractionName) {
                const endpoint = autocomplete === 'true' ? 'https://us1.locationiq.com/v1/autocomplete.php' : 'https://us1.locationiq.com/v1/search.php';

                const forwardResponse = await axios.get(endpoint, {
                    params: {
                        key: apiKey,
                        q: attractionName,
                        countrycodes: 'si', // samo slovenija
                        format: 'json'
                    }
                });
    
                return res.json({
                    data: forwardResponse.data
                });
            }
            return res.status(400).json({ message: 'Missing parameters: please provide either attractionName or lat/lon' });
    
        }
        catch (error) {
            console.error(error);
    
            return res.status(500).json({
                message: 'Error fetching data from LocationIQ',
                error: error.message
            });
        }
    },
    
    retrieve: async function (req, res) {
        const { num, page } = req.query;

        try {
            const endpoint = "https://api.kamzavikend.si/public/search"

            const forwardResponse = await axios.get(endpoint, {
                headers: {
                    Accept: 'application/json',
                },
                params: {
                    'page[size]': num,
                    'page[number]': page 
                }
            });

            return res.json({
                data: forwardResponse.data
            });
        }catch (error) {
            console.error(error);

            return res.status(500).json({
                message: 'Error fetching data from kamzavikend.si',
                error: error.message
            });
        }
    }
};