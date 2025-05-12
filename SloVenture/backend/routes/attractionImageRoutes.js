var express = require('express');
var router = express.Router();
var attractionImageController = require('../controllers/attractionImageController.js');

/*
 * GET
 */
router.get('/', attractionImageController.list);

/*
 * GET
 */
router.get('/:id', attractionImageController.show);

/*
 * POST
 */
router.post('/', attractionImageController.create);

/*
 * PUT
 */
router.put('/:id', attractionImageController.update);

/*
 * DELETE
 */
router.delete('/:id', attractionImageController.remove);

module.exports = router;
