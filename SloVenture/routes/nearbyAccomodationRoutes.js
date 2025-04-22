var express = require('express');
var router = express.Router();
var nearbyAccomodationController = require('../controllers/nearbyAccomodationController.js');

/*
 * GET
 */
router.get('/', nearbyAccomodationController.list);

/*
 * GET
 */
router.get('/:id', nearbyAccomodationController.show);

/*
 * POST
 */
router.post('/', nearbyAccomodationController.create);

/*
 * PUT
 */
router.put('/:id', nearbyAccomodationController.update);

/*
 * DELETE
 */
router.delete('/:id', nearbyAccomodationController.remove);

module.exports = router;
