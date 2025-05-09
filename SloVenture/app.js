require('dotenv').config(); // s tem lahko uporabim API ključ kjerkoli
var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var mongoose = require('mongoose');
<<<<<<< Updated upstream
var mongoDB='mongodb://127.0.0.1:27017/SloVenture';
=======
var mongoDB='mongodb://127.0.0.1:27017/SloVentureDB';
>>>>>>> Stashed changes
mongoose.set('strictQuery', true);
mongoose.connect(mongoDB);
mongoose.Promise = global.Promise;
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error:'));

<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
var indexRouter = require('./routes/index');
var usersRouter = require('./routes/userRoutes');
var attractionImageRouter = require('./routes/attractionImageRoutes');
var attractionRouter = require('./routes/attractionRoutes');
var commentRouter = require('./routes/commentRoutes');
var nearbyAccommodationRouter = require('./routes/nearbyAccommodationRoutes');
var nearbyAttractionRouter = require('./routes/nearbyAttractionRoutes');
var regionRouter = require('./routes/regionRoutes');
var reviewRouter = require('./routes/reviewRoutes');
var tripAttractionRouter = require('./routes/tripAttractionRoutes');
var tripRouter = require('./routes/tripRoutes');
var userSavedRouter = require('./routes/userSavedRoutes');
var userViewHistoryRouter = require('./routes/userViewHistoryRoutes');
var userVisitRouter=require('./routes/userVisitRoutes');
var weatherDataRouter = require('./routes/weatherDataRoutes');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/attraction-images', attractionImageRouter);
app.use('/attractions', attractionRouter);
app.use('/comments', commentRouter);
app.use('/nearby-accommodation', nearbyAccommodationRouter);
app.use('/nearby-attractions', nearbyAttractionRouter);
app.use('/regions', regionRouter);
app.use('/reviews', reviewRouter);
app.use('/trip-attractions', tripAttractionRouter);
app.use('/trips', tripRouter);
app.use('/user-saved', userSavedRouter);
app.use('/user-view-history', userViewHistoryRouter);
app.use('/user-visit', userVisitRouter);
app.use('/weather', weatherDataRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
