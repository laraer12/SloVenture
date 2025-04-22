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
    }
};
