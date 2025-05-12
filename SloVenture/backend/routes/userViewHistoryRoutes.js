var express = require('express');
var router = express.Router();
var userViewHistoryController = require('../controllers/userViewHistoryController.js');

/*
 * GET
 */
router.get('/', userViewHistoryController.list);

/*
 * GET
 */
router.get('/:id', userViewHistoryController.show);

/*
 * POST
 */
router.post('/', userViewHistoryController.create);

/*
 * PUT
 */
router.put('/:id', userViewHistoryController.update);

/*
 * DELETE
 */
router.delete('/:id', userViewHistoryController.remove);

module.exports = router;
