var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

const CoordinatesSchema = new Schema({
  'lat': Number,
  'lon': Number
}, { _id: false });

const AddressSchema = new Schema({
  'street': String,
  'city': String,
  'postalCode': String,
  'country': String
}, { _id: false });

const OpeningHoursSchema = new Schema({
  'monday': String,
  'tuesday': String,
  'wednesday': String,
  'thursday': String,
  'friday': String,
  'saturday': String,
  'sunday': String
}, { _id: false });

const attractionSchema = new Schema({
  'name': String,
  'regionId': {
    type: Schema.Types.ObjectId,
    ref: 'region'
  },
  'location': CoordinatesSchema,
  'address': AddressSchema,
  'description': String,
  'classification': String,
  'locationType': String,
  'elevation': Number,
  'accessibilityOptions': String,
  'ratingFamilyFriendly': Number,
  'ratingElderlyFriendly': Number,
  'ratingAccessible': Number,
  'rating': Number,
  'requiresReservation': Boolean,
  'openingHours': OpeningHoursSchema,
  'entryFee': Number,
  'googleMapsLink': String,
  'createdAt': Date,
  'verified': Boolean
});

module.exports = mongoose.model('attraction', attractionSchema);