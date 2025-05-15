var UserviewhistoryModel = require('../models/userViewHistoryModel.js');

/**
 * userViewHistoryController.js
 *
 * @description :: Server-side logic for managing userViewHistorys.
 */
module.exports = {

    /**
     * userViewHistoryController.list()
     */
    list: function (req, res) {
        UserviewhistoryModel.find(function (err, userViewHistorys) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userViewHistory.',
                    error: err
                });
            }

            return res.json(userViewHistorys);
        });
    },

    /**
     * userViewHistoryController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        UserviewhistoryModel.findOne({_id: id}, function (err, userViewHistory) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userViewHistory.',
                    error: err
                });
            }

            if (!userViewHistory) {
                return res.status(404).json({
                    message: 'No such userViewHistory'
                });
            }

            return res.json(userViewHistory);
        });
    },

    /**
     * userViewHistoryController.create()
     */
    create: function (req, res) {
        var userViewHistory = new UserviewhistoryModel({
			userId : req.body.userId,
			attractionId : req.body.attractionId,
			viewedAt : req.body.viewedAt
        });

        userViewHistory.save(function (err, userViewHistory) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating userViewHistory',
                    error: err
                });
            }

            return res.status(201).json(userViewHistory);
        });
    },

    /**
     * userViewHistoryController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        UserviewhistoryModel.findOne({_id: id}, function (err, userViewHistory) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userViewHistory',
                    error: err
                });
            }

            if (!userViewHistory) {
                return res.status(404).json({
                    message: 'No such userViewHistory'
                });
            }

            userViewHistory.userId = req.body.userId ? req.body.userId : userViewHistory.userId;
			userViewHistory.attractionId = req.body.attractionId ? req.body.attractionId : userViewHistory.attractionId;
			userViewHistory.viewedAt = req.body.viewedAt ? req.body.viewedAt : userViewHistory.viewedAt;
			
            userViewHistory.save(function (err, userViewHistory) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating userViewHistory.',
                        error: err
                    });
                }

                return res.json(userViewHistory);
            });
        });
    },

    /**
     * userViewHistoryController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        UserviewhistoryModel.findByIdAndRemove(id, function (err, userViewHistory) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the userViewHistory.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};