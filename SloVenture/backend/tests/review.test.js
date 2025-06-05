process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const Review = require('../models/reviewModel');
const User = require('../models/userModel');
const { AttractionModel } = require('../models/attractionModel');

describe('Review API testi:', () => {
    let testUser;
    let testAttraction;
    let testReview;
    let cookie;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));

        await User.deleteMany({});
        await Review.deleteMany({});
        await AttractionModel.deleteMany({});

        // testni uporabnik
        testUser = await User.create({
            username: 'testniUporabnik',
            email: 'testni@uporabnik.com',
            password: 'testnoGeslo'
        });

        // testna znamenitost
        testAttraction = await AttractionModel.create({
            name: 'Testna znamenitost',
            createdAt: new Date()
        });

        const fakeIo = { emit: jest.fn() };
        app.set('io', fakeIo);

        // prijava in shranim cookie
        const loginRes = await request(app)
            .post('/users/login')
            .send({ username: 'testniUporabnik', password: 'testnoGeslo' });

        cookie = loginRes.headers['set-cookie'];
    });

    beforeEach(async () => {
        await Review.deleteMany({});

        // testni review (ocena)
        testReview = await Review.create({
            userId: testUser._id,
            attractionId: testAttraction._id,
            rating: 4,
            ratingFamilyFriendly: 5,
            ratingElderlyFriendly: 3,
            ratingAccessible: 4,
            createdAt: new Date()
        });
    });

    afterAll(async () => {
        await mongoose.connection.close();
    });

    // mora vrniti vse ocene
    describe('GET /reviews', () => {
        it('vrne vse ocene', async () => {
            const res = await request(app).get('/reviews');
            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
        });
    });

    describe('GET /reviews/:id', () => {

        // mora vrniti eno oceno glede na ID
        it('vrne eno oceno po ID-ju', async () => {
            const res = await request(app).get(`/reviews/${testReview._id}`);
            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('_id', testReview._id.toString());
        });

        // mora vrniti napako če ocena ne obstaja
        it('vrne 404 za neobstoječo oceno', async () => {
            const fakeId = new mongoose.Types.ObjectId();
            const res = await request(app).get(`/reviews/${fakeId}`);
            expect(res.statusCode).toBe(404);
        });
    });

    describe('POST /reviews', () => {

        // mora ustvariti novo oceno in obstoječo posodobiti
        it('ustvari novo oceno ali posodobi obstoječo', async () => {
            const reviewData = {
                userId: testUser._id,
                attractionId: testAttraction._id,
                rating: 5,
                ratingFamilyFriendly: 4,
                ratingElderlyFriendly: 4,
                ratingAccessible: 5
            };

            const res = await request(app)
                .post('/reviews')
                .set('Cookie', cookie)
                .send(reviewData);

            expect(res.statusCode).toBe(200);
            expect(res.body.review).toHaveProperty('userId', reviewData.userId.toString());
        });

        // ne sme, če manjkajo vrednosti
        it('ne uspe, če manjkajo ocene', async () => {
            const res = await request(app)
                .post('/reviews')
                .set('Cookie', cookie)
                .send({
                    userId: testUser._id,
                    attractionId: testAttraction._id
                });

            expect(res.statusCode).toBe(400);
        });
    });

    describe('PUT /reviews/:id', () => {

        // mora posodobiti oceno
        it('posodobi oceno', async () => {
            const res = await request(app)
                .put(`/reviews/${testReview._id}`)
                .set('Cookie', cookie)
                .send({ rating: 2 });

            expect(res.statusCode).toBe(200);
            expect(res.body.rating).toBe(2);
        });

        // vrne napako, če ne najde ocene
        it('vrne 404, če ocene ni mogoče najti', async () => {
            const fakeId = new mongoose.Types.ObjectId();
            const res = await request(app)
                .put(`/reviews/${fakeId}`)
                .set('Cookie', cookie)
                .send({ rating: 3 });

            expect(res.statusCode).toBe(404);
        });
    });

    // mora izbrisati oceno
    describe('DELETE /reviews/:id', () => {
        it('izbriše oceno', async () => {
            const res = await request(app)
                .delete(`/reviews/${testReview._id}`)
                .set('Cookie', cookie);

            expect(res.statusCode).toBe(204);
        });
    });

    describe('GET /reviews/averages/:attractionId', () => {

        // mora vrniti povprečja ocen za znamenitost
        it('vrne povprečja ocen za znamenitost', async () => {
            const res = await request(app).get(`/reviews/averages/${testAttraction._id}`);
            expect(res.statusCode).toBe(200);
            expect(res.body.averages).toHaveProperty('rating');
        });

        // mora vrniti prazen seznam, če ni ocen
        it('vrne prazen seznam, če ni ocen', async () => {
            await Review.deleteMany({});
            const res = await request(app).get(`/reviews/averages/${testAttraction._id}`);
            expect(res.statusCode).toBe(200);
            expect(res.body).toEqual({
                averages: null,
                reviews: []
            });
        });
    });
});