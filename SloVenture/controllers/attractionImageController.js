var AttractionimageModel = require('../models/attractionImageModel.js');

/**
 * attractionImageController.js
 *
 * @description :: Server-side logic for managing attractionImages.
 */
module.exports = {

    /**
     * attractionImageController.list()
     */
    list: function (req, res) {
        AttractionimageModel.find(function (err, attractionImages) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attractionImage.',
                    error: err
                });
            }

            return res.json(attractionImages);
        });
    },

    /**
     * attractionImageController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        AttractionimageModel.findOne({_id: id}, function (err, attractionImage) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attractionImage.',
                    error: err
                });
            }

            if (!attractionImage) {
                return res.status(404).json({
                    message: 'No such attractionImage'
                });
            }

            return res.json(attractionImage);
        });
    },

    /**
     * attractionImageController.create()
     */
    create: function (req, res) {
        var attractionImage = new AttractionimageModel({
			attractionId : req.body.attractionId,
			url : req.body.url,
			source : req.body.source,
			uploadedBy : req.body.uploadedBy,
			createdAt : req.body.createdAt
        });

        attractionImage.save(function (err, attractionImage) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating attractionImage',
                    error: err
                });
            }

            return res.status(201).json(attractionImage);
        });
    },

    /**
     * attractionImageController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        AttractionimageModel.findOne({_id: id}, function (err, attractionImage) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attractionImage',
                    error: err
                });
            }

            if (!attractionImage) {
                return res.status(404).json({
                    message: 'No such attractionImage'
                });
            }

            attractionImage.attractionId = req.body.attractionId ? req.body.attractionId : attractionImage.attractionId;
			attractionImage.url = req.body.url ? req.body.url : attractionImage.url;
			attractionImage.source = req.body.source ? req.body.source : attractionImage.source;
			attractionImage.uploadedBy = req.body.uploadedBy ? req.body.uploadedBy : attractionImage.uploadedBy;
			attractionImage.createdAt = req.body.createdAt ? req.body.createdAt : attractionImage.createdAt;
			
            attractionImage.save(function (err, attractionImage) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating attractionImage.',
                        error: err
                    });
                }

                return res.json(attractionImage);
            });
        });
    },

    /**
     * attractionImageController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        AttractionimageModel.findByIdAndRemove(id, function (err, attractionImage) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the attractionImage.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
