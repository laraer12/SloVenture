require('dotenv').config(); 
var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

// CSRF zaščita
const csrf = require('csurf');
const csrfProtection = csrf({ cookie: true });

//test

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
var userVisitRouter=require('./routes/userVisitRoutes');
var weatherDataRouter = require('./routes/weatherDataRoutes');

var app = express();

var cors = require('cors');

var allowedOrigins = ['http://localhost:3000', 'http://localhost:3001', 'http://40.68.129.50:3000', 'http://40.68.129.50:3001', 'http://40.68.129.50', 'http://192.168.12.30:3001', 'http://192.168.178.40:3001', 'http://192.168.1.101:3001'];

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

// sprememba glede na development in test okolje

const mongoose = require('mongoose');
const uri = process.env.NODE_ENV === 'test'
  ? process.env.MONGODB_TEST_URI
  : process.env.MONGODB_URI;

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

const sessionOptions = {
  secret: 'work hard',
  resave: true,
  saveUninitialized: false,
};

// uporabim MongoStore samo, če ni testno okolje
if (process.env.NODE_ENV !== 'test')
  sessionOptions.store = MongoStore.create({ mongoUrl: uri });

// vedno uporabim session middleware
app.use(session(sessionOptions));

// pridobim csrf token
if (process.env.NODE_ENV !== 'test') {
  app.get('/csrf-token', csrfProtection, (req, res) => {
    res.json({ csrfToken: req.csrfToken() });
  });
}
else {
  app.get('/csrf-token', (req, res) => { // v testnem okolju pošljem prazen token ali pa to pot kar ignoriram
    res.json({ csrfToken: '' });
  });
}

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