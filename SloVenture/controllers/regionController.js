var RegionModel = require('../models/regionModel.js');

/**
 * regionController.js
 *
 * @description :: Server-side logic for managing regions.
 */
module.exports = {

    /**
     * regionController.list()
     */
    list: function (req, res) {
        RegionModel.find(function (err, regions) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting region.',
                    error: err
                });
            }

            return res.json(regions);
        });
    },

    /**
     * regionController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        RegionModel.findOne({_id: id}, function (err, region) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting region.',
                    error: err
                });
            }

            if (!region) {
                return res.status(404).json({
                    message: 'No such region'
                });
            }

            return res.json(region);
        });
    },

    /**
     * regionController.create()
     */
    create: function (req, res) {
        var region = new RegionModel({
			name : req.body.name,
			location : req.body.location
        });

        region.save(function (err, region) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating region',
                    error: err
                });
            }

            return res.status(201).json(region);
        });
    },

    /**
     * regionController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        RegionModel.findOne({_id: id}, function (err, region) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting region',
                    error: err
                });
            }

            if (!region) {
                return res.status(404).json({
                    message: 'No such region'
                });
            }

            region.name = req.body.name ? req.body.name : region.name;
			region.location = req.body.location ? req.body.location : region.location;
			
            region.save(function (err, region) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating region.',
                        error: err
                    });
                }

                return res.json(region);
            });
        });
    },

    /**
     * regionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        RegionModel.findByIdAndRemove(id, function (err, region) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the region.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
