var express = require('express');
var router = express.Router();
var commentController = require('../controllers/commentController.js');

function requiresLogin(req, res, next) {
    if (req.session && req.session.userId)
        return next();
    
    else {
        var err = new Error("Morate biti prijavljeni");
        err.status = 401;
        return next(err);
    }
}

router.get('/attraction/:attractionId', commentController.listByAttraction); // komentarji pri določeni znamenitosti
router.post('/attraction/:attractionId', requiresLogin, commentController.create); // dodajanje komentarja

router.get('/', commentController.list); // vsi komentarji
router.get('/:id', commentController.show); // določen komentar
router.put('/:id', commentController.update); // posodobitev komentarja
router.delete('/:id', commentController.remove); // brisanje komentarja

module.exports = router;