var express = require('express');
var router = express.Router();
var reviewController = require('../controllers/reviewController.js');
const jwt = require('jsonwebtoken');
// funkcija ki dovoli določeno akcijo izvesti samo prijavljenim uporabnikom
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

router.get('/ratings-by-attraction', reviewController.reviewsByAttraction); // pridobim ocene glede na id znamenitosti
router.get('/', reviewController.list);
router.get('/:id', reviewController.show);
router.post('/', requiresLogin, reviewController.create); // oddaja ocene
router.put('/:id', requiresLogin, reviewController.update);
router.delete('/:id', requiresLogin, reviewController.remove);

router.get('/averages/:attractionId', reviewController.getAveragesByAttraction); // povprečje ocen

module.exports = router;