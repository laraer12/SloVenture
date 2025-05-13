var UserModel = require('../models/userModel.js');

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
        UserModel.findByIdAndRemove(req.params.id, function (err) {
            if (err)
                return res.status(500).json({ message: 'Error when deleting the user.', error: err });
            
            return res.status(204).json();
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
            });
        });
    }
};

// user saved
// user view history
// user visit