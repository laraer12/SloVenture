var express = require('express');
var router = express.Router();
var nearbyAccommodationController = require('../controllers/nearbyAccommodationController.js');

/*
 * GET
 */
router.get('/', nearbyAccommodationController.list);

/*
 * GET
 */
router.get('/:id', nearbyAccommodationController.show);

/*
 * POST
 */
router.post('/', nearbyAccommodationController.create);

/*
 * PUT
 */
router.put('/:id', nearbyAccommodationController.update);

/*
 * DELETE
 */
router.delete('/:id', nearbyAccommodationController.remove);

module.exports = router;
