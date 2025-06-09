var express = require('express');
var router = express.Router();
var attractionController = require('../controllers/attractionController.js');

/*
// Iskanje znamenitosti preko LocationIQ API-ja
router.get('/search', attractionController.search);
*/

router.get('/fullKotlin/:id', attractionController.showFullAttractionKotlin);
router.get('/listKotlin', attractionController.listKotlin);
router.get('/getAllAttractionsKotlin', attractionController.getAllAttractionsKotlin);

router.get('/classifications', attractionController.listClassifications);
router.get('/classification/:classificationName', attractionController.listByClassification);

router.get('/', attractionController.list);
router.get('/:id', attractionController.show);
router.post('/', attractionController.create);
router.put('/:id', attractionController.update);
router.delete('/:id', attractionController.remove);
router.get('/region/:regionId', attractionController.listByRegion);
router.get('/type/:typeId', attractionController.listByLocationType);

module.exports = router;