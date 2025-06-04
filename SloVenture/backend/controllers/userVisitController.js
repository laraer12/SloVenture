var UservisitModel = require('../models/userVisitModel.js');
var AttractionImageModel = require('../models/attractionImageModel.js');

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
        const { userId, attractionId, visitDate, isFakeData } = req.body;

        // preverim, ali zapis že obstaja
        UservisitModel.findOne({ userId, attractionId, visitDate }, function (err, existingVisit) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating userVisit',
                    error: err
                });
            }
            if (existingVisit)
                return res.status(409).json({ message: 'Visit already exists' });

            // če pa ne obstaja, ustvarim nov zapis
            const userVisit = new UservisitModel({
                userId,
                attractionId,
                visitDate,
                isFakeData: isFakeData || false // privzeto je isFakeData false, če ni podano
            });

            userVisit.save(function (err, savedVisit) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error saving visit',
                        error: err
                    });
                }
                return res.status(201).json(savedVisit);
            });
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
            userVisit.isFakeData = req.body.isFakeData !== undefined ? req.body.isFakeData : userVisit.isFakeData;
			
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
    },

    /**
     * userVisitController.findByUserId()
     */
    findByUserId: async function (req, res) {
        const userId = req.params.userId;

        try {
            // dobim vse obiske uporabnika z znamenitostmi
            const visits = await UservisitModel.find({ userId })
                .populate('attractionId')
                .exec();

            // za vsako zanmenitost poiščem slike in jih dodam
            const visitsWithImages = await Promise.all(visits.map(async (visit) => {
                const attractionId = visit.attractionId?._id;
                let images = [];

                if (attractionId)
                    images = await AttractionImageModel.find({ attractionId }).select('url -_id');

                return {
                    ...visit.toObject(),
                    attractionImages: images.map(img => img.url)
                };
            }));

            return res.json(visitsWithImages);
        }
        catch (err) {
            return res.status(500).json({
                message: 'Error getting user visits with images',
                error: err
            });
        }
    }
};