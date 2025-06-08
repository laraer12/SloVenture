var express = require('express');
var router = express.Router();
var userController = require('../controllers/userController.js');

// za posodobitev profilne slike
var multer = require('multer');
var upload = multer({ dest: 'public/images/' });

// CSRF zaščita
const csrf = require('csurf');
const csrfProtection = csrf({ cookie: true });

const jwt = require('jsonwebtoken');

function requiresLogin(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader) {
        return res.status(401).json({ message: 'Niste prijavljeni' });
    }

    const token = authHeader.split(' ')[1]; // Bearer <token>

    jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
        if (err) {
            return res.status(401).json({ message: 'Neveljaven token' });
      }
        req.user = decoded; 
        next();
    });
}

router.get('/getIdByUsername/:username', userController.getIdByUsername);
router.get('/', userController.list);
router.post('/Kotlin', userController.createKotlin);

 // registracija, sedaj dodana csrf zaščita
if (process.env.NODE_ENV === 'test')
    router.post('/', userController.create);

else
    router.post('/', csrfProtection, userController.create);

router.post('/login', userController.login); // prijava
router.get('/profile', requiresLogin, userController.profile); // prikaz profila
router.get('/logout', userController.logout); // odjava
router.get('/:id', userController.show);
router.put('/:id', userController.update);
router.delete('/:id', userController.remove);

router.post('/upload-profile-picture', upload.single('profilePicture'), requiresLogin, userController.uploadProfilePicture); // sprememba profilne slike
router.put('/:id/remove-profile-picture', requiresLogin, userController.removeProfilePicture); // da lahko admin izbriše neprimerno profilno sliko uporabnika

module.exports = router;