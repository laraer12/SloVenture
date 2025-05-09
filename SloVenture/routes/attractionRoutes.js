var express = require('express');
var router = express.Router();
var attractionController = require('../controllers/attractionController.js');

// Iskanje znamenitosti preko LocationIQ API-ja
router.get('/search', attractionController.search);

/*
 * GET
 */
router.get('/', attractionController.list);

/*
 * GET
 */
router.get('/:id', attractionController.show);

/*
 * POST
 */
router.post('/', attractionController.create);

/*
 * PUT
 */
router.put('/:id', attractionController.update);

/*
 * DELETE
 */
router.delete('/:id', attractionController.remove);

module.exports = router;