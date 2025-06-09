import { useContext, useState, useEffect } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';

function Login(){
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const userContext = useContext(UserContext); 

    useEffect(() => {
      document.title = "Prijava"; // naslov zavihka
    }, []);

    async function Login(e){
        e.preventDefault();

        const res = await fetch(`${process.env.REACT_APP_BACKEND_URL}/users/login`, {
            method: "POST",
            headers: { 'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await res.json();

        if(data.token && data.user){
            localStorage.setItem("token", data.token);
            userContext.setUserContext(data.user);
        }
        else {
            setUsername("");
            setPassword("");
            setError("Invalid username or password");
        }
    }

    return (
        <div>
            <h3>Prijava</h3>
            <br></br>
            <form onSubmit={Login}>
                {userContext.user ? <Navigate replace to="/" /> : ""}

                <label for="username">Uporabniško ime: </label>
                <input type="text" className="form-control" name="username" value={username} onChange={(e)=>(setUsername(e.target.value))}/>
                
                <label for="password">Geslo: </label>
                <input type="password" className="form-control" name="password" value={password} onChange={(e)=>(setPassword(e.target.value))}/>
                <br></br>
                
                <input type="submit" className="btn btn-primary" name="submit" value="Prijava"/>
                <label>{error}</label>
            </form>
        </div>
    );
}

export default Login;