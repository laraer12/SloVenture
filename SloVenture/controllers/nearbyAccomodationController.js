var NearbyaccomodationModel = require('../models/nearbyAccomodationModel.js');

/**
 * nearbyAccomodationController.js
 *
 * @description :: Server-side logic for managing nearbyAccomodations.
 */
module.exports = {

    /**
     * nearbyAccomodationController.list()
     */
    list: function (req, res) {
        NearbyaccomodationModel.find(function (err, nearbyAccomodations) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAccomodation.',
                    error: err
                });
            }

            return res.json(nearbyAccomodations);
        });
    },

    /**
     * nearbyAccomodationController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        NearbyaccomodationModel.findOne({_id: id}, function (err, nearbyAccomodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAccomodation.',
                    error: err
                });
            }

            if (!nearbyAccomodation) {
                return res.status(404).json({
                    message: 'No such nearbyAccomodation'
                });
            }

            return res.json(nearbyAccomodation);
        });
    },

    /**
     * nearbyAccomodationController.create()
     */
    create: function (req, res) {
        var nearbyAccomodation = new NearbyaccomodationModel({
			name : req.body.name,
			linkToBooking : req.body.linkToBooking,
			attractionId : req.body.attractionId,
			distance : req.body.distance
        });

        nearbyAccomodation.save(function (err, nearbyAccomodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating nearbyAccomodation',
                    error: err
                });
            }

            return res.status(201).json(nearbyAccomodation);
        });
    },

    /**
     * nearbyAccomodationController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        NearbyaccomodationModel.findOne({_id: id}, function (err, nearbyAccomodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAccomodation',
                    error: err
                });
            }

            if (!nearbyAccomodation) {
                return res.status(404).json({
                    message: 'No such nearbyAccomodation'
                });
            }

            nearbyAccomodation.name = req.body.name ? req.body.name : nearbyAccomodation.name;
			nearbyAccomodation.linkToBooking = req.body.linkToBooking ? req.body.linkToBooking : nearbyAccomodation.linkToBooking;
			nearbyAccomodation.attractionId = req.body.attractionId ? req.body.attractionId : nearbyAccomodation.attractionId;
			nearbyAccomodation.distance = req.body.distance ? req.body.distance : nearbyAccomodation.distance;
			
            nearbyAccomodation.save(function (err, nearbyAccomodation) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating nearbyAccomodation.',
                        error: err
                    });
                }

                return res.json(nearbyAccomodation);
            });
        });
    },

    /**
     * nearbyAccomodationController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        NearbyaccomodationModel.findByIdAndRemove(id, function (err, nearbyAccomodation) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the nearbyAccomodation.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
