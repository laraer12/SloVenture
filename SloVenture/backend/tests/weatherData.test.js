process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const WeatherData = require('../models/weatherDataModel');

describe('WeatherData API testi:', () => {
    let testWeatherData;
    let attractionId = new mongoose.Types.ObjectId().toString();

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));
    });

    beforeEach(async () => {
        await WeatherData.deleteMany({});

        // testni vremenski podatki
        testWeatherData = await new WeatherData({
            attractionId,
            currentWeather: { temperature: 22, condition: 'Sončno' },
            forecast: [
                { date: '2025-06-06', temperature: 25, condition: 'Oblačno' },
                { date: '2025-06-07', temperature: 27, condition: 'Dež' }
            ],
            lastUpdated: new Date(),
            location: { lat: 46.0569, lon: 14.5058 }
        }).save();
    });

    afterAll(async () => {
        await mongoose.connection.close();
    });

    // mora vrniti vse vremenske podatke
    describe('GET /weather-data', () => {
        it('vrne vse vremenske podatke', async () => {
            const res = await request(app).get('/weather-data');

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body.length).toBeGreaterThan(0);
        });
    });

    describe('GET /weather-data/:id', () => {

        // mora vrniti vremenske podatke glede na ID
        it('vrne vremenske podatke po ID', async () => {
            const res = await request(app).get(`/weather-data/${testWeatherData._id}`);

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('attractionId', attractionId);
        });

        // mora vrniti napako če ID ne obstaja
        it('vrne 404, če ID ne obstaja', async () => {
            const res = await request(app).get('/weather-data/000000000000000000000000');
            expect(res.statusCode).toBe(404);
        });
    });

    describe('GET /weather-data/attraction/:attractionId', () => {

        // mora vrniti vremenske podatke za dan ID znamenitosti
        it('vrne vremenske podatke za dano attractionId', async () => {
            const res = await request(app).get(`/weather-data/by-attraction/${attractionId}`);

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('attractionId', attractionId);
        });

        // mora vrniti napako, če ID znamenitosti ne obstaja
        it('vrne 404, če attractionId ne obstaja', async () => {
            const fakeId = new mongoose.Types.ObjectId().toString();
            const res = await request(app).get(`/weather-data/by-attraction/${fakeId}`);
            
            expect(res.statusCode).toBe(404);
        });
    });

    describe('POST /weather-data', () => {

        // mora ustvariti nove vremenske podatke
        it('ustvari nove vremenske podatke', async () => {
            const newAttractionId = new mongoose.Types.ObjectId().toString();
            const res = await request(app)
                .post('/weather-data')
                .send({
                    attractionId: newAttractionId,
                    currentWeather: { temperature: 18, condition: 'Deževno' },
                    forecast: [
                        { date: '2025-06-08', temperature: 19, condition: 'Oblačno' }
                    ],
                    lastUpdated: new Date(),
                    location: { lat: 45.0, lon: 15.0 }
                });

            expect(res.statusCode).toBe(201);
            expect(res.body).toHaveProperty('attractionId', newAttractionId);
        });

        // ne sme ustvariti, če podatki manjkajo
        it('ne ustvari, če manjkajo podatki', async () => {
            const res = await request(app).post('/weather-data').send({});
            expect(res.statusCode).toBe(400); // popravljen status iz 500 v 400
        });
    });
    
    describe('DELETE /weather-data/:id', () => {

        // mora izbrisati vse vremenske podatke
        it('izbriše vremenske podatke', async () => {
            const res = await request(app).delete(`/weather-data/${testWeatherData._id}`);
            expect(res.statusCode).toBe(204);
        });

        // mora vrniti napako, če podatki ne obstajajo
        it('vrne 404, če podatki ne obstajajo', async () => {
            const res = await request(app).delete('/weather-data/000000000000000000000000');
            expect(res.statusCode).toBe(404); // popravljen iz 500/404 na samo 404
        });
    });
});