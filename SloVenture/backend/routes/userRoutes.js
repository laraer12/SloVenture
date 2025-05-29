var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');

// za posodobitev profilne slike
var multer = require('multer');
var upload = multer({ dest: 'public/images/' });

// CSRF zaščita
const csrf = require('csurf');
const csrfProtection = csrf({ cookie: true });

router.get('/', userController.list);
router.post('/', csrfProtection, userController.create); // registracija, sedaj dodana csrf zaščita
router.post('/login', userController.login); // prijava
router.get('/profile', userController.profile); // prikaz profila
router.get('/logout', userController.logout); // odjava
router.get('/:id', userController.show);
router.put('/:id', userController.update);
router.delete('/:id', userController.remove);

router.post('/upload-profile-picture', upload.single('profilePicture'), userController.uploadProfilePicture); // sprememba profilne slike
router.put('/:id/remove-profile-picture', userController.removeProfilePicture); // da lahko admin izbriše neprimerno profilno sliko uporabnika

module.exports = router;