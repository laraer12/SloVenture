var express = require('express');
var router = express.Router();
var tripController = require('../controllers/tripController.js');

function requiresLogin(req, res, next) {
    if (req.session && req.session.userId)
        return next();
    
    else {
        var err = new Error("Morate biti prijavljeni");
        err.status = 401;
        return next(err);
    }
}

router.get('/user/:userId', tripController.tripsByUser); // potovanja glede na uporabnikov id

router.get('/', tripController.list);
router.get('/:id', tripController.show);
router.post('/', requiresLogin, tripController.create);
router.put('/:id', tripController.update);
router.delete('/:id', tripController.remove);

module.exports = router;