var express = require('express');
var router = express.Router();
var nearbyAttractionController = require('../controllers/nearbyAttractionController.js');

/*
 * GET
 */
router.get('/', nearbyAttractionController.list);

/*
 * GET
 */
router.get('/:id', nearbyAttractionController.show);

/*
 * POST
 */
router.post('/', nearbyAttractionController.create);

/*
 * PUT
 */
router.put('/:id', nearbyAttractionController.update);

/*
 * DELETE
 */
router.delete('/:id', nearbyAttractionController.remove);

module.exports = router;
