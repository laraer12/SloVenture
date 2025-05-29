var express = require('express');
var router = express.Router();
var weatherDataController = require('../controllers/weatherDataController.js');

/* test apija
za lat, lon in cnt
router.get('/', weatherDataController.getWeatherByCoordinates);
*/

router.get('/', weatherDataController.list);
router.get('/:id', weatherDataController.show);
router.post('/', weatherDataController.create);
router.put('/:id', weatherDataController.update);
router.delete('/:id', weatherDataController.remove);
router.get('/by-attraction/:attractionId', weatherDataController.getByAttractionId); // pridobim vreme za določeno znamenitost

module.exports = router;