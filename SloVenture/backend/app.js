require('dotenv').config(); // s tem lahko uporabim ključe iz .env kjerkoli
var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

// CSRF zaščita
const csrf = require('csurf');
const csrfProtection = csrf({ cookie: true });

/*
// lokalna povezava z bazo
var mongoose = require('mongoose');
var mongoDB='mongodb://127.0.0.1:27017/SloVentureDB';
mongoose.set('strictQuery', true);
mongoose.connect(mongoDB);
mongoose.Promise = global.Promise;
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error:'));
*/

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/userRoutes');
var attractionImageRouter = require('./routes/attractionImageRoutes');
var attractionRouter = require('./routes/attractionRoutes');
var commentRouter = require('./routes/commentRoutes');
var nearbyAttractionRouter = require('./routes/nearbyAttractionRoutes');
var regionRouter = require('./routes/regionRoutes');
var reviewRouter = require('./routes/reviewRoutes');
var tripAttractionRouter = require('./routes/tripAttractionRoutes');
var tripRouter = require('./routes/tripRoutes');
var userSavedRouter = require('./routes/userSavedRoutes');
var userVisitRouter=require('./routes/userVisitRoutes');
var weatherDataRouter = require('./routes/weatherDataRoutes');

var app = express();

var cors = require('cors');

var allowedOrigins = ['http://localhost:3000', 'http://localhost:3001'];

app.use(cors({
  credentials: true,
  origin: function (origin, callback) {
    if (!origin)
      return callback(null, true);

    if (allowedOrigins.includes(origin))
      return callback(null, true);
    
    else
      return callback(new Error('Not allowed by CORS'), false);
  }
}));

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

const mongoose = require('mongoose');
const uri = "mongodb+srv://laraerzar:4899raimzi*so12@sloventure.4djf5rv.mongodb.net/SloVentureDB?retryWrites=true&w=majority&appName=SloVenture";

const clientOptions = { serverApi: { version: '1', strict: true, deprecationErrors: true } };

async function run() {
  try {
    await mongoose.connect(uri, clientOptions);
    await mongoose.connection.db.admin().command({ ping: 1 });
    console.log("Pinged your deployment. You successfully connected to MongoDB!");
  }
  catch (error) {
    console.error("Database connection error:", error);
  }
}
run().catch(console.dir);

// test za session
var session = require('express-session');
var MongoStore = require('connect-mongo');
app.use(session({
  secret: 'work hard',
  resave: true,
  saveUninitialized: false,
  store: MongoStore.create({mongoUrl: uri})
}));

// pridobim csrf token
app.get('/csrf-token', csrfProtection, (req, res) => {
  res.json({ csrfToken: req.csrfToken() });
});

app.use(function (req, res, next) {
  res.locals.session = req.session;
  next();
});

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/attraction-images', attractionImageRouter);
app.use('/attractions', attractionRouter);
app.use('/comments', commentRouter);
app.use('/nearby-attractions', nearbyAttractionRouter);
app.use('/regions', regionRouter);
app.use('/reviews', reviewRouter);
app.use('/trip-attractions', tripAttractionRouter);
app.use('/trips', tripRouter);
app.use('/user-saved', userSavedRouter);
app.use('/user-visit', userVisitRouter);
app.use('/weather-data', weatherDataRouter);

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
