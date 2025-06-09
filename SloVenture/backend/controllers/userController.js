const fs = require('fs');
const path = require('path');

var UserModel = require('../models/userModel.js');
var TripModel = require('../models/tripModel.js');
var TripAttractionModel = require('../models/tripAttractionModel.js');

var axios = require('axios');

var multer = require('multer'); // za objavo datotek
const { getFormLabelUtilityClasses } = require('@mui/material');
const jwt = require('jsonwebtoken');

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

    getAdmins: function (req, res) {
        UserModel.find({ isAdmin: true }, 'username _id', function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting admin usernames.',
                    error: err
                });
            }

            const result = users.map(user => ({
                id: user._id,
                username: user.username
            }));

            return res.json(result);
        });
    },

    getIdByUsername: function (req, res) {
        UserModel.findOne({ username: req.params.username }, function (err, user) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting user by username.',
                    error: err
                });
            }
            if (!user)
                return res.status(404).json({ message: 'No such user' });
            
            return res.json(user);
        });
    },


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
        var { username, email, password, captchaToken, isFakeData } = req.body;

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
                    password: req.body.password,
                    isAdmin: false,
                    isFakeData: isFakeData || false,
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

    createKotlin: function (req, res) {
        const { username, email, password, isAdmin, profilePicture, isFakeData } = req.body;

        if (!username || !email || !password) {
            return res.status(400).json({ message: 'All fields are required!' });
        }

        UserModel.findOne({ $or: [{ username }, { email }] }, function (err, existingUser) {
            if (err)
                return res.status(500).json({ message: 'Error checking user existence', error: err });

            if (existingUser)
                return res.status(400).json({ message: 'Username or email already exists' });

            const user = new UserModel({
                username,
                email,
                password,
                isAdmin,
                profilePicture,
                isFakeData
            });

            user.save(function (err, savedUser) {
                if (err)
                    return res.status(500).json({ message: 'Error creating user', error: err });

                return res.status(201).json(savedUser);
            });
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
            user.isAdmin = req.body.isAdmin;
            user.profilePicture = req.body.profilePicture || user.profilePicture;
            user.isFakeData = req.body.isFakeData !== undefined ? req.body.isFakeData : user.isFakeData;


            user.save(function (err, updatedUser) {
                if (err)
                    return res.status(500).json({ message: 'Error when updating user', error: err });
                
                return res.json(updatedUser);
            });
        });
    },

    remove: function (req, res) {
        const targetUserId = req.params.id;

        UserModel.findByIdAndRemove(targetUserId, function (err, deletedUser) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the user.',
                    error: err
                });
            }

            TripModel.find({ userId: targetUserId }, function (err, trips) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when getting the user\'s trips.',
                        error: err
                    });
                }

                if (trips.length === 0) {
                    return TripModel.deleteMany({ userId: targetUserId }, function (err) {
                        if (err) {
                            return res.status(500).json({
                                message: 'Error when deleting trips.',
                                error: err
                            });
                        }
                        return res.sendStatus(204);
                    });
                }

                let deletedAttractionsCount = 0;
                let hasError = false;

                trips.forEach(function (trip) {
                    TripAttractionModel.deleteMany({ tripId: trip._id }, function (err) {
                        if (err && !hasError) {
                            hasError = true;
                            return res.status(500).json({
                                message: 'Error when deleting trip attractions.',
                                error: err
                            });
                        }

                        deletedAttractionsCount++;
                        if (deletedAttractionsCount === trips.length && !hasError) {
                            TripModel.deleteMany({ userId: targetUserId }, function (err) {
                                if (err) {
                                    return res.status(500).json({
                                        message: 'Error when deleting user\'s trips.',
                                        error: err
                                    });
                                }
                                return res.sendStatus(204);
                            });
                        }
                    });
                });
            });
        });
    },



    /**
     * userController.login()
     */
    login: function (req, res) {
        UserModel.authenticate(req.body.username, req.body.password, function (err, user) {
            if (err || !user)
                return res.status(401).json({ message: err ? err.message : "Invalid credentials" });
            
            const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, { expiresIn: '1h' });
            return res.json({token: token, user: user});
        });
    },

    /**
     * userController.logout()
     */
    logout: function (req, res, next) {
        if (req.user) {
            req.user.destroy(function (err) {
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
        var userId = req.user.userId;

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
        var userId = req.user.userId;
        
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
    },

    removeProfilePicture: function(req, res) {
        const userId = req.params.id; // ID uporabnika, ki mu resetiram sliko
        const currentUserId = req.user.userId;

        if (!currentUserId)
            return res.status(401).json({ message: 'Not logged in' });

        UserModel.findById(currentUserId, function(err, currentUser) {
            if (err || !currentUser)
                return res.status(500).json({ message: 'Error verifying user identity' });

            if (!currentUser.isAdmin)
                return res.status(403).json({ message: 'Access denied: Admin only' });

            UserModel.findById(userId, function(err, user) {
                if (err || !user)
                    return res.status(404).json({ message: 'User not found' });

                // če je profilna slika že default, ne nardim nič
                if (user.profilePicture === 'default-profile-picture.jpg')
                    return res.json({ message: 'Profile picture is already default', user });

                // če trenutna profilna slika ni default, jo poskušam izbrisati iz diska
                const imagePath = path.join(__dirname, '..', 'public', 'images', user.profilePicture);

                fs.unlink(imagePath, function(err) {
                    if (err && err.code !== 'ENOENT')
                        return res.status(500).json({ message: 'Error deleting profile picture file', error: err });

                    // po brisanju datoteke ali če datoteka ni obstajala, nastavim default in shranim
                    user.profilePicture = 'default-profile-picture.jpg';

                    user.save(function(err, updatedUser) {
                        if (err)
                            return res.status(500).json({ message: 'Error updating user', error: err });

                        return res.json({ message: 'Profile picture reset to default', user: updatedUser });
                    });
                });
            });
        });
    }
};