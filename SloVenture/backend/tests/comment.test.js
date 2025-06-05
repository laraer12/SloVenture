process.env.NODE_ENV = 'test'; // testno okolje, da se povežem s testno bazo

const request = require('supertest');
const app = require('../app');
const mongoose = require('mongoose');
const CommentModel = require('../models/commentModel');
const UserModel = require('../models/userModel');
const { AttractionModel } = require('../models/attractionModel');

describe('Comment API testi:', () => {
    let testUser;
    let testAttraction;
    let cookie;

    beforeAll(async () => {
        if (mongoose.connection.readyState !== 1)
            await new Promise(resolve => mongoose.connection.once('open', resolve));

        const fakeIo = { emit: jest.fn() };
        app.set('io', fakeIo);
    });

    beforeEach(async () => {
        await Promise.all([
            CommentModel.deleteMany({}),
            UserModel.deleteMany({}),
            AttractionModel.deleteMany({})
        ]);

        // testni uporabnik
        testUser = await new UserModel({
            username: 'uporabnikKomentar',
            email: 'uporabnik@komentar.com',
            password: 'komentarGeslo'
        }).save();

        // testna znamenitost
        testAttraction = await new AttractionModel({
            name: 'Komentar znamenitost'
        }).save();

        // prijava in shranim cookie
        const res = await request(app).post('/users/login').send({
            username: 'uporabnikKomentar',
            password: 'komentarGeslo'
        });
        cookie = res.headers['set-cookie'];
    });

    afterAll(async () => {
        await mongoose.connection.close();
    });

    // mora vrniti vse komentarje
    describe('GET /comments', () => {
        it('vrne vse komentarje', async () => {
            await CommentModel.create({ text: 'Komentar test', userId: testUser._id, attractionId: testAttraction._id });
            const res = await request(app).get('/comments');

            expect(res.statusCode).toBe(200);
            expect(Array.isArray(res.body)).toBe(true);
            expect(res.body[0]).toHaveProperty('text', 'Komentar test');
        });
    });

    // mora vrniti en komentar glede na ID
    describe('GET /comments/:id', () => {
        it('vrne en komentar po ID', async () => {
            const comment = await CommentModel.create({ text: 'Komentar po ID', userId: testUser._id, attractionId: testAttraction._id });
            const res = await request(app).get(`/comments/${comment._id}`);

            expect(res.statusCode).toBe(200);
            expect(res.body).toHaveProperty('text', 'Komentar po ID');
        });
    });

    // mora vrniti vse komentarje glede na znamenitost
    describe('GET /comments/attraction/:attractionId', () => {
        it('vrne komentarje za znamenitost', async () => {
            await CommentModel.create({ text: 'Komentar za znamenitost', userId: testUser._id, attractionId: testAttraction._id });
            const res = await request(app).get(`/comments/attraction/${testAttraction._id}`);

            expect(res.statusCode).toBe(200);
            expect(res.body[0]).toHaveProperty('text', 'Komentar za znamenitost');
        });
    });

    describe('POST /comments/attraction/:attractionId', () => {

        // mora dodati komentar če je uporabnik prijavljen
        it('doda komentar, če je uporabnik prijavljen', async () => {
            const res = await request(app)
                .post(`/comments/attraction/${testAttraction._id}`)
                .set('Cookie', cookie)
                .send({ text: 'Avtenticiran komentar' });

            expect(res.statusCode).toBe(201);
            expect(res.body).toHaveProperty('text', 'Avtenticiran komentar');
        });

        // ne sme dodati komentarja če uporabnik ni prijavljen
        it('ne uspe dodajanje brez prijave', async () => {
            const res = await request(app)
                .post(`/comments/attraction/${testAttraction._id}`)
                .send({ text: 'Neavtenticiran komentar' });

            expect(res.statusCode).toBe(401);
        });
    });

    describe('DELETE /comments/:id', () => {

        // mora izbrisati komentar, če ga briše lastnik
        it('izbriše komentar, če je lastnik', async () => {
            const comment = await CommentModel.create({ text: 'Za izbris', userId: testUser._id, attractionId: testAttraction._id });
            const res = await request(app)
                .delete(`/comments/${comment._id}`)
                .set('Cookie', cookie);

            expect(res.statusCode).toBe(204);
        });

        // ne sme brisati komentarja, če ga ne briše lastnik
        it('ne dovoli brisanja, če ni lastnik', async () => {
            const otherUser = await new UserModel({ username: 'drugiKomentarUporabnik', email: 'drugikomentar@uporabnik.com', password: 'drugoKomentarGeslo' }).save();
            const comment = await CommentModel.create({ text: 'Ne tvoj komentar', userId: otherUser._id, attractionId: testAttraction._id });

            const res = await request(app)
                .delete(`/comments/${comment._id}`)
                .set('Cookie', cookie);

            expect(res.statusCode).toBe(403);
        });
    });
});