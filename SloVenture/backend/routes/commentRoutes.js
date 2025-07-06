var express = require('express');
var router = express.Router();
var commentController = require('../controllers/commentController.js');

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

router.get('/attraction/:attractionId', commentController.listByAttraction); // komentarji pri določeni znamenitosti
router.post('/attraction/:attractionId', requiresLogin, commentController.create); // dodajanje komentarja

router.get('/', commentController.list); // vsi komentarji
router.get('/:id', commentController.show); // določen komentar
router.put('/:id', commentController.update); // posodobitev komentarja
router.delete('/:id', requiresLogin, commentController.remove); // brisanje komentarja

module.exports = router;