process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const RegionModel = require('../models/regionModel');

describe('Region API testi:', () => {
    let testRegion;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));
    });

    beforeEach(async () => {
        await RegionModel.deleteMany({});

        // testna regija
        testRegion = await new RegionModel({
            name: 'Testna regija',
            location: [{ lat: 46.3, lon: 14.1 }]
        }).save();
    });

    afterAll(async () => {
        await mongoose.connection.close();
    });

    // mora vrniti vse regije
    describe('GET /regions', () => {
        it('vrne vse regije', async () => {
            const res = await request(app).get('/regions');
            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body[0]).toHaveProperty('name', 'Testna regija');
            expect(res.body[0].location[0]).toMatchObject({ lat: 46.3, lon: 14.1 });
        });
    });

    describe('GET /regions/:id', () => {

        // mora vrniti regijo glede na ID
        it('vrne regijo po ID', async () => {
            const res = await request(app).get(`/regions/${testRegion._id}`);
            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('name', 'Testna regija');
            expect(res.body.location[0]).toMatchObject({ lat: 46.3, lon: 14.1 });
        });

        // ne sme vrniti regije če ID ne obstaja
        it('vrne 404 za neobstoječ ID', async () => {
            const res = await request(app).get('/regions/000000000000000000000000');
            expect(res.statusCode).toBe(404);
        });
    });

    // mora ustvariti novo regijo
    describe('POST /regions', () => {
        it('ustvari novo regijo', async () => {
            const regionData = {
                name: 'Druga testna regija',
                location: [{ lat: 45.5, lon: 13.7 }]
            };

            const res = await request(app).post('/regions').send(regionData);
            expect(res.statusCode).toBe(201);
            expect(res.body).toHaveProperty('name', 'Druga testna regija');
            expect(res.body.location[0]).toMatchObject({ lat: 45.5, lon: 13.7 });
        });
    });

    describe('PUT /regions/:id', () => {

        // mora posodobiti regijo
        it('posodobi regijo', async () => {
            const res = await request(app)
                .put(`/regions/${testRegion._id}`)
                .send({
                    name: 'Tretja testna regija',
                    location: [{ lat: 46.4, lon: 14.2 }]
                });

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('name', 'Tretja testna regija');
            expect(res.body.location[0]).toMatchObject({ lat: 46.4, lon: 14.2 });
        });

        // mora vrniti napako če regija ne obstaja
        it('vrne 404 za neobstoječo regijo', async () => {
            const res = await request(app)
                .put('/regions/000000000000000000000000')
                .send({ name: 'Nova' });

            expect(res.statusCode).toBe(404);
        });
    });

    describe('DELETE /regions/:id', () => {

        // mora izbrisati regijo
        it('izbriše regijo', async () => {
            const res = await request(app).delete(`/regions/${testRegion._id}`);
            expect(res.statusCode).toBe(204);
        });

        // ne sme izbrisati regije če ID ne obstaja
        it('ne uspe za neobstoječ ID', async () => {
            const res = await request(app).delete('/regions/000000000000000000000000');
            expect(res.statusCode).toBe(204); // še vedno success, lahko spremeniš obnašanje
        });
    });
});