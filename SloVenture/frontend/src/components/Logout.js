import { useEffect, useContext } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';

function Logout() {
    const userContext = useContext(UserContext);

    useEffect(() => {
        const logout = async () => {
            await fetch("http://localhost:3001/users/logout", {
                credentials: "include"
            });

            userContext.setUserContext(null);
        };
        logout();
    }, []);

    return (
        <Navigate replace to="/" />
    );
}

export default Logout;