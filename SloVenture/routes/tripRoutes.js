var express = require('express');
var router = express.Router();
var tripController = require('../controllers/tripController.js');

/*
 * GET
 */
router.get('/', tripController.list);

/*
 * GET
 */
router.get('/:id', tripController.show);

/*
 * POST
 */
router.post('/', tripController.create);

/*
 * PUT
 */
router.put('/:id', tripController.update);

/*
 * DELETE
 */
router.delete('/:id', tripController.remove);

module.exports = router;
