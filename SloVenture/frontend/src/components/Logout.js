import { useEffect, useContext } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';

function Logout() {
    const userContext = useContext(UserContext);

    useEffect(() => {
        const logout = async () => {
            await fetch(`${process.env.REACT_APP_BACKEND_URL}/users/logout`, {
                credentials: "include"
            });

            localStorage.removeItem("token");
            userContext.setUserContext(null);
        };
        logout();
    }, []);

    return (
        <Navigate replace to="/" />
    );
}

export default Logout;