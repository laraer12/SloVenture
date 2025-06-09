var express = require('express');
var router = express.Router();
var tripAttractionController = require('../controllers/tripAttractionController.js');

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

router.get('/', tripAttractionController.list);
router.get('/:id', tripAttractionController.show);
router.post('/', requiresLogin, tripAttractionController.create); // dodajanje izleta
router.put('/:id', tripAttractionController.update);
router.delete('/:id', tripAttractionController.remove);

module.exports = router;