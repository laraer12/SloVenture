var CommentModel = require('../models/commentModel.js');

/**
 * commentController.js
 *
 * @description :: Server-side logic for managing comments.
 */
module.exports = {

    /**
     * commentController.list()
     * prikaz čisto vseh komentarjev
     */
    list: function (req, res) {
        CommentModel.find(function (err, comments) {
            if (err) {
                return res.status(500).json({
                    message: 'Error getting comments',
                    error: err
                });
            }
            return res.json(comments);
        });
    },

    /**
     * commentController.listByAttraction()
     * komentarji pod določeno znamenitostjo
     */
    listByAttraction: function (req, res) {
        const attractionId = req.params.attractionId;

        CommentModel.find({ attractionId })
            .populate('userId', 'username') // da bo vidno tudi kdo je komentar objavil
            .sort({ createdAt: -1 }) // sortiram po datumu padajoče
            .exec((err, comments) => {
                if (err) {
                    return res.status(500).json({
                        message: 'Error getting comments',
                        error: err
                    });
                }
                res.json(comments);
            });
    },

    /**
     * commentController.show()
     * prikaz posameznega komentarja
     */
    show: function (req, res) {
        var id = req.params.id;

        CommentModel.findOne({_id: id}, function (err, comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error getting commen',
                    error: err
                });
            }
            if (!comment) {
                return res.status(404).json({
                    message: 'No such comment'
                });
            }
            return res.json(comment);
        });
    },

    /**
     * commentController.create()
     * dodajanje komentarja
     */
    create: function (req, res) {
        if (!req.session || !req.session.userId)
            return res.status(401).json({ message: 'User has to be logged in' });

        const { text } = req.body;
        const { attractionId } = req.params;

        var comment = new CommentModel({
			userId : req.session.userId,
			attractionId,
			text : text.trim(),
			createdAt : Date.now()
        });

        comment.save(function (err, savedComment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating comment',
                    error: err
                });
            }
            savedComment.populate('userId', 'username')
                .then(populated => {
                    res.status(201).json(populated);
                })
                .catch(err => {
                    res.status(201).json(savedComment);
                });
        });
    },

    /**
     * commentController.update()
     * posodobitev komentarja, verjetno ne bo uporabljeno glede na to da ga uporabnik lahko izbriše
     */
    update: function (req, res) {
        var id = req.params.id;

        CommentModel.findOne({_id: id}, function (err, comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting comment',
                    error: err
                });
            }

            if (!comment) {
                return res.status(404).json({
                    message: 'No such comment'
                });
            }

            comment.userId = req.body.userId ? req.body.userId : comment.userId;
			comment.attractionId = req.body.attractionId ? req.body.attractionId : comment.attractionId;
			comment.text = req.body.text ? req.body.text : comment.text;
			comment.createdAt = req.body.createdAt ? req.body.createdAt : comment.createdAt;
			
            comment.save(function (err, comment) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating comment.',
                        error: err
                    });
                }

                return res.json(comment);
            });
        });
    },

    /**
     * commentController.remove()
     * brisanje komentarja
     */
    remove: function (req, res) {
        var id = req.params.id;

        CommentModel.findById(id, function (err, comment) {
            if (err) {
                return res.status(500).json({
                    message: 'Error finding comment',
                    error: err
                });
            }
            if (!comment)
                return res.status(404).json({ message: 'Comment was not found' });

            if (!req.session || !req.session.userId || comment.userId.toString() !== req.session.userId)
                return res.status(403).json({ message: 'Only the owner of this comment can delete it' }); // omejitev da lahko izbriše komentar samo lastnik tega

            comment.remove(function (err) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error deleting comment',
                        error: err
                    });
                }
                return res.status(204).send();
            });
        });
    }
};