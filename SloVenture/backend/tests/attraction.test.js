process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo
const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const {AttractionModel} = require('../models/attractionModel');

describe('Attraction API testi', () => {
    let testAttraction;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));
    });

    beforeEach(async () => {
        await AttractionModel.deleteMany({});

        testAttraction = await new AttractionModel({
            name: 'Testna Znamenitost',
            regionId: new mongoose.Types.ObjectId(),
        }).save();
    });

    afterAll(async () => {
        await mongoose.connection.close();
    });

    describe('GET /attractions/:id', () => {
        it('vrne znamenitost s pravilnim ID-jem', async () => {
            const res = await request(app).get(`/attractions/${testAttraction._id}`);
            expect(res.statusCode).toBe(200);
            expect(res.body.attraction._id).toBe(testAttraction._id.toString());
            expect(res.body.attraction.name).toBe('Testna Znamenitost');
        });
    });

    describe('GET /attractions', () => {
        it('vrne seznam znamenitosti', async () => {
            const res = await request(app).get('/attractions');
            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body[0]).toHaveProperty('attraction');
            expect(res.body[0]).toHaveProperty('images');
        });
    });

    describe('PUT /attractions/:id', () => {
        it('posodobi znamenitost s pravilnim ID-jem', async () => {
            const res = await request(app)
                .put(`/attractions/${testAttraction._id}`)
                .send({
                    name: 'Posodobljena Znamenitost',
                    regionId: testAttraction.regionId,
                });
            expect(res.statusCode).toBe(200);
            expect(res.body._id).toBe(testAttraction._id.toString());
            expect(res.body.name).toBe('Posodobljena Znamenitost');
        });

        it('vrne 404, če znamenitost ne obstaja', async () => {
            const nonExistentId = new mongoose.Types.ObjectId();
            const res = await request(app)
                .put(`/attractions/${nonExistentId}`)
                .send({
                    name: 'Posodobljena Znamenitost',
                    regionId: new mongoose.Types.ObjectId(),
                });
            expect(res.statusCode).toBe(404);
            expect(res.body.message).toBe('No such attraction');
        });
    });

    describe('DELETE /attractions/:id', () => {
        it('izbriše znamenitost s pravilnim ID-jem', async () => {
            const res = await request(app).delete(`/attractions/${testAttraction._id}`);
            expect(res.statusCode).toBe(204);
        });

        it('vrne 404, če znamenitost ne obstaja', async () => {
            const nonExistentId = new mongoose.Types.ObjectId();
            const res = await request(app).delete(`/attractions/${nonExistentId}`);
            expect(res.statusCode).toBe(404);
        });
    });
});