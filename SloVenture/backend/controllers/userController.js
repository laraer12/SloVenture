var UserModel = require('../models/userModel.js');
var TripModel = require('../models/tripModel.js');
var TripAttractionModel = require('../models/tripAttractionModel.js');

var axios = require('axios');

var multer = require('multer'); // za objavo datotek

var storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'public/images/'); // sem shranim slike
    },
    filename: function (req, file, cb) {
        cb(null, Date.now() + '-' + file.originalname); // ime slike vsebuje časovni žig in originalno ime
    }
});

var upload = multer({ storage: storage }); // inicializacija multer-ja

/**
 * userController.js
 *
 * @description :: Server-side logic for managing users.
 */
module.exports = {

    /**
     * userController.list()
     */
    list: function (req, res) {
        UserModel.find(function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting user.',
                    error: err
                });
            }
            return res.json(users);
        });
    },

    /**
     * userController.show()
     */
    show: function (req, res) {
        UserModel.findOne({ _id: req.params.id }, function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting user.',
                    error: err
                });
            }
            if (!user)
                return res.status(404).json({ message: 'No such user' });
            
            return res.json(user);
        });
    },

    /**
     * userController.create()
     */
    create: function (req, res) {
        var { username, email, password, captchaToken } = req.body;

        if (!username || !email || !password || !captchaToken)
            return res.status(400).json({ message: 'Vsa polja morajo biti izpolnjena!' });

        const secretKey = process.env.RECAPTCHA_SECRET_KEY;
        const verifyUrl = `https://www.google.com/recaptcha/api/siteverify?secret=${secretKey}&response=${captchaToken}`;

        axios.post(verifyUrl)
            .then(function (captchaRes) {
                if (!captchaRes.data.success)
                    return res.status(400).json({ message: 'Error verifying reCAPTCHA' });
            
            UserModel.findOne({ $or: [{ username: req.body.username }, { email: req.body.email }] }, function (err, existingUser) {
                if (err)
                    return res.status(500).json({ message: 'Error when checking user existence', error: err });
                
                if (existingUser)
                    return res.status(400).json({ message: 'Username or email already exists' });

                var user = new UserModel({
                    username: req.body.username,
                    email: req.body.email,
                    password: req.body.password
                });

                user.save(function (err, savedUser) {
                    if (err)
                        return res.status(500).json({ message: 'Error when creating user', error: err });
                    
                    return res.status(201).json(savedUser);
                });
            });
        })
        .catch(function (error) {
            return res.status(500).json({ message: 'Error communicating with reCAPTCHA server', error: error });
        });
    },

    /**
     * userController.update()
     */
    update: function (req, res) {
        UserModel.findOne({ _id: req.params.id }, function (err, user) {
            if (err)
                return res.status(500).json({ message: 'Error when getting user', error: err });
            
            if (!user)
                return res.status(404).json({ message: 'No such user' });

            if (req.body.isAdmin !== undefined)
                delete req.body.isAdmin;

            user.username = req.body.username || user.username;
            user.email = req.body.email || user.email;
            user.password = req.body.password || user.password;

            user.save(function (err, updatedUser) {
                if (err)
                    return res.status(500).json({ message: 'Error when updating user', error: err });
                
                return res.json(updatedUser);
            });
        });
    },

    /**
     * userController.remove()
     */
    remove: function (req, res) {
        const currentUserId = req.session.userId;
        const targetUserId = req.params.id;

        UserModel.findById(currentUserId, function (err, currentUser) {
            if (err || !currentUser)
                return res.status(500).json({ message: 'Error verifying user identity' });

            // Če trenutni uporabnik ni admin in želi izbrisati nekoga drugega – zavrni
            if (!currentUser.isAdmin && currentUserId !== targetUserId)
                return res.status(403).json({ message: "Cannot delete another user" });

            UserModel.findByIdAndRemove(targetUserId, function (err, user) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when deleting the user.',
                        error: err
                    });
                }

            TripModel.find({userId: id}, function (err, trips) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting the user\'s trips.',
                        error: err
                    });
                }

                trips.forEach(function (trip) {
                    TripAttractionModel.deleteMany({tripId: trip._id}, function (err) {
                        if (err) {
                            return res.status(500).json({
                                message: 'Error when deleting the trip attractions.',
                                error: err
                            });
                        }
                    });
                   
                });

            });

            TripModel.deleteMany({userId: id}, function (err) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when deleting the user\'s trips.',
                        error: err
                    });
                }
            });

            return res.status(204).json();
            });
        });
    },

    /**
     * userController.login()
     */
    login: function (req, res) {
        UserModel.authenticate(req.body.username, req.body.password, function (err, user) {
            if (err || !user) {
                console.error("Login error:", err ? err.message : "Invalid credentials");
                return res.status(401).json({ message: err ? err.message : "Invalid credentials" });
            }
            req.session.userId = user._id;
            return res.json(user);
        });
    },

    /**
     * userController.logout()
     */
    logout: function (req, res, next) {
        if (req.session) {
            req.session.destroy(function (err) {
                if (err) return next(err);
                return res.status(200).json({ message: "Logout successful" });
            });
        } else {
            return res.status(200).json({ message: "No session to destroy" });
        }
    },

    /**
     * userController.profile()
     */
    profile: function (req, res, next) {
        var userId = req.session.userId;

        if (!userId)
            return res.status(401).json({ message: 'Not logged in' });

        UserModel.findById(userId, function (err, user) {
            if (err) {
                console.error("Unexpected error in profile:", err);
                return res.status(500).json({ message: 'Internal server error' });
            }
            if (!user)
                return res.status(404).json({ message: 'User was not found' });

            return res.json({
                username: user.username,
                email: user.email,
                profilePicture: user.profilePicture,
                isAdmin: user.isAdmin
            });
        });
    },

    /**
     * userController.uploadAvatar()
     * posodobi profilno sliko
     */
    uploadProfilePicture: function (req, res) {
        var userId = req.session.userId;
        
        UserModel.findById(userId, function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error getting user',
                    error: err
                });
            }
            if (!user) {
                return res.status(404).json({
                    message: 'User not found'
                });
            }
            if (req.file)
                user.profilePicture = req.file.filename;
    
            user.save(function (err, updatedUser) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error while updating user',
                        error: err
                    });
                }
                return res.json(updatedUser);
            });
        });
    }
};