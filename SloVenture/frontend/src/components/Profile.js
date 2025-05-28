import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';

function Profile() {
    const { id } = useParams(); // pridobim id iz URL-ja, če obstaja
    const [profile, setProfile] = useState(null);
    const fileInputRef = useRef();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        document.title = "Profil"; // naslov zavihka

        const fetchProfile = async () => {
            try {
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
            }
            catch (err) {
                setError('Napaka pri nalaganju profila.');
            }
            finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, [id]);

    const handleProfilePictureUpload = async (e) => {
        e.preventDefault();

        const file = fileInputRef.current.files[0];

        if (!file)
            return;

        const formData = new FormData();
        formData.append('profilePicture', file);

        const res = await fetch('http://localhost:3001/users/upload-profile-picture', {
            method: 'POST',
            body: formData,
            credentials: 'include',
        });

        if (res.ok) {
            const data = await res.json();
            setProfile(data);
            fileInputRef.current.value = '';
        }
    };

    if (loading)
        return <p>Nalaganje...</p>;

    if (error)
        return <p style={{ color: 'red' }}>{error}</p>;

    if (!profile)
        return <p>Uporabnik ni bil najden.</p>;

    return (
        <>
            <h1>Profil uporabnika {profile.username}</h1>
            <br />

            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                <div>
                    <img src={`http://localhost:3001/images/${profile.profilePicture}`} alt="Profilna slika" width="100" height="100" className="profile-picture-profile" />
                </div>
                <div>
                    <p>Spremeni profilno sliko:</p>
                    
                    <form onSubmit={handleProfilePictureUpload}>
                        <input type="file" name="profilePicture" ref={fileInputRef} accept="image/*" style={{ marginRight: '15px'}}/>

                        <button type="submit" className="btn btn-primary">Shrani sliko</button>
                    </form>
                </div>
            </div>
            
            <br />

            <div>
                <p><strong>Uporabniško ime:</strong> {profile.username}</p>
                <p><strong>Email:</strong> {profile.email}</p>
            </div>
        </>
    );
}

export default Profile;