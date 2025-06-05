process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const UservisitModel = require('../models/userVisitModel');
const AttractionImageModel = require('../models/attractionImageModel');
const UserModel = require('../models/userModel');
const { AttractionModel } = require('../models/attractionModel');

describe('UserVisit API testi', () => {
    let testVisit;
    let testUser;
    let cookie;
    let testAttraction;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));

        await UserModel.deleteMany({});
        await AttractionModel.deleteMany({});
        await UservisitModel.deleteMany({});
        await AttractionImageModel.deleteMany({});

        // testni uporabnik
        testUser = await UserModel.create({
            username: 'testniUporabnik',
            email: 'testni@uporabnik.com',
            password: 'testnoGeslo'
        });

        // ustvarim testno znamenitost
        testAttraction = await AttractionModel.create({
            name: 'Testna znamenitost',
            createdAt: new Date()
        });

        // prijava in shranjevanje cookie-ja
        const loginRes = await request(app)
            .post('/users/login')
            .send({ username: 'testniUporabnik', password: 'testnoGeslo' });

        cookie = loginRes.headers['set-cookie'];
    });

    beforeEach(async () => {
        await UservisitModel.deleteMany({});
        await AttractionImageModel.deleteMany({});

        // ustvarim testni obisk s pravim attractionId
        testVisit = new UservisitModel({
            userId: testUser._id,
            attractionId: testAttraction._id,
            visitDate: new Date('2024-01-01')
        });

        await testVisit.save();
    });

    afterAll(async () => {
        await UserModel.deleteMany({});
        await AttractionModel.deleteMany({});
        await UservisitModel.deleteMany({});
        await AttractionImageModel.deleteMany({});
        await mongoose.connection.close();
    });

    // mora vrniti seznam vseh obiskov
    describe('GET /user-visit', () => {
        it('vrne seznam vseh obiskov', async () => {
            const res = await request(app).get('/user-visit');

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body.length).toBeGreaterThan(0);
        });
    });

    describe('GET /user-visit/:id', () => {
        // mora vrniti en obisk glede na ID
        it('vrne en obisk po ID-ju', async () => {
            const res = await request(app).get(`/user-visit/${testVisit._id}`);

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('userId');
            expect(res.body._id).toBe(testVisit._id.toString());
        });
        
        // mora vrniti napako, če obisk ne obstaja
        it('vrne 404 če obisk ne obstaja', async () => {
            const fakeId = new mongoose.Types.ObjectId();
            const res = await request(app).get(`/user-visit/${fakeId}`);

            expect(res.statusCode).toBe(404);
            expect(res.body.message).toBe('No such userVisit');
        });
    });

    describe('POST /user-visit', () => {

        // mora ustvariti nov obisk
        it('ustvari nov obisk, če ne obstaja', async () => {
            const newVisit = {
                userId: testUser._id,
                attractionId: new mongoose.Types.ObjectId(),
                visitDate: new Date('2024-02-02')
            };

            const res = await request(app)
                .post('/user-visit')
                .set('Cookie', cookie)
                .send(newVisit);

            expect(res.statusCode).toBe(201);
            expect(res.body).toHaveProperty('_id');
            expect(new Date(res.body.visitDate)).toEqual(newVisit.visitDate);
        });

        // ne sme ustvariti obiska, če že obstaja
        it('ne ustvari obiska, če že obstaja', async () => {
            const res = await request(app)
                .post('/user-visit')
                .set('Cookie', cookie)
                .send({
                    userId: testVisit.userId.toString(),
                    attractionId: testVisit.attractionId.toString(),
                    visitDate: testVisit.visitDate.toISOString()
                });

            expect(res.statusCode).toBe(409);
            expect(res.body.message).toBe('Visit already exists');
        });
    });

    // mora vrniti obiske uporabnika z vsemi potrebnimi podatki (če so prisotni)
    describe('GET /user-visit/user/:userId', () => {
        it('vrne obiske uporabnika s pripetimi znamenitostmi in slikami (če obstajajo)', async () => {
            const attractionId = testAttraction._id;

            await AttractionImageModel.create([
                { attractionId, url: 'http://testnaSlika1.jpg' },
                { attractionId, url: 'http://testnaSlika2.jpg' }
            ]);

            const res = await request(app).get(`/user-visit/user/${testUser._id}`);

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body.length).toBeGreaterThan(0);

            const visit = res.body[0];
            expect(visit).toHaveProperty('attractionId');
            expect(visit.attractionId).not.toBeNull();
            expect(visit.attractionId).toHaveProperty('_id');

            expect(visit).toHaveProperty('attractionImages');
            expect(Array.isArray(visit.attractionImages)).toBe(true);

            if (visit.attractionImages.length > 0) {
                expect(visit.attractionImages).toEqual(
                    expect.arrayContaining(['http://testnaSlika1.jpg', 'http://testnaSlika2.jpg'])
                );
            }
        });
    });
});