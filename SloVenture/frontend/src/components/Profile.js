import { useContext, useEffect, useRef, useState } from 'react';
import { UserContext } from '../userContext';
import { Navigate, useParams } from 'react-router-dom';

function Profile() {
    const { id } = useParams(); // pridobim id iz URL-ja, če obstaja
    const { user } = useContext(UserContext);
    const [profile, setProfile] = useState(null);
    const fileInputRef = useRef();

    const isOwnProfile = user && (!id || user._id === id);

    useEffect(() => {
        const fetchProfile = async () => {
            const url = 'http://localhost:3001/users/profile';

            const res = await fetch(url, {
                credentials: 'include'
            });

            if (res.ok) {
                const data = await res.json();
                setProfile(data);
            }
            else
                setProfile(null);
        };

        fetchProfile();
    }, [id]);

    if (!profile)
        return <p>Uporabnik ni bil najden.</p>;

    return (
        <>
            <h1>Profil uporabnika {profile.username}</h1>
            <br></br>
            
            <div>
                <p><strong>Uporabniško ime:</strong> {profile.username}</p>
                <p><strong>Email:</strong> {profile.email}</p>
            </div>
        </>
    );
}

export default Profile;