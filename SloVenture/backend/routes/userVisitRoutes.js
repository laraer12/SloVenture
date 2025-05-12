var express = require('express');
var router = express.Router();
var userVisitController = require('../controllers/userVisitController.js');

/*
 * GET
 */
router.get('/', userVisitController.list);

/*
 * GET
 */
router.get('/:id', userVisitController.show);

/*
 * POST
 */
router.post('/', userVisitController.create);

/*
 * PUT
 */
router.put('/:id', userVisitController.update);

/*
 * DELETE
 */
router.delete('/:id', userVisitController.remove);

module.exports = router;
