import { useEffect, useState } from 'react';
import ReCAPTCHA from "react-google-recaptcha";

const RECAPTCHA_SITE_KEY = "6LdHgCgrAAAAALVNV8pff70c17o8iDEiiZu6z6sK"; // uporabim svoj pridobljeni ključ

function Register() {
    const [username, setUsername] = useState([]);
    const [password, setPassword] = useState([]);
    const [email, setEmail] = useState([]);
    const [error, setError] = useState([]);
    const [captchaToken, setCaptchaToken] = useState(""); // dodana captcha
    const [csrfToken, setCsrfToken] = useState(''); // dodan csrftoken

    const handleCaptcha = (value) => {
        setCaptchaToken(value);
    };

    useEffect(() => {
        fetch('http://localhost:3001/csrf-token', {
            credentials: 'include'
        })
        .then(res => res.json())
        .then(data => setCsrfToken(data.csrfToken))
        .catch(err => console.error("Pridobivanje CSRF token-a je spodletelo", err));
    }, []);    

    async function Register(e){
        e.preventDefault();

        if (!captchaToken) {
            setError("Prosim potrdite da niste robot");
            return;
        }
        const res = await fetch("http://localhost:3001/users", {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': csrfToken
            },
            body: JSON.stringify({
                email: email,
                username: username,
                password: password,
                captchaToken: captchaToken // captcha dodana
            })
        });

        const data = await res.json();

        if (data._id !== undefined)
            window.location.href="/";
        
        else {
            setUsername("");
            setPassword("");
            setEmail("");
            setError("Registration failed");
        }
    }

    return(
        <div>
            <h3>Registracija</h3>
            <br></br>

            <form onSubmit={Register}>
                <label for="username">Uporabniško ime:</label>
                <input type="text" className="form-control" name="username" value={username} onChange={(e)=>(setUsername(e.target.value))}/>

                <label for="email">E-naslov:</label>
                <input type="text" className="form-control" name="email" value={email} onChange={(e)=>(setEmail(e.target.value))} />

                <label for="password">Geslo:</label>
                <input type="password" className="form-control" name="password" value={password} onChange={(e)=>(setPassword(e.target.value))} />
                <br></br>

                <ReCAPTCHA sitekey={RECAPTCHA_SITE_KEY} onChange={handleCaptcha}/> {/* captcha dodana */}
                <br></br>

                <input type="submit" className="btn btn-primary" name="submit" value="Registracija" />
                <label>{error}</label>
            </form>
        </div>
    );
}

export default Register;