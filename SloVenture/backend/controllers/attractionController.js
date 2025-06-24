const axios = require('axios');
const { AttractionModel } = require('../models/attractionModel.js');
var AttractionImageModel = require('../models/attractionImageModel.js');
var ReviewModel = require('../models/reviewModel.js');
var WeatherDataModel = require('../models/weatherDataModel.js');
var nearbyAttractionModel = require('../models/nearbyAttractionModel.js');
const app = require('../app.js');

/**
 * attractionController.js
 *
 * @description :: Server-side logic for managing attractions.
 */
module.exports = {
    /**
     * attractionController.list()
     */
    list: function (req, res) {
        AttractionModel.find()
            .populate('regionId')
            .exec(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction.',
                        error: err
                    });
                }
                if (attractions.length === 0)
                    return res.json([]);

                var promises = attractions.map(function (attraction) {
                    return AttractionImageModel.find({ attractionId: attraction._id })
                        .then(function (images) {
                            return {
                                attraction: attraction,
                                images: images
                            };
                        })
                        .catch(function () {
                            return {
                                attraction: attraction,
                                images: []
                            };
                        });
                });

                Promise.all(promises)
                    .then(function (result) {
                        return res.json(result);
                    })
                    .catch(function (err) {
                        return res.status(500).json({
                            message: 'Error when processing attractions.',
                            error: err
                        });
                    });
            });
    },

    getAllAttractionsKotlin: function (req, res) { //Kotlin: samo id, name in location
        AttractionModel.find({}, '_id name location')
            .exec(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction locations.',
                        error: err
                    });
                }

                res.json(attractions);
            });
    },

    listKotlin: function (req, res) {
        AttractionModel.find()
            .exec(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction.',
                        error: err
                    });
                }
                if (attractions.length === 0)
                    return res.json([]);

                var promises = attractions.map(function (attraction) {
                    return AttractionImageModel.find({ attractionId: attraction._id })
                        .then(function (images) {
                            return {
                                attraction: attraction,
                                images: images
                            };
                        })
                        .catch(function () {
                            return {
                                attraction: attraction,
                                images: []
                            };
                        });
                });

                Promise.all(promises)
                    .then(function (result) {
                        return res.json(result);
                    })
                    .catch(function (err) {
                        return res.status(500).json({
                            message: 'Error when processing attractions.',
                            error: err
                        });
                    });
            });
    },

    /**
     * attractionController.listByRegion()
     */
    //TODO CHANGE
    listByRegion: function (req, res) {
        var regionId = req.params.regionId;

        AttractionModel.find({ regionId: regionId })
            .populate('regionId')
            .exec(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction.',
                        error: err
                    });
                }
                if (attractions.length === 0)
                    return res.json([]);

                var promises = attractions.map(function (attraction) {
                    return AttractionImageModel.find({ attractionId: attraction._id })
                        .then(function (images) {
                            return {
                                attraction: attraction,
                                images: images
                            };
                        })
                        .catch(function () {
                            return {
                                attraction: attraction,
                                images: []
                            };
                        });
                });

                Promise.all(promises)
                    .then(function (result) {
                        return res.json(result);
                    })
                    .catch(function (err) {
                        return res.status(500).json({
                            message: 'Error when processing attractions.',
                            error: err
                        });
                    });
            });
    },

    listClassifications: function(req, res) {
        AttractionModel.find({}, 'classification')
            .then(attractions => {
                const uniqueClassifications = [...new Set(attractions
                    .map(a => a.classification)
                    .filter(c => c && c.trim() !== ''))];

                // Vrni array stringov
                res.json(uniqueClassifications);
            })
            .catch(err => {
                console.error("Napaka pri pridobivanju klasifikacij:", err);
                res.status(500).json({ message: "Napaka pri pridobivanju klasifikacij" });
            });
    },
    
    /**
     * attractionController.listByClassification()
     */
    listByClassification: function (req, res) {
        const classification = req.params.classificationName;

        AttractionModel.find({ classification: classification })  // ← tukaj iščeš po stringu
            .populate('regionId')
            .exec(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction.',
                        error: err
                    });
                }

                if (attractions.length === 0)
                    return res.json([]);

                const promises = attractions.map(function (attraction) {
                    return AttractionImageModel.find({ attractionId: attraction._id })
                        .then(function (images) {
                            return {
                                attraction: attraction,
                                images: images
                            };
                        })
                        .catch(function () {
                            return {
                                attraction: attraction,
                                images: []
                            };
                        });
                });

                Promise.all(promises)
                    .then(function (result) {
                        return res.json(result);
                    })
                    .catch(function (err) {
                        return res.status(500).json({
                            message: 'Error when processing attractions.',
                            error: err
                        });
                    });
            });
    },

    /**
     * attractionController.listByLocationType()
     */
    listByLocationType: function (req, res) {
        var locationType = req.params.locationType;

        AttractionModel.find({ locationType: locationType })
            .populate('regionId')
            .exec(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction.',
                        error: err
                    });
                }
                if (attractions.length === 0)
                    return res.json([]);

                var promises = attractions.map(function (attraction) {
                    return AttractionImageModel.find({ attractionId: attraction._id })
                        .then(function (images) {
                            return {
                                attraction: attraction,
                                images: images
                            };
                        })
                        .catch(function () {
                            return {
                                attraction: attraction,
                                images: []
                            };
                        });
                });

                Promise.all(promises)
                    .then(function (result) {
                        return res.json(result);
                    })
                    .catch(function (err) {
                        return res.status(500).json({
                            message: 'Error when processing attractions.',
                            error: err
                        });
                    });
            });
    },

    showFullAttractionKotlin: function (req, res) {
    var id = req.params.id;

    AttractionModel.findOne({ _id: id })
        .exec(function (err, attraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting attraction.',
                    error: err
                });
            }
            if (!attraction) {
                return res.status(404).json({
                    message: 'No such attraction'
                });
            }

            AttractionImageModel.find({ attractionId: id })
                .populate('uploadedBy')
                .exec(function (err, images) {
                    if (err) {
                        return res.status(500).json({
                            message: 'Error when getting attraction images.',
                            error: err
                        });
                    }

                    WeatherDataModel.findOne({ attractionId: id }).exec(function (err, weatherData) {
                        if (err) {
                            return res.status(500).json({
                                message: 'Error when getting weather data.',
                                error: err
                            });
                        }

                        nearbyAttractionModel.find({ attractionId: id })
                            .exec(function (err, nearbyAttractions) {
                                if (err) {
                                    return res.status(500).json({
                                        message: 'Error when getting nearby attractions.',
                                        error: err
                                    });
                                }

                                return res.json({
                                    attraction: attraction,
                                    images: images,
                                    weatherData: weatherData,
                                    nearbyAttractions: nearbyAttractions
                                });
                            });
                    });
                });
        });
    },


    /**
     * attractionController.show()
     */

    show: function (req, res) {
        var id = req.params.id;

        AttractionModel.findOne({ _id: id })
            .populate('regionId')
            .exec(function (err, attraction) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attraction.',
                        error: err
                    });
                }
                if (!attraction) {
                    return res.status(404).json({
                        message: 'No such attraction'
                    });
                }                    
                AttractionImageModel.find({ attractionId: id })
                    .populate({
                        path: 'uploadedBy',
                        model: 'user'
                    })
                    .exec(function (err, images) {
                        if (err) {
                            return res.status(500).json({
                                message: 'Error when getting attraction images.',
                                error: err
                            });
                        }
                        ReviewModel.find({ attractionId: id })
                            .populate({
                                path: 'userId',
                                model: 'user'
                            })
                            .exec(function (err, reviews) {
                                if (err) {
                                    return res.status(500).json({
                                        message: 'Error when getting reviews.',
                                        error: err
                                    });
                                }
                                WeatherDataModel.find({ attractionId: id }).exec(function (err, weatherData) {
                                    if (err) {
                                        return res.status(500).json({
                                            message: 'Error when getting weather data.',
                                            error: err
                                        });
                                    }
                                   // pridobimo nearby attractions
                                    nearbyAttractionModel.find({ attractionId: id })
                                        .populate({
                                            path: 'nearbyAttractionId',
                                            model: 'attraction'
                                        })
                                        .exec(function (err, nearbyAttractions) {
                                            if (err) {
                                                return res.status(500).json({
                                                    message: 'Error when getting nearby attractions.',
                                                    error: err
                                                });
                                            }
                                            if (!nearbyAttractions || nearbyAttractions.length === 0) {
                                                return res.json({
                                                    attraction,
                                                    images,
                                                    reviews,
                                                    weatherData,
                                                    nearbyAttractions: []
                                                });
                                            }
                                            
                                            // pridobimo slike za vsako znamenitost v nearbyAttractions
                                            var promises = nearbyAttractions.map(function (nearbyAttraction) {
                                            return AttractionImageModel.find({ attractionId: nearbyAttraction.nearbyAttractionId._id })
                                                .then(function (nearbyImages) {
                                                    return {
                                                        nearbyAttraction: nearbyAttraction,
                                                        images: nearbyImages
                                                    };
                                                })
                                                .catch(function () {
                                                    return {
                                                        nearbyAttraction: nearbyAttraction,
                                                        images: []
                                                    };
                                                });
                                            });

                                            Promise.all(promises)
                                                .then(function (results) {
                                                  const enhancedNearbyAttractions = results.map(item => {
                                                    // pridobimo "čisti" objekt znamenitosti
                                                    const attraction = item.nearbyAttraction.nearbyAttractionId.toObject ? item.nearbyAttraction.nearbyAttractionId.toObject() : item.nearbyAttraction.nearbyAttractionId;
                                                    attraction.images = item.images;
                                                    return attraction;
                                                  });
                                              
                                                  return res.json({
                                                    attraction,
                                                    images,
                                                    reviews,
                                                    weatherData,
                                                    nearbyAttractions: enhancedNearbyAttractions
                                                  });
                                                })
                                            });
                                });
                            });
                    });
            });
    },


    /**
     * attractionController.create()
     */
    /*
    create: function (req, res) {

        function toMeters(lat, lon){
            const R = 6371e3; // Radius of the Earth in meters
            const x =toRadians(lon) * R * Math.cos(toRadians(lat));
            const y = toRadians(lat) * R;
            return { x: x, y: y };
        }

        function approximateDistance(lat1, lon1, lat2, lon2) {
            const {x: x1, y: y1} = toMeters(lat1, lon1);
            const {x: x2, y: y2} = toMeters(lat2, lon2);
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        function toRadians(degrees) {
            return degrees * (Math.PI / 180);
        }

    //test TODO
        console.log("Incoming request body:", req.body);

        const attraction = new AttractionModel({
            name: req.body.name,
            regionId: req.body.regionId,
            location: {
                lat: req.body.location.lat,
                lon: req.body.location.lon
            },
            address: {
                street: req.body.address.street,
                city: req.body.address.city,
                postalCode: req.body.address.postalCode,
                country: req.body.address.country
            },
            description: req.body.description,
            classification: req.body.classification,
            locationType: req.body.locationType,
            elevation: req.body.elevation,
            accessibilityOptions: req.body.accessibilityOptions,
            ratingFamilyFriendly: req.body.ratingFamilyFriendly,
            ratingElderlyFriendly: req.body.ratingElderlyFriendly,
            ratingAccessible: req.body.ratingAccessible,
            rating: req.body.rating,
            googleMapsLink: req.body.googleMapsLink,
            createdAt: Date.now(),
            verified: req.body.verified
        });

        attraction.save(function (err, saved) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating attraction',
                    error: err
                });
            }

            AttractionModel.find(function (err, attractions) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting attractions.',
                        error: err
                    });
                }

                //izračun nearby attractions
                attractions.forEach(function (attr) {
                    var distance = approximateDistance(attr.location.lat, attr.location.lon, attraction.location.lat, attraction.location.lon);

                    if (distance < 2000 && attr._id.toString() !== attraction._id.toString()) {              

                        var nearbyAttraction = new nearbyAttractionModel({
                            attractionId: attraction._id,
                            nearbyAttractionId: attr._id,
                            distance: distance
                        })

                        nearbyAttraction.save(function (err) {
                            if (err && err.code === 11000) {
                            }
                            else if (err) {
                                console.error('Error saving nearby attraction:', err);
                            }
                        });

                        var nearbyAttraction2 = new nearbyAttractionModel({
                            attractionId: attr._id,
                            nearbyAttractionId: attraction._id,
                            distance: distance
                        });

                        nearbyAttraction2.save(function (err) {
                            if (err && err.code === 11000) {
                            }
                            else if (err) {
                                console.error('Error saving nearby attraction:', err);
                            }
                        });
                    }
                });
            });

            return res.status(201).json(saved);
        });
    },  */
   create: function (req, res) {
        function toMeters(lat, lon) {
            const R = 6371e3;
            const x = toRadians(lon) * R * Math.cos(toRadians(lat));
            const y = toRadians(lat) * R;
            return { x: x, y: y };
        }

        function approximateDistance(lat1, lon1, lat2, lon2) {
            const { x: x1, y: y1 } = toMeters(lat1, lon1);
            const { x: x2, y: y2 } = toMeters(lat2, lon2);
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        function toRadians(degrees) {
            return degrees * (Math.PI / 180);
        }

        console.log("Incoming request body:", req.body);

        AttractionModel.findOne({ name: req.body.name }, function (err, existingAttraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when checking for existing attraction',
                    error: err
                });
            }

            if (existingAttraction) {
                return res.status(409).json({
                    message: 'Attraction with the same name already exists'
                });
            }

            const attraction = new AttractionModel({
                name: req.body.name,
                regionId: req.body.regionId,
                location: {
                    lat: req.body.location.lat,
                    lon: req.body.location.lon
                },
                address: req.body.address,
                description: req.body.description,
                classification: req.body.classification,
                locationType: req.body.locationType,
                elevation: req.body.elevation,
                accessibilityOptions: req.body.accessibilityOptions,
                ratingFamilyFriendly: req.body.ratingFamilyFriendly,
                ratingElderlyFriendly: req.body.ratingElderlyFriendly,
                ratingAccessible: req.body.ratingAccessible,
                rating: req.body.rating,
                googleMapsLink: req.body.googleMapsLink,
                createdAt: Date.now(),
                verified: req.body.verified
            });

            attraction.save(function (err, saved) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when creating attraction',
                        error: err
                    });
                }

                AttractionModel.find(function (err, attractions) {
                    if (err) {
                        return res.status(500).json({
                            message: 'Error when getting attractions.',
                            error: err
                        });
                    }

                    attractions.forEach(function (attr) {
                        var distance = approximateDistance(attr.location.lat, attr.location.lon, attraction.location.lat, attraction.location.lon);

                        if (distance < 8000 && attr._id.toString() !== attraction._id.toString()) {
                            const nearbyAttraction = new nearbyAttractionModel({
                                attractionId: attraction._id,
                                nearbyAttractionId: attr._id,
                                distance: distance
                            });

                            nearbyAttraction.save(err => {
                                if (err && err.code !== 11000) {
                                    console.error('Error saving nearby attraction:', err);
                                }
                            });

                            const nearbyAttraction2 = new nearbyAttractionModel({
                                attractionId: attr._id,
                                nearbyAttractionId: attraction._id,
                                distance: distance
                            });

                            nearbyAttraction2.save(err => {
                                if (err && err.code !== 11000) {
                                    console.error('Error saving nearby attraction:', err);
                                }
                            });
                        }
                    });
                });

                return res.status(201).json(saved);
            });
        });
    },
 
    /**
     * attractionController.update()
     */
update: function (req, res) {
    const id = req.params.id;

    AttractionModel.findById(id, function (err, attraction) {
        if (err) return res.status(500).json({ message: 'Error when getting attraction', error: err });
        if (!attraction) return res.status(404).json({ message: 'No such attraction' });

        attraction.name = req.body.name || attraction.name;
        attraction.regionId = req.body.regionId || attraction.regionId;
        attraction.description = req.body.description || attraction.description;
        attraction.classification = req.body.classification || attraction.classification;
        attraction.locationType = req.body.locationType || attraction.locationType;
        attraction.elevation = req.body.elevation || attraction.elevation;
        attraction.accessibilityOptions = req.body.accessibilityOptions || attraction.accessibilityOptions;
        attraction.ratingFamilyFriendly = req.body.ratingFamilyFriendly || attraction.ratingFamilyFriendly;
        attraction.ratingElderlyFriendly = req.body.ratingElderlyFriendly || attraction.ratingElderlyFriendly;
        attraction.ratingAccessible = req.body.ratingAccessible || attraction.ratingAccessible;
        attraction.rating = req.body.rating || attraction.rating;
        attraction.requiresReservation = req.body.requiresReservation ?? attraction.requiresReservation;
        attraction.entryFee = req.body.entryFee ?? attraction.entryFee;
        attraction.googleMapsLink = req.body.googleMapsLink || attraction.googleMapsLink;
        attraction.verified = req.body.verified ?? attraction.verified;

        if (req.body.location) {
            attraction.location.lat = req.body.location.lat ?? attraction.location.lat;
            attraction.location.lon = req.body.location.lon ?? attraction.location.lon;
        }

        if (req.body.address) {
            attraction.address.street = req.body.address.street ?? attraction.address.street;
            attraction.address.city = req.body.address.city ?? attraction.address.city;
            attraction.address.postalCode = req.body.address.postalCode ?? attraction.address.postalCode;
            attraction.address.country = req.body.address.country ?? attraction.address.country;
        }

        if (req.body.openingHours) {
            const days = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'];
            days.forEach(day => {
                attraction.openingHours[day] = req.body.openingHours[day] ?? attraction.openingHours[day];
            });
        }

        attraction.save(function (err, updated) {
            if (err) return res.status(500).json({ message: 'Error when updating attraction', error: err });
            return res.json(updated);
        });
    });
},


    /**
     * attractionController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        AttractionModel.findByIdAndRemove(id, function (err, attraction) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the attraction.',
                    error: err
                });
            }
            if (!attraction) {
                return res.status(404).json({
                    message: 'No such attraction'
                });
            }
            AttractionImageModel.deleteMany({ attractionId: id }, function (err) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when deleting attraction images.',
                        error: err
                    });
                }
                ReviewModel.deleteMany({ attractionId: id }, function (err) {
                    if (err) {
                        return res.status(500).json({
                            message: 'Error when deleting reviews.',
                            error: err
                        });
                    }
                    WeatherDataModel.deleteMany({ attractionId: id }, function (err) {
                        if (err) {
                            return res.status(500).json({
                                message: 'Error when deleting weather data.',
                                error: err
                            });
                        }
                        nearbyAttractionModel.deleteMany({ attractionId: id }, function (err) {
                            if (err) {
                                return res.status(500).json({
                                    message: 'Error when deleting nearby attractions.',
                                    error: err
                                });
                            }

                            nearbyAttractionModel.deleteMany({ nearbyAttractionId: id }, function (err) {
                                if (err) {
                                    return res.status(500).json({
                                        message: 'Error when deleting nearby attractions.',
                                        error: err
                                    });
                                }

                                return res.status(204).json();
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * Iskanje znamenitosti po imenu / lokaciji z uporabo LocationIQ
    */
    /*
    search: async function (req, res) {
        const { attractionName, lat, lon, autocomplete } = req.query;
        const apiKey = process.env.LOCATIONIQ_API_KEY;
    
        // validiram vhodne parametre
        if (lat || lon) {
            const parsedLat = parseFloat(lat);
            const parsedLon = parseFloat(lon);
            
            if (isNaN(parsedLat) || isNaN(parsedLon)) {
                return res.status(400).json({
                    message: 'Invalid latitude or longitude. Please provide valid numeric values'
                });
            }
    
            // preverim, ali sta lat in lon v razponu
            if (parsedLat < -90 || parsedLat > 90) {
                return res.status(400).json({
                    message: 'Latitude must be between -90 and 90 degrees'
                });
            }
            if (parsedLon < -180 || parsedLon > 180) {
                return res.status(400).json({
                    message: 'Longitude must be between -180 and 180 degrees'
                });
            }
        }
        if (attractionName) {
            // preverim, če je attractionName tipa string
            if (typeof attractionName !== 'string' || attractionName.trim().length === 0) {
                return res.status(400).json({
                    message: 'Invalid attraction name. Please provide a valid non-empty string.'
                });
            }
        }
        try {
            // iskanje po lat in lon
            if (lat && lon) {
                const reverseResponse = await axios.get('https://us1.locationiq.com/v1/reverse.php', {
                    params: {
                        key: apiKey,
                        lat: parseFloat(lat),
                        lon: parseFloat(lon),
                        format: 'json'
                    }
                });
    
                return res.json({
                    data: reverseResponse.data
                });
            }
    
            // iskanje po imenu znamenitosti
            if (attractionName) {
                const endpoint = autocomplete === 'true' ? 'https://us1.locationiq.com/v1/autocomplete.php' : 'https://us1.locationiq.com/v1/search.php';

                const forwardResponse = await axios.get(endpoint, {
                    params: {
                        key: apiKey,
                        q: attractionName,
                        countrycodes: 'si', // samo slovenija
                        format: 'json'
                    }
                });
    
                return res.json({
                    data: forwardResponse.data
                });
            }
            return res.status(400).json({ message: 'Missing parameters: please provide either attractionName or lat/lon' });
    
        }
        catch (error) {
            console.error(error);
    
            return res.status(500).json({
                message: 'Error fetching data from LocationIQ',
                error: error.message
            });
        }
    }
    */    
};