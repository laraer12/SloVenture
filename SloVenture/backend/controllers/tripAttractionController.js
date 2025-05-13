var TripattractionModel = require('../models/tripAttractionModel.js');

/**
 * tripAttractionController.js
 *
 * @description :: Server-side logic for managing tripAttractions.
 */
module.exports = {

    /**
     * tripAttractionController.list()
     */
    list: function (req, res) {
        TripattractionModel.find(function (err, tripAttractions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting tripAttraction.',
                    error: err
                });
            }

            return res.json(tripAttractions);
        });
    },

    /**
     * tripAttractionController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        TripattractionModel.findOne({_id: id}, function (err, tripAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting tripAttraction.',
                    error: err
                });
            }

            if (!tripAttraction) {
                return res.status(404).json({
                    message: 'No such tripAttraction'
                });
            }

            return res.json(tripAttraction);
        });
    },

    /**
     * tripAttractionController.create()
     */
    create: function (req, res) {
        var tripAttraction = new TripattractionModel({
			tripId : req.body.tripId,
			attractionId : req.body.attractionId,
			order : req.body.order,
			description : req.body.description,
			plannedVisitTime : req.body.plannedVisitTime
        });

        tripAttraction.save(function (err, tripAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating tripAttraction',
                    error: err
                });
            }

            return res.status(201).json(tripAttraction);
        });
    },

    /**
     * tripAttractionController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        TripattractionModel.findOne({_id: id}, function (err, tripAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting tripAttraction',
                    error: err
                });
            }

            if (!tripAttraction) {
                return res.status(404).json({
                    message: 'No such tripAttraction'
                });
            }

            tripAttraction.tripId = req.body.tripId ? req.body.tripId : tripAttraction.tripId;
			tripAttraction.attractionId = req.body.attractionId ? req.body.attractionId : tripAttraction.attractionId;
			tripAttraction.order = req.body.order ? req.body.order : tripAttraction.order;
			tripAttraction.description = req.body.description ? req.body.description : tripAttraction.description;
			tripAttraction.plannedVisitTime = req.body.plannedVisitTime ? req.body.plannedVisitTime : tripAttraction.plannedVisitTime;
			
            tripAttraction.save(function (err, tripAttraction) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating tripAttraction.',
                        error: err
                    });
                }

                return res.json(tripAttraction);
            });
        });
    },

    /**
     * tripAttractionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        TripattractionModel.findByIdAndRemove(id, function (err, tripAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the tripAttraction.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
