var express = require('express');
var router = express.Router();
var userVisitController = require('../controllers/userVisitController.js');

function requiresLogin(req, res, next) {
    if (req.session && req.session.userId)
        return next();
    
    else {
        var err = new Error("Za objavo slik moraš biti prijavljen");
        err.status = 401;
        return next(err);
    }
}

router.get('/', userVisitController.list);
router.get('/:id', userVisitController.show);
router.post('/', requiresLogin, userVisitController.create);
router.put('/:id', userVisitController.update);
router.delete('/:id', userVisitController.remove);

router.get('/user/:userId', userVisitController.findByUserId); // pridobim vse obiske glede na uporabnikov id

module.exports = router;