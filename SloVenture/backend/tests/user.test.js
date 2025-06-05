process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const UserModel = require('../models/userModel');

jest.mock('axios'); // mock za axios, ki uporablja recaptcha v create
const axios = require('axios');

describe('User API testi:', () => {
    let testUser;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));
    });

    beforeEach(async () => {
        await UserModel.deleteMany({}); // pobrišem vse uporabnike pred vsakim testom

        // ustvarim test uporabnika za prijavo in ostalo
        testUser = new UserModel({
            username: 'testniUporabnik',
            email: 'testni@uporabnik.com',
            password: 'testnoGeslo',
            isAdmin: false
        });

        await testUser.save();
    });

    afterAll(async () => {
        await UserModel.deleteMany({});
        await mongoose.connection.close();
    });

    // mora vrniti seznam uporabnikov
    describe('GET /users', () => {
        it('vrne seznam uporabnikov', async () => {
            const res = await request(app).get('/users');

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body.length).toBeGreaterThan(0);
            expect(res.body[0]).toHaveProperty('username', 'testniUporabnik');
        });
    });

    // mora vrniti uporabnika po ID-ju
    describe('GET /users/:id', () => {
        it('vrne uporabnika po ID-ju', async () => {
            const res = await request(app).get(`/users/${testUser._id}`);

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('username', 'testniUporabnik');
            expect(res.body._id).toBe(testUser._id.toString());
        });

        it('vrne 404 če uporabnik ne obstaja', async () => {
            const fakeId = new mongoose.Types.ObjectId();
            const res = await request(app).get(`/users/${fakeId}`);

            expect(res.statusCode).toBe(404);
            expect(res.body.message).toBe('No such user');
        });
    });

    describe('POST /users', () => {
        beforeEach(() => {
            axios.post.mockResolvedValue({ data: { success: true } }); // ponaredim recaptcha uspešen odgovor
        });

        // mora ustvariti novega uporabnika
        it('ustvari novega uporabnika, če so podatki pravilni in captcha uspešna', async () => {
            const newUser = {
                username: 'noviUporabnik',
                email: 'novi@uporabnik.com',
                password: 'novoGeslo',
                captchaToken: 'fake-token'
            };

            const res = await request(app).post('/users').send(newUser);

            expect(res.statusCode).toBe(201);
            expect(res.body).toHaveProperty('_id');
            expect(res.body.username).toBe('noviUporabnik');
        });

        // ne sme ustvariti uporabnika brez tokena
        it('ne ustvari uporabnika brez captcha tokena', async () => {
            const res = await request(app).post('/users').send({
                username: 'noviUporabnik',
                email: 'novi@uporabnik.com',
                password: 'novoGeslo'
            });

            expect(res.statusCode).toBe(400);
            expect(res.body.message).toMatch(/Vsa polja morajo biti izpolnjena/);
        });

        // ne sme ustvariti uporabnika če captcha ne uspe
        it('ne ustvari uporabnika, če captcha ni uspešna', async () => {
            axios.post.mockResolvedValueOnce({ data: { success: false } });

            const res = await request(app).post('/users').send({
                username: 'noviUporabnik',
                email: 'novi@uporabnik.com',
                password: 'novoGeslo',
                captchaToken: 'fake-token'
            });

            expect(res.statusCode).toBe(400);
            expect(res.body.message).toMatch(/Error verifying reCAPTCHA/);
        });

        // ne ustvari uporabnika, če tak že obstaja
        it('ne ustvari uporabnika, če uporabnik z istim username ali email že obstaja', async () => {
            const res = await request(app).post('/users').send({
                username: 'testniUporabnik',
                email: 'testni@uporabnik.com',
                password: 'testnoGeslo',
                captchaToken: 'fake-token'
            });

            expect(res.statusCode).toBe(400);
            expect(res.body.message).toMatch(/exists/);
        });
    });

    describe('POST /users/login', () => {

        // mora pravilno prijaviti uporabnika
        it('pravilno prijavi uporabnika', async () => {
            const res = await request(app).post('/users/login').send({
                username: 'testniUporabnik',
                password: 'testnoGeslo'
            });

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('username', 'testniUporabnik');
        });

        // ne sme prijaviti uporabnika z napačnim geslom
        it('ne uspe prijava z napačnim geslom', async () => {
            const res = await request(app).post('/users/login').send({
                username: 'testniUporabnik',
                password: 'napacnoGeslo'
            });

            expect(res.statusCode).toBe(401);
            expect(res.body.message).toMatch(/Wrong password/);
        });
    });
});