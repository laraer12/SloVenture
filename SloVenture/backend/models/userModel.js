var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var bcrypt = require('bcrypt');

var userSchema = new Schema({
	'username' : String,
	'email' : String,
	'password' : String,
	'profilePicture' : { type: String, default: 'default-profile-picture.jpg' }, // če uporabnik še ni dodal svoje profilne slike se prikaže privzeta
	'isAdmin' : Boolean,
	'createdAt' : Date
});

userSchema.pre('save', function (next) {
	var user = this;

	if (!user.isModified('password'))
		return next();

	bcrypt.hash(user.password, 10, function (err, hashedPassword) {
		if (err) {
			console.error('Error while hashing password:', err);
			return next(err);
		}
		user.password = hashedPassword;
		next();
	});
});

userSchema.statics.authenticate = function (username, password, callback) {
	this.findOne({ username: username }, function (err, user) {
		if (err) {
			console.error('Error during authentication (findOne):', err);
			return callback(err);
		}
		if (!user)
			return callback(new Error("User not found"));

		bcrypt.compare(password, user.password, function (err, result) {
			if (err) {
				console.error('Error during password comparison:', err);
				return callback(err);
			}
			if (result)
				return callback(null, user);
			
            else
				return callback(new Error("Wrong password"));
		});
	});
};

module.exports = mongoose.model('user', userSchema);