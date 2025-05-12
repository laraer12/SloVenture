var express = require('express');
var router = express.Router();
var tripAttractionController = require('../controllers/tripAttractionController.js');

/*
 * GET
 */
router.get('/', tripAttractionController.list);

/*
 * GET
 */
router.get('/:id', tripAttractionController.show);

/*
 * POST
 */
router.post('/', tripAttractionController.create);

/*
 * PUT
 */
router.put('/:id', tripAttractionController.update);

/*
 * DELETE
 */
router.delete('/:id', tripAttractionController.remove);

module.exports = router;
