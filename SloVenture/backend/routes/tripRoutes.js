var express = require('express');
var router = express.Router();
var tripController = require('../controllers/tripController.js');

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
        req.user = decoded; // npr. uporabnikovi podatki
        next();
    });
}

router.get('/user/:userId', tripController.tripsByUser); // potovanja glede na uporabnikov id

router.get('/', requiresLogin, tripController.list);
router.get('/:id', tripController.show);
router.post('/', requiresLogin, tripController.create);
router.put('/:id', tripController.update);
router.delete('/:id', tripController.remove);

module.exports = router;