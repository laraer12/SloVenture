var ReviewModel = require('../models/reviewModel.js');
var { AttractionModel } = require('../models/attractionModel.js');

/**
 * reviewController.js
 *
 * @description :: Server-side logic for managing reviews.
 */
module.exports = {

    /**
     * reviewController.list()
     */
    list: function (req, res) {
        ReviewModel.find(function (err, reviews) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting review.',
                    error: err
                });
            }

            return res.json(reviews);
        });
    },

    /**
     * reviewController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        ReviewModel.findOne({_id: id}, function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting review.',
                    error: err
                });
            }

            if (!review) {
                return res.status(404).json({
                    message: 'No such review'
                });
            }

            return res.json(review);
        });
    },

    /**
     * reviewController.create()
     * ustvarim mnenje, podam glasove
     */
create: function (req, res) {
    const userId = req.body.isFakeData ? req.body.userId : req.user.userId;

    if (!req.body.rating || req.body.rating < 1 ||
        !req.body.ratingFamilyFriendly || req.body.ratingFamilyFriendly < 1 ||
        !req.body.ratingElderlyFriendly || req.body.ratingElderlyFriendly < 1 ||
        !req.body.ratingAccessible || req.body.ratingAccessible < 1) {
        return res.status(400).json({
            message: 'Every review has to be set and at least from 1 to 5'
        });
    }

    ReviewModel.findOneAndUpdate(
        // filter
        { userId: userId, attractionId: req.body.attractionId },

        // update
        {
            userId: userId,
            attractionId: req.body.attractionId,
            rating: req.body.rating,
            ratingFamilyFriendly: req.body.ratingFamilyFriendly,
            ratingElderlyFriendly: req.body.ratingElderlyFriendly,
            ratingAccessible: req.body.ratingAccessible,
            createdAt: req.body.createdAt,
            isFakeData: req.body.isFakeData || false
        },

        // options
        { upsert: true, new: true, setDefaultsOnInsert: true },

        function (err, review) {
            if (err) {
                console.error('Review creation/updating error:', err);
                return res.status(500).json({
                    message: 'Error creating or updating review',
                    error: err.message || err
                });
            }

            const io = req.app.get('io');
            io.emit("reviewAdded", review);

            return res.status(200).json({
                message: 'Review successfully given',
                review: review
            });
        }
    );
},


    /**
     * reviewController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        ReviewModel.findOne({_id: id}, function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting review',
                    error: err
                });
            }
            if (!review) {
                return res.status(404).json({
                    message: 'No such review'
                });
            }
            review.userId = req.body.userId ? req.body.userId : review.userId;
			review.attractionId = req.body.attractionId ? req.body.attractionId : review.attractionId;
			review.rating = req.body.rating ? req.body.rating : review.rating;
			review.ratingFamilyFriendly = req.body.ratingFamilyFriendly ? req.body.ratingFamilyFriendly : review.ratingFamilyFriendly;
			review.ratingElderlyFriendly = req.body.ratingElderlyFriendly ? req.body.ratingElderlyFriendly : review.ratingElderlyFriendly;
			review.ratingAccessible = req.body.ratingAccessible ? req.body.ratingAccessible : review.ratingAccessible;
			review.createdAt = req.body.createdAt ? req.body.createdAt : review.createdAt;
			review.isFakeData = req.body.isFakeData !== undefined ? req.body.isFakeData : review.isFakeData;

            review.save(function (err, review) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating review.',
                        error: err
                    });
                }
                return res.json(review);
            });
        });
    },

    /**
     * reviewController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        ReviewModel.findByIdAndRemove(id, function (err, review) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the review.',
                    error: err
                });
            }
            return res.status(204).json();
        });
    },

    /**
     * reviewController.getAveragesByAttraction()
     * pridobim povprečje glasov glede na določeno znamenitost
    */
    getAveragesByAttraction: async function (req, res) {
        const { attractionId } = req.params;

        try {
            // poiščem vse ocene za določeno znamenitost, če ni, vrnem prazen seznam (spodaj)
            const reviews = await ReviewModel.find({ attractionId });

            if (!reviews.length) 
                return res.json({ averages: null, reviews: [] });
            
            // drugače izračunam povprečja za dana polja v seznamu ocen
            const avg = (arr, key) => arr.reduce((sum, r) => sum + (r[key] || 0), 0) / arr.length;

            // izračunam, koliko je ocen od 1 do 5
            const getDistribution = (arr, key) => {
                const dist = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };

                arr.forEach(r => {
                    const val = r[key];

                    if (val >= 1 && val <= 5)
                        dist[val]++;
                });
                return dist;
            };

            // izračunam povprečja za vse tipe ocen
            const averages = {
                rating: avg(reviews, 'rating'),
                ratingFamilyFriendly: avg(reviews, 'ratingFamilyFriendly'),
                ratingElderlyFriendly: avg(reviews, 'ratingElderlyFriendly'),
                ratingAccessible: avg(reviews, 'ratingAccessible'),
            };
            
            // izračunam distribucije ocen za vse tipe ocen
            const distributions = {
                rating: getDistribution(reviews, 'rating'),
                ratingFamilyFriendly: getDistribution(reviews, 'ratingFamilyFriendly'),
                ratingElderlyFriendly: getDistribution(reviews, 'ratingElderlyFriendly'),
                ratingAccessible: getDistribution(reviews, 'ratingAccessible'),
            };

            // shranim posobljene podatke o oceni tudi v attraction v bazi
            const updatedAttraction = await AttractionModel.findByIdAndUpdate(
                attractionId,
                {
                    $set: {
                        rating: averages.rating,
                        ratingFamilyFriendly: averages.ratingFamilyFriendly,
                        ratingElderlyFriendly: averages.ratingElderlyFriendly,
                        ratingAccessible: averages.ratingAccessible
                    }
                },
                { new: true }
            );

            if (!updatedAttraction) {
                console.error('Attraction not found with ID:', attractionId);
                return res.status(404).json({ message: 'Attraction not found' });
            }
            res.json({ averages, distributions, count: reviews.length });
        }
        catch (err) {
            console.error('Full error:', err);
            res.status(500).json({ message: 'Error getting averages', error: err });
        }
    },
/*
    reviewsByAttraction: async function (req, res) {
    try {
        const results = await ReviewModel.aggregate([
        {
            $group: {
                _id: "$attractionId",
                avgRating: { $avg: "$rating" },
                avgFamilyFriendly: { $avg: "$ratingFamilyFriendly" },
                avgElderlyFriendly: { $avg: "$ratingElderlyFriendly" },
                avgAccessible: { $avg: "$ratingAccessible" }
            }
        },
        {
            $project: {
                attractionId: "$_id",
                avgRatingAll: {
                $avg: ["$avgRating", "$avgFamilyFriendly", "$avgElderlyFriendly", "$avgAccessible"]
                }
            }
        },
        {
            $sort: { avgRatingAll: -1 }
        },
        {
            $limit: 15
        },
        {
            $lookup: {
                from: "attractions", 
                localField: "attractionId",
                foreignField: "_id",
                as: "attraction"
            }
        },
        {
            $unwind: "$attraction"
        },
        {
            $project: {
                attractionId: 1,
                avgRatingAll: 1,
                attractionName: "$attraction.name"
            }
        }
        ]);

        return res.json(results);
    } catch (error) {
        console.error("Napaka pri pridobivanju najbolj ocenjenih znamenitosti:", error);
        return res.status(500).json({ message: "Napaka pri pridobivanju podatkov." });
    }
  }
    */
   reviewsByAttraction: async function (req, res) {
    try {
        const allReviews = await ReviewModel.find({})
            .populate('attractionId', 'name')
            .lean();

        const ratingData = {};

        for (const review of allReviews) {
            const attr = review.attractionId;
            if (!attr || !attr._id) {
                continue;
            }

            const id = attr._id.toString();

            if (!ratingData[id]) {
                ratingData[id] = {
                    attractionId: attr._id,
                    attractionName: attr.name,
                    totalRatings: 0,
                    sumRating: 0,
                    sumFamilyFriendly: 0,
                    sumElderlyFriendly: 0,
                    sumAccessible: 0
                };
            }

            ratingData[id].totalRatings += 1;
            ratingData[id].sumRating += review.rating || 0;
            ratingData[id].sumFamilyFriendly += review.ratingFamilyFriendly || 0;
            ratingData[id].sumElderlyFriendly += review.ratingElderlyFriendly || 0;
            ratingData[id].sumAccessible += review.ratingAccessible || 0;
        }

        const results = Object.values(ratingData).map(entry => {
            const avgRating = entry.sumRating / entry.totalRatings;
            const avgFamily = entry.sumFamilyFriendly / entry.totalRatings;
            const avgElderly = entry.sumElderlyFriendly / entry.totalRatings;
            const avgAccessible = entry.sumAccessible / entry.totalRatings;
            const avgRatingAll = (avgRating + avgFamily + avgElderly + avgAccessible) / 4;

            return {
                attractionId: entry.attractionId,
                attractionName: entry.attractionName,
                avgRatingAll: +avgRatingAll.toFixed(2)
            };
        });

        const sortedTop = results
            .sort((a, b) => b.avgRatingAll - a.avgRatingAll)
            .slice(0, 15);

        res.json(sortedTop);
    } catch (error) {
        console.error("Napaka pri pridobivanju ocen znamenitosti:", error);
        res.status(500).json({ message: "Napaka pri pridobivanju podatkov", error: error.message || error });
    }
}


};