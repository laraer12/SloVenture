var express = require('express');
var router = express.Router();
var weatherDataController = require('../controllers/weatherDataController.js');


// za lat, lon in cnt
router.get('/', weatherDataController.getWeatherByCoordinates);

router.get('/findByAttractionId/:attractionId', weatherDataController.findByAttractionId);
/*
 * GET
 */
router.get('/', weatherDataController.list);

/*
 * GET
 */
router.get('/:id', weatherDataController.show);

/*
 * POST
 */
router.post('/', weatherDataController.create);

/*
 * PUT
 */
router.put('/:id', weatherDataController.update);

/*
 * DELETE
 */
router.delete('/:id', weatherDataController.remove);

module.exports = router;