var TripModel = require('../models/tripModel.js');
var TripAttractionModel = require('../models/tripAttractionModel.js');
const { AttractionModel } = require('../models/attractionModel.js');
var AttractionImageModel = require('../models/attractionImageModel.js');

/**
 * tripController.js
 *
 * @description :: Server-side logic for managing trips.
 */
module.exports = {

    /**
     * tripController.list()
     */
    list: function (req, res) {
        var userId = req.session.userId;

        TripModel.find({userId: userId},function (err, trips) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting trip.',
                    error: err
                });
            }

            return res.json(trips);
        });
    },

    /**
     * tripController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        TripModel.findOne({_id: id}, function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting trip.',
                    error: err
                });
            }
            if (!trip) {
                return res.status(404).json({
                    message: 'No such trip'
                });
            }
            TripAttractionModel.find({ tripId: id })
                .populate({
                    path: 'attractionId',
                    model: AttractionModel,
                })
                .exec(async function (err, tripAttractions) {
                    if (err) {
                        return res.status(500).json({
                            message: 'Error when getting trip attractions.',
                            error: err,
                        });
                    }
                    const result = await Promise.all(tripAttractions.map(async (ta) => {
                    const images = await AttractionImageModel.find({ attractionId: ta.attractionId._id });

                    return {
                        tripAttraction: ta,
                        attraction: ta.attractionId,
                        images: images
                    };
                    }));

                    return res.json({
                        trip: trip,
                        attractions: result,
                    });
                });
        });
    },
    

    /**
     * tripController.create()
     */
    create: function (req, res) {
        const { userId, tripName, startDate } = req.body;

        if (!userId || !tripName || !startDate ) {
            return res.status(400).json({
                message: 'No field should be empty'
            });
        }
        var trip = new TripModel({
            userId: userId,
            tripName: tripName,
            tripDescription: req.body.tripDescription,
            startDate: startDate,
            isPublic: req.body.isPublic ?? false,
            createdAt: req.body.createdAt ?? new Date()
        });

        trip.save(function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error creating trip',
                    error: err
                });
            }
            return res.status(201).json(trip);
        });
    },

    /**
     * tripController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        TripModel.findOne({_id: id}, function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting trip',
                    error: err
                });
            }

            if (!trip) {
                return res.status(404).json({
                    message: 'No such trip'
                });
            }

            trip.userId = req.body.userId ? req.body.userId : trip.userId;
			trip.tripName = req.body.tripName ? req.body.tripName : trip.tripName;
			trip.tripDescription = req.body.tripDescription ? req.body.tripDescription : trip.tripDescription;
			trip.startDate = req.body.startDate ? req.body.startDate : trip.startDate;
			trip.endDate = req.body.endDate ? req.body.endDate : trip.endDate;
			trip.isPublic = req.body.isPublic ? req.body.isPublic : trip.isPublic;
			trip.createdAt = req.body.createdAt ? req.body.createdAt : trip.createdAt;
			
            trip.save(function (err, trip) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating trip.',
                        error: err
                    });
                }

                return res.json(trip);
            });
        });
    },

    /**
     * tripController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        TripModel.findByIdAndRemove(id, function (err, trip) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the trip.',
                    error: err
                });
            }

            TripAttractionModel.deleteMany({tripId: id}, function (err) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when deleting trip attractions.',
                        error: err
                    });
                }
            });

            return res.status(204).json();
        });
    },

    tripsByUser: async function (req, res, next) {
        const userId = req.params.userId;

        try {
            const trips = await TripModel.find({ userId });

            const tripsWithAttractions = await Promise.all(
            trips.map(async (trip) => {

                // pridobim vse tripAttractions za dano potovanje, sortirane po 'order' naraščajoče
                const tripAttractions = await TripAttractionModel.find({ tripId: trip._id })
                    .sort({ order: 1 })  // sortiranje po order
                    .populate('attractionId');

                    // pridobim prvo sliko znamenitosti (če obstaja)
                    let firstImageUrl = null;

                    if (tripAttractions.length > 0) {
                        const firstAttractionId = tripAttractions[0].attractionId._id;
                        const image = await AttractionImageModel.findOne({ attractionId: firstAttractionId });
                        firstImageUrl = image ? image.url : null;
                    }
                    return {
                        ...trip.toObject(),
                        attractions: tripAttractions,
                        firstImageUrl,
                    };
                })
            );

            res.status(200).json(tripsWithAttractions);
        }
        catch (err) {
            console.error("Error getting trips with attractions: ", err);
            next(err);
        }
    }
};