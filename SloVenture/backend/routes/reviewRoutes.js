var express = require('express');
var router = express.Router();
var reviewController = require('../controllers/reviewController.js');

// funkcija ki dovoli določeno akcijo izvesti samo prijavljenim uporabnikom
function requiresLogin(req, res, next) {
    if (req.session && req.session.userId)
        return next();
    
    else {
        var err = new Error("Za komentiranje moraš biti prijavljen");
        err.status = 401;
        return next(err);
    }
}

router.get('/', reviewController.list);
router.get('/:id', reviewController.show);
router.post('/', requiresLogin, reviewController.create); // oddaja ocene
router.put('/:id', requiresLogin, reviewController.update);
router.delete('/:id', requiresLogin, reviewController.remove);

router.get('/averages/:attractionId', reviewController.getAveragesByAttraction); // povprečje ocen

module.exports = router;