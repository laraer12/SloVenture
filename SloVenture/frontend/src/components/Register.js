import { useState, useEffect } from 'react';
import ReCAPTCHA from "react-google-recaptcha"; // za uporabo captche

const siteKey = process.env.REACT_APP_RECAPTCHA_SITE_KEY; // ključ je shranjen v .env datoteki

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [captchaToken, setCaptchaToken] = useState(""); // captcha
    const [csrfToken, setCsrfToken] = useState(''); // csrf token

    const handleCaptcha = (value) => {
        setCaptchaToken(value); // nastavim token za captcho
    };

    useEffect(() => {
        document.title = "Registracija"; // naslov zavihka

        fetch('http://localhost:3001/csrf-token', { // pridobim csrf token
            credentials: 'include'
        })
        .then(res => res.json())
        .then(data => setCsrfToken(data.csrfToken))
        .catch(err => console.error("Getting CSRF token failed", err));
    }, []);

    async function Register(e) {
        e.preventDefault();

        // uporabnik ne sme captche preskočiti
        if (!captchaToken) {
            setError("Prosim potrdite da niste robot");
            return;
        }
        const res = await fetch("http://localhost:3001/users", {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': csrfToken // dodana csrf token zaščita
            },
            body: JSON.stringify({
                email: email,
                username: username,
                password: password,
                captchaToken: captchaToken // dodana captcha
            })
        });

        const data = await res.json();

        if (data._id !== undefined)
            window.location.href = "/";
        else {
            setUsername("");
            setPassword("");
            setEmail("");
            setError("Registracija ni uspela");
        }
    }

    return (
        <div>
            <h3>Registracija</h3>
            <br />

            <form onSubmit={Register}>
                <label htmlFor="username">Uporabniško ime:</label>
                <input type="text" className="form-control" name="username" value={username} onChange={(e) => setUsername(e.target.value)} required />

                <label htmlFor="email">E-naslov:</label>
                <input type="text" className="form-control" name="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

                <label htmlFor="password">Geslo:</label>
                <input type="password" className="form-control" name="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                <br />

                <ReCAPTCHA sitekey={siteKey} onChange={handleCaptcha} required/> {/* captcha obrazec */}
                <br />

                <input type="submit" className="btn btn-primary" name="submit" value="Registracija" />
                <label>{error}</label>
            </form>
        </div>
    );
}

export default Register;