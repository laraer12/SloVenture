var express = require('express');
var router = express.Router();
var tripAttractionController = require('../controllers/tripAttractionController.js');

function requiresLogin(req, res, next) {
    if (req.session && req.session.userId)
        return next();
    
    else {
        var err = new Error("Morate biti prijavljeni");
        err.status = 401;
        return next(err);
    }
}

router.get('/', tripAttractionController.list);
router.get('/:id', tripAttractionController.show);
router.post('/', requiresLogin, tripAttractionController.create); // dodajanje izleta
router.put('/:id', tripAttractionController.update);
router.delete('/:id', tripAttractionController.remove);

module.exports = router;