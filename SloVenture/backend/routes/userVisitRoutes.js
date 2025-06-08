var express = require('express');
var router = express.Router();
var userVisitController = require('../controllers/userVisitController.js');
const jwt = require('jsonwebtoken');

function requiresLogin(req, res, next) {
    if (process.env.NODE_ENV === 'development' && req.body && req.body.isFakeData)
        return next();

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
router.get('/visits-by-attraction', userVisitController.visitsByAttraction); // pridobim vse obiske glede na id znamenitosti
router.get('/', userVisitController.list);
router.get('/:id', userVisitController.show);
router.post('/', requiresLogin, userVisitController.create);
router.put('/:id', userVisitController.update);
router.delete('/:id', userVisitController.remove);


router.get('/user/:userId', userVisitController.findByUserId); // pridobim vse obiske glede na uporabnikov id



module.exports = router;