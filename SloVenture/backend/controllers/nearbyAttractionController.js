var NearbyattractionModel = require('../models/nearbyAttractionModel.js');

/**
 * nearbyAttractionController.js
 *
 * @description :: Server-side logic for managing nearbyAttractions.
 */
module.exports = {

    /**
     * nearbyAttractionController.list()
     */
    list: function (req, res) {
        NearbyattractionModel.find(function (err, nearbyAttractions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAttraction.',
                    error: err
                });
            }

            return res.json(nearbyAttractions);
        });
    },

    /**
     * nearbyAttractionController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        NearbyattractionModel.findOne({_id: id}, function (err, nearbyAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAttraction.',
                    error: err
                });
            }

            if (!nearbyAttraction) {
                return res.status(404).json({
                    message: 'No such nearbyAttraction'
                });
            }

            return res.json(nearbyAttraction);
        });
    },

    /**
     * nearbyAttractionController.create()
     */
    create: function (req, res) {
        var nearbyAttraction = new NearbyattractionModel({
			attractionId : req.body.attractionId,
			nearbyAttractionId : req.body.nearbyAttractionId,
			distance : req.body.distance
        });

        nearbyAttraction.save(function (err, nearbyAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating nearbyAttraction',
                    error: err
                });
            }

            return res.status(201).json(nearbyAttraction);
        });
    },

    /**
     * nearbyAttractionController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        NearbyattractionModel.findOne({_id: id}, function (err, nearbyAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting nearbyAttraction',
                    error: err
                });
            }

            if (!nearbyAttraction) {
                return res.status(404).json({
                    message: 'No such nearbyAttraction'
                });
            }

            nearbyAttraction.attractionId = req.body.attractionId ? req.body.attractionId : nearbyAttraction.attractionId;
			nearbyAttraction.nearbyAttractionId = req.body.nearbyAttractionId ? req.body.nearbyAttractionId : nearbyAttraction.nearbyAttractionId;
			nearbyAttraction.distance = req.body.distance ? req.body.distance : nearbyAttraction.distance;
			
            nearbyAttraction.save(function (err, nearbyAttraction) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating nearbyAttraction.',
                        error: err
                    });
                }

                return res.json(nearbyAttraction);
            });
        });
    },

    /**
     * nearbyAttractionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        NearbyattractionModel.findByIdAndRemove(id, function (err, nearbyAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the nearbyAttraction.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
