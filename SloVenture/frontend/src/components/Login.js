import { Navigate } from 'react-router-dom';

function Login(){
    return (
        <div>
            <h3>Prijava</h3>
            <br></br>
            <form>
                <label for="username">Uporabniško ime: </label>
                <input type="text" className="form-control" name="username"/>
                
                <label for="password">Geslo: </label>
                <input type="password" className="form-control" name="password"/>
                <br></br>
                
                <input type="submit" className="btn btn-primary" name="submit" value="Prijava"/>
            </form>
        </div>
    );
}

export default Login;