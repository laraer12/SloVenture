var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');

router.get('/', userController.list);
router.post('/', userController.create);
router.get('/:id', userController.show);
router.put('/:id', userController.update);
router.delete('/:id', userController.remove);

module.exports = router;