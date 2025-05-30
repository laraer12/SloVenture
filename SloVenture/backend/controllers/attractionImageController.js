var fs = require('fs');
var path = require('path');

var AttractionImageModel = require('../models/attractionImageModel.js');
var UserModel = require('../models/userModel');

var axios = require('axios');

var multer = require('multer'); // za objavo datotek

var storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'public/images/'); // sem shranim slike
    },
    filename: function (req, file, cb) {
        cb(null, Date.now() + '-' + file.originalname); // ime slike vsebuje časovni žig in originalno ime
    }
});

var upload = multer({ storage: storage }); // inicializacija multer-ja

/**
 * attractionImageController.js
 *
 * @description :: Server-side logic for managing attractionImage.
 */
module.exports = {

    /**
     * attractionImageController.list()
     */
    list: function (req, res) {
        AttractionImageModel.find(function (err, attractionImages) {
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

        AttractionImageModel.findOne({_id: id}, function (err, attractionImage) {
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
        var attractionImage = new AttractionImageModel({
            url : req.body.url,
            attractionId : req.body.attractionId,
            source : req.body.source,
            createdAt : Date.now(),
            uploadedBy : req.body.uploadedBy
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

        AttractionImageModel.findOne({_id: id}, function (err, attractionImage) {
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
        var id = req.session?.id;

        if (!id)
            return res.status(401).json({ message: 'User not logged in' });

        UserModel.findById(id, function (err, user) {
            if (err || !user?.isAdmin)
                return res.status(403).json({ message: 'Only the admin can delete images' });

            AttractionImageModel.findById(req.params.id, function (err, image) {
                if (err || !image)
                    return res.status(404).json({ message: 'Image not found' });

                const imagePath = path.join(__dirname, '..', 'public', image.url); // pot, kamor se shranijo slike, ki jih uporabnik naloži preko vmesnika

                // izbrišem sliko iz mape
                fs.unlink(imagePath, function (err) {
                    if (err && err.code !== 'ENOENT')
                        return res.status(500).json({ message: 'Error deleting image', error: err });
                    
                    // izbrišem sliko iz baze
                    AttractionImageModel.findByIdAndDelete(req.params.id, function (err) {
                        if (err) 
                            return res.status(500).json({ message: 'Error deleting from database', error: err });

                        return res.sendStatus(204);
                    });
                });
            });
        });
    },

    /**
     * attractionImageController.uploadAttractionImage()
     * uporabnik lahko doda svojo sliko znamenitosti
     */
    uploadAttractionImage: function(req, res) {
        if (!req.file)
            return res.status(400).json({ message: "File was not uploaded" });

        const imageUrl = `/images/${req.file.filename}`; // takšen bo url slike
        
        // potrebni podatki
        const attractionImage = new AttractionImageModel({
            attractionId: req.body.attractionId,
            url: imageUrl,
            source: req.file.originalname,
            uploadedBy: req.body.uploadedBy,
            createdAt: new Date()
        });
        
        // shranim
        attractionImage.save(function(err, savedImage) {
            if (err) {
                return res.status(500).json({
                message: 'Napaka pri shranjevanju slike',
                error: err
                });
            }
            return res.status(201).json(savedImage);
        });
    }
};