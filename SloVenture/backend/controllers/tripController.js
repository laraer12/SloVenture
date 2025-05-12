var TripModel = require('../models/tripModel.js');

/**
 * tripController.js
 *
 * @description :: Server-side logic for managing trips.
 */
module.exports = {

    /**
     * tripController.list()
     */
    list: function (req, res) {
        TripModel.find(function (err, trips) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting trip.',
                    error: err
                });
            }

            return res.json(trips);
        });
    },

    /**
     * tripController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        TripModel.findOne({_id: id}, function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting trip.',
                    error: err
                });
            }

            if (!trip) {
                return res.status(404).json({
                    message: 'No such trip'
                });
            }

            return res.json(trip);
        });
    },

    /**
     * tripController.create()
     */
    create: function (req, res) {
        var trip = new TripModel({
			userId : req.body.userId,
			name : req.body.name,
			description : req.body.description,
			startDate : req.body.startDate,
			endDate : req.body.endDate,
			isPublic : req.body.isPublic,
			createdAt : req.body.createdAt
        });

        trip.save(function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating trip',
                    error: err
                });
            }

            return res.status(201).json(trip);
        });
    },

    /**
     * tripController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        TripModel.findOne({_id: id}, function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting trip',
                    error: err
                });
            }

            if (!trip) {
                return res.status(404).json({
                    message: 'No such trip'
                });
            }

            trip.userId = req.body.userId ? req.body.userId : trip.userId;
			trip.name = req.body.name ? req.body.name : trip.name;
			trip.description = req.body.description ? req.body.description : trip.description;
			trip.startDate = req.body.startDate ? req.body.startDate : trip.startDate;
			trip.endDate = req.body.endDate ? req.body.endDate : trip.endDate;
			trip.isPublic = req.body.isPublic ? req.body.isPublic : trip.isPublic;
			trip.createdAt = req.body.createdAt ? req.body.createdAt : trip.createdAt;
			
            trip.save(function (err, trip) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating trip.',
                        error: err
                    });
                }

                return res.json(trip);
            });
        });
    },

    /**
     * tripController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        TripModel.findByIdAndRemove(id, function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the trip.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};

// trip attraction