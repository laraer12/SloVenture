var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');

router.get('/', userController.list);
router.post('/', userController.create); // registracija
router.post('/login', userController.login); // prijava
router.get('/profile', userController.profile); // prikaz profila
router.get('/logout', userController.logout); // odjava
router.get('/:id', userController.show);
router.put('/:id', userController.update);
router.delete('/:id', userController.remove);

module.exports = router;