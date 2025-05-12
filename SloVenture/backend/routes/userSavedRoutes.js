var express = require('express');
var router = express.Router();
var userSavedController = require('../controllers/userSavedController.js');

/*
 * GET
 */
router.get('/', userSavedController.list);

/*
 * GET
 */
router.get('/:id', userSavedController.show);

/*
 * POST
 */
router.post('/', userSavedController.create);

/*
 * PUT
 */
router.put('/:id', userSavedController.update);

/*
 * DELETE
 */
router.delete('/:id', userSavedController.remove);

module.exports = router;
