import { useContext } from "react";
import { UserContext } from "../userContext";
import { Link } from "react-router-dom";

function Header(props) {
    return (
        <header>
            <nav className="navbar navbar-expand-lg navbar-dark bg-primary custom-fixed-navbar">
                <div className="container-fluid">
                    <Link className="navbar-brand" to="/">Domov</Link>
                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav">
                            <li className="nav-item">
                                <Link className="nav-link" to='/map'>Zemljevid</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to='/attractions'>Znamenitosti</Link>
                            </li>
                            <UserContext.Consumer>
                                {context => (
                                    context.user ?
                                        <>
                                            <li className="nav-item">
                                                <Link className="nav-link" to='/profile'>Profil</Link>
                                            </li>
                                            <li className="nav-item">
                                                <Link className="nav-link" to='/logout'>Odjava</Link>
                                            </li>
                                        </>
                                    :
                                        <>
                                            <li className="nav-item">
                                                <Link className="nav-link" to='/login'>Prijava</Link>
                                            </li>
                                            <li className="nav-item">
                                                <Link className="nav-link" to='/register'>Registracija</Link>
                                            </li>
                                        </>
                                )}
                            </UserContext.Consumer>
                        </ul>
                    </div>
                </div>
            </nav>
        </header>
    );
}

export default Header;