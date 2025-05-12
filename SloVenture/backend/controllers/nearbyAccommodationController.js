var NearbyAccommodationModel = require('../models/nearbyAccommodationModel.js');

/**
 * nearbyAccommodationController.js
 *
 * @description :: Server-side logic for managing nearbyAccommodations.
 */
module.exports = {

    /**
     * nearbyAccommodationController.list()
     */
    list: function (req, res) {
        NearbyAccommodationModel.find(function (err, nearbyAccommodations) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAccommodation.',
                    error: err
                });
            }

            return res.json(nearbyAccommodations);
        });
    },

    /**
     * nearbyAccommodationController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        NearbyAccommodationModel.findOne({_id: id}, function (err, nearbyAccommodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAccommodation.',
                    error: err
                });
            }

            if (!nearbyAccommodation) {
                return res.status(404).json({
                    message: 'No such nearbyAccommodation'
                });
            }

            return res.json(nearbyAccommodation);
        });
    },

    /**
     * nearbyAccommodationController.create()
     */
    create: function (req, res) {
        var nearbyAccommodation = new NearbyAccommodationModel({
			name : req.body.name,
			linkToBooking : req.body.linkToBooking,
			attractionId : req.body.attractionId,
			distance : req.body.distance
        });

        nearbyAccommodation.save(function (err, nearbyAccommodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating nearbyAccommodation',
                    error: err
                });
            }

            return res.status(201).json(nearbyAccommodation);
        });
    },

    /**
     * nearbyAccommodationController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        NearbyAccommodationModel.findOne({_id: id}, function (err, nearbyAccommodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAccommodation',
                    error: err
                });
            }

            if (!nearbyAccommodation) {
                return res.status(404).json({
                    message: 'No such nearbyAccommodation'
                });
            }

            nearbyAccommodation.name = req.body.name ? req.body.name : nearbyAccommodation.name;
			nearbyAccommodation.linkToBooking = req.body.linkToBooking ? req.body.linkToBooking : nearbyAccommodation.linkToBooking;
			nearbyAccommodation.attractionId = req.body.attractionId ? req.body.attractionId : nearbyAccommodation.attractionId;
			nearbyAccommodation.distance = req.body.distance ? req.body.distance : nearbyAccommodation.distance;
			
            nearbyAccommodation.save(function (err, nearbyAccommodation) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating nearbyAccommodation.',
                        error: err
                    });
                }

                return res.json(nearbyAccommodation);
            });
        });
    },

    /**
     * nearbyAccommodationController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        NearbyAccommodationModel.findByIdAndRemove(id, function (err, nearbyAccommodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the nearbyAccommodation.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
