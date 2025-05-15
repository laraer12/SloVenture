var UservisitModel = require('../models/userVisitModel.js');

/**
 * userVisitController.js
 *
 * @description :: Server-side logic for managing userVisits.
 */
module.exports = {

    /**
     * userVisitController.list()
     */
    list: function (req, res) {
        UservisitModel.find(function (err, userVisits) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userVisit.',
                    error: err
                });
            }

            return res.json(userVisits);
        });
    },

    /**
     * userVisitController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        UservisitModel.findOne({_id: id}, function (err, userVisit) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userVisit.',
                    error: err
                });
            }

            if (!userVisit) {
                return res.status(404).json({
                    message: 'No such userVisit'
                });
            }

            return res.json(userVisit);
        });
    },

    /**
     * userVisitController.create()
     */
    create: function (req, res) {
        var userVisit = new UservisitModel({
			userId : req.body.userId,
			attractionId : req.body.attractionId,
			visitDate : req.body.visitDate
        });

        userVisit.save(function (err, userVisit) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating userVisit',
                    error: err
                });
            }

            return res.status(201).json(userVisit);
        });
    },

    /**
     * userVisitController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        UservisitModel.findOne({_id: id}, function (err, userVisit) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userVisit',
                    error: err
                });
            }

            if (!userVisit) {
                return res.status(404).json({
                    message: 'No such userVisit'
                });
            }

            userVisit.userId = req.body.userId ? req.body.userId : userVisit.userId;
			userVisit.attractionId = req.body.attractionId ? req.body.attractionId : userVisit.attractionId;
			userVisit.visitDate = req.body.visitDate ? req.body.visitDate : userVisit.visitDate;
			
            userVisit.save(function (err, userVisit) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating userVisit.',
                        error: err
                    });
                }

                return res.json(userVisit);
            });
        });
    },

    /**
     * userVisitController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        UservisitModel.findByIdAndRemove(id, function (err, userVisit) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the userVisit.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};