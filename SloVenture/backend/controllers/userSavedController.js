var UsersavedModel = require('../models/userSavedModel.js');

/**
 * userSavedController.js
 *
 * @description :: Server-side logic for managing userSaveds.
 */
module.exports = {

    /**
     * userSavedController.list()
     */
    list: function (req, res) {
        UsersavedModel.find(function (err, userSaveds) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userSaved.',
                    error: err
                });
            }

            return res.json(userSaveds);
        });
    },

    /**
     * userSavedController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        UsersavedModel.findOne({_id: id}, function (err, userSaved) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userSaved.',
                    error: err
                });
            }

            if (!userSaved) {
                return res.status(404).json({
                    message: 'No such userSaved'
                });
            }

            return res.json(userSaved);
        });
    },

    /**
     * userSavedController.create()
     */
    create: function (req, res) {
        var userSaved = new UsersavedModel({
			userId : req.body.userId,
			attractionId : req.body.attractionId
        });

        userSaved.save(function (err, userSaved) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating userSaved',
                    error: err
                });
            }

            return res.status(201).json(userSaved);
        });
    },

    /**
     * userSavedController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        UsersavedModel.findOne({_id: id}, function (err, userSaved) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting userSaved',
                    error: err
                });
            }

            if (!userSaved) {
                return res.status(404).json({
                    message: 'No such userSaved'
                });
            }

            userSaved.userId = req.body.userId ? req.body.userId : userSaved.userId;
			userSaved.attractionId = req.body.attractionId ? req.body.attractionId : userSaved.attractionId;
			
            userSaved.save(function (err, userSaved) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating userSaved.',
                        error: err
                    });
                }

                return res.json(userSaved);
            });
        });
    },

    /**
     * userSavedController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        UsersavedModel.findByIdAndRemove(id, function (err, userSaved) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the userSaved.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};