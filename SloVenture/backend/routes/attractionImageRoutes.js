var express = require('express');
var router = express.Router();
var attractionImageController = require('../controllers/attractionImageController.js');
const jwt = require('jsonwebtoken');

function requiresLogin(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader) {
        return res.status(401).json({ message: 'Niste prijavljeni' });
    }

    const token = authHeader.split(' ')[1]; // Bearer <token>

    jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
        if (err) {
            return res.status(401).json({ message: 'Neveljaven token' });
      }
        req.user = decoded; 
        next();
    });
}

// za posodobitev profilne slike
var multer = require('multer');
var upload = multer({ dest: 'public/images/' });

router.get('/', attractionImageController.list);
router.get('/:id', attractionImageController.show);
router.post('/',  attractionImageController.create);
router.put('/:id', attractionImageController.update);
router.delete('/:id', requiresLogin, attractionImageController.remove);

router.post('/upload-attraction-image', requiresLogin, upload.single('source'), attractionImageController.uploadAttractionImage); // da lahko uporabnik doda svojo sliko znamenitosti

module.exports = router;