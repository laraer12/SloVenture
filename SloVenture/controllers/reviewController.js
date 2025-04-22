var ReviewModel = require('../models/reviewModel.js');

/**
 * reviewController.js
 *
 * @description :: Server-side logic for managing reviews.
 */
module.exports = {

    /**
     * reviewController.list()
     */
    list: function (req, res) {
        ReviewModel.find(function (err, reviews) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting review.',
                    error: err
                });
            }

            return res.json(reviews);
        });
    },

    /**
     * reviewController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        ReviewModel.findOne({_id: id}, function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting review.',
                    error: err
                });
            }

            if (!review) {
                return res.status(404).json({
                    message: 'No such review'
                });
            }

            return res.json(review);
        });
    },

    /**
     * reviewController.create()
     */
    create: function (req, res) {
        var review = new ReviewModel({
			userId : req.body.userId,
			attractionId : req.body.attractionId,
			rating : req.body.rating,
			ratingFamilyFriendly : req.body.ratingFamilyFriendly,
			ratingElderlyFriendly : req.body.ratingElderlyFriendly,
			ratingAccesible : req.body.ratingAccesible,
			createdAt : req.body.createdAt
        });

        review.save(function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating review',
                    error: err
                });
            }

            return res.status(201).json(review);
        });
    },

    /**
     * reviewController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        ReviewModel.findOne({_id: id}, function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting review',
                    error: err
                });
            }

            if (!review) {
                return res.status(404).json({
                    message: 'No such review'
                });
            }

            review.userId = req.body.userId ? req.body.userId : review.userId;
			review.attractionId = req.body.attractionId ? req.body.attractionId : review.attractionId;
			review.rating = req.body.rating ? req.body.rating : review.rating;
			review.ratingFamilyFriendly = req.body.ratingFamilyFriendly ? req.body.ratingFamilyFriendly : review.ratingFamilyFriendly;
			review.ratingElderlyFriendly = req.body.ratingElderlyFriendly ? req.body.ratingElderlyFriendly : review.ratingElderlyFriendly;
			review.ratingAccesible = req.body.ratingAccesible ? req.body.ratingAccesible : review.ratingAccesible;
			review.createdAt = req.body.createdAt ? req.body.createdAt : review.createdAt;
			
            review.save(function (err, review) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating review.',
                        error: err
                    });
                }

                return res.json(review);
            });
        });
    },

    /**
     * reviewController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        ReviewModel.findByIdAndRemove(id, function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the review.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
