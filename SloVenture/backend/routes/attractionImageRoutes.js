var express = require('express');
var router = express.Router();
var attractionImageController = require('../controllers/attractionImageController.js');

function requiresLogin(req, res, next) {
    if (req.session && req.session.userId)
        return next();
    
    else {
        var err = new Error("Morate biti prijavljeni");
        err.status = 401;
        return next(err);
    }
}

// za posodobitev profilne slike
var multer = require('multer');
var upload = multer({ dest: 'public/images/' });

router.get('/', attractionImageController.list);
router.get('/:id', attractionImageController.show);
router.post('/',  attractionImageController.create);
router.put('/:id', attractionImageController.update);
router.delete('/:id', attractionImageController.remove);

router.post('/upload-attraction-image', requiresLogin, upload.single('source'), attractionImageController.uploadAttractionImage); // da lahko uporabnik doda svojo sliko znamenitosti

module.exports = router;