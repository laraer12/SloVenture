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

        // preverjam če vse ocene obstajajo in so večje od 1
        if (!req.body.rating || req.body.rating < 1 ||
            !req.body.ratingFamilyFriendly || req.body.ratingFamilyFriendly < 1 ||
            !req.body.ratingElderlyFriendly || req.body.ratingElderlyFriendly < 1 ||
            !req.body.ratingAccessible || req.body.ratingAccessible < 1) {
            return res.status(400).json({
                message: 'Every review has to be set and at least from 1 to 5'
            });
        }

        // če obstaja ocena, posodobi, če ne, ustvari novo
        ReviewModel.findOneAndUpdate(
            // poiščem oceno, ki ustreza uporabniku in znamenitosti
            { userId: req.body.userId, attractionId: req.body.attractionId },
            
            // podatki za posodobitev ali vnos, če ocena še ne obstaja
            {
                userId: req.body.userId,
                attractionId: req.body.attractionId,
                rating: req.body.rating,
                ratingFamilyFriendly: req.body.ratingFamilyFriendly,
                ratingElderlyFriendly: req.body.ratingElderlyFriendly,
                ratingAccessible: req.body.ratingAccessible,
                createdAt: req.body.createdAt,
                isFakeData: req.body.isFakeData || false // privzeto je isFakeData false, če ni podano
            },
            
            /*
            Opcije metode:
            - upsert: če ocena ne obstaja, jo ustvarim
            - new: vrnem novo ali posodobljeno oceno
            - setDefaultsOnInsert: če se ustvari nova ocena, nastavim privzete vrednosti iz modela
            */
            { upsert: true, new: true, setDefaultsOnInsert: true },

            function (err, review) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error creating or updating review',
                        error: err
                    });
                }
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
    }
};