import { useState } from 'react';

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");

    async function Register(e) {
        e.preventDefault();

        const res = await fetch("http://localhost:3001/users", {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                username: username,
                password: password
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
                <input type="text" className="form-control" name="username" value={username} onChange={(e) => setUsername(e.target.value)} />

                <label htmlFor="email">E-naslov:</label>
                <input type="text" className="form-control" name="email" value={email} onChange={(e) => setEmail(e.target.value)} />

                <label htmlFor="password">Geslo:</label>
                <input type="password" className="form-control" name="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                <br />

                <input type="submit" className="btn btn-primary" name="submit" value="Registracija" />
                <label>{error}</label>
            </form>
        </div>
    );
}

export default Register;