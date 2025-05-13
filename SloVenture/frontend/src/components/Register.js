function Register() {
    return (
        <div>
            <h3>Registracija</h3>
            <br />

            <form>
                <label htmlFor="username">Uporabniško ime:</label>
                <input type="text" className="form-control" name="username" />

                <label htmlFor="email">E-naslov:</label>
                <input type="text" className="form-control" name="email"/>

                <label htmlFor="password">Geslo:</label>
                <input type="password" className="form-control" name="password" />
                <br />

                <input type="submit" className="btn btn-primary" name="submit" value="Registracija" />
            </form>
        </div>
    );
}

export default Register;