process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const User = require('../models/userModel');
const Trip = require('../models/tripModel');
const TripAttraction = require('../models/tripAttractionModel');
const { AttractionModel } = require('../models/attractionModel');
const AttractionImage = require('../models/attractionImageModel');

describe('Trip API testi:', () => {
    let testUser;
    let testAttraction;
    let testTrip;
    let token;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));

        await User.deleteMany({});
        await Trip.deleteMany({});
        await TripAttraction.deleteMany({});
        await AttractionModel.deleteMany({});
        await AttractionImage.deleteMany({});

        // testni uporabnik
        testUser = await User.create({
            username: 'izletUporabnik',
            email: 'izlet@uporabnik.com',
            password: 'izletGeslo'
        });

        const loginRes = await request(app)
            .post('/users/login')
            .send({ username: 'izletUporabnik', password: 'izletGeslo' });

        token = loginRes.body.token;

        // testna znamenitost
        testAttraction = await AttractionModel.create({
            name: 'Testna znamenitost',
            description: 'Testni opis',
            category: 'Testna kultura',
            createdAt: new Date()
        });

        // testni izlet
        testTrip = await Trip.create({
            userId: testUser._id,
            tripName: 'Moj testni izlet',
            tripDescription: 'Opis testnega izleta',
            startDate: new Date(),
            endDate: new Date(),
            isPublic: true,
            createdAt: new Date()
        });

        await TripAttraction.create({
            tripId: testTrip._id,
            attractionId: testAttraction._id,
            order: 0
        });

        await AttractionImage.create({
            attractionId: testAttraction._id,
            url: 'https://example.com/slika.jpg'
        });

        app.set('io', { emit: jest.fn() });
    });

    afterAll(async () => {
        await mongoose.connection.close();
    });

    // mora vrniti vse izlete prijavljenega uporabnika
    describe('GET /trips', () => {
        it('vrne vse izlete za prijavljenega uporabnika', async () => {
            const res = (await request(app).get('/trips').set('Authorization', `Bearer ${token}`));

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body[0]).toHaveProperty('tripName', 'Moj testni izlet');
        });
    });

    describe('GET /trips/:id', () => {

        // mora vrniti vse podrobnosti izleta
        it('vrne podrobnosti izleta in pripadajoče znamenitosti', async () => {
            const res = await request(app)
                .get(`/trips/${testTrip._id}`)
                .set('Authorization', `Bearer ${token}`);

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('trip');
            expect(res.body.trip.tripName).toBe('Moj testni izlet');
            expect(Array.isArray(res.body.attractions)).toBe(true);
        });

        // mora vrniti napako če izlet ne obstaja
        it('vrne 404 če izlet ne obstaja', async () => {
            const fakeId = new mongoose.Types.ObjectId();
            const res = await request(app).get(`/trips/${fakeId}`).set('Authorization', `Bearer ${token}`);

            expect(res.statusCode).toBe(404);
        });
    });

    describe('POST /trips', () => {

        // mora ustvariti nov izlet
        it('ustvari nov izlet', async () => {
            const newTrip = {
                userId: testUser._id,
                tripName: 'Nov testni izlet',
                tripDescription: 'Nov testni opis',
                startDate: new Date(),
                endDate: new Date(),
                isPublic: false,
                createdAt: new Date()
            };

            const res = await request(app)
                .post('/trips')
                .set('Authorization', `Bearer ${token}`)
                .send(newTrip);

            expect(res.statusCode).toBe(201);
            expect(res.body.tripName).toBe('Nov testni izlet');
        });

        // mora vrniti napako, če podatki manjkajo
        it('vrne napako, če manjkajo podatki', async () => {
            const res = await request(app).post('/trips').set('Authorization', `Bearer ${token}`).send({});

            expect(res.statusCode).toBe(400);
        });
    });

    describe('PUT /trips/:id', () => {

        // mora posodobiti izlet
        it('posodobi izlet', async () => {
            const res = await request(app)
                .put(`/trips/${testTrip._id}`)
                .set('Authorization', `Bearer ${token}`)
                .send({ tripName: 'Posodobljen izlet' });

            expect(res.statusCode).toBe(200);
            expect(res.body.tripName).toBe('Posodobljen izlet');
        });

        // mora vrniti napako, če izlet ne obstaja
        it('vrne 404 če izlet ne obstaja', async () => {
            const fakeId = new mongoose.Types.ObjectId();
            const res = await request(app)
                .put(`/trips/${fakeId}`)
                .set('Authorization', `Bearer ${token}`)
                .send({ tripName: 'Ne obstaja' });
                
            expect(res.statusCode).toBe(404);
        });
    });

    // mora izbrisati izlet
    describe('DELETE /trips/:id', () => {
        it('zbriše izlet', async () => {
            const newTrip = await Trip.create({
                userId: testUser._id,
                tripName: 'Za brisanje',
                tripDescription: '',
                startDate: new Date(),
                endDate: new Date(),
                isPublic: true,
                createdAt: new Date()
            });

            const res = await request(app)
                .delete(`/trips/${newTrip._id}`)
                .set('Authorization', `Bearer ${token}`);

            expect(res.statusCode).toBe(204);
        });
    });

    // mora vrniti vse izlete uporabnika z ustreznimi podatki
    describe('GET /trips/user/:userId', () => {
        it('vrne vse izlete uporabnika z znamenitostmi in prvo sliko', async () => {
            const res = await request(app)
                .get(`/trips/user/${testUser._id}`)
                .set('Authorization', `Bearer ${token}`);

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body[0]).toHaveProperty('firstImageUrl');
        });
    });
});