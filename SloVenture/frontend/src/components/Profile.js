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
            const url = id
                ? `http://localhost:3001/users/${id}`  // drug uporabnik
                : 'http://localhost:3001/users/profile'; // moj profil

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

    const handleAvatarUpload = async (e) => {
        e.preventDefault();

        const file = fileInputRef.current.files[0];

        if (!file)
            return;

        const formData = new FormData();
        formData.append('avatar', file);

        const res = await fetch('http://localhost:3001/users/upload-avatar', {
            method: 'POST',
            body: formData,
            credentials: 'include',
        });

        if (res.ok) {
            const data = await res.json();
            setProfile(data);
        }
    };

    if (!profile)
        return <p>Uporabnik ni bil najden.</p>;

    return (
        <>
            <h1>Profil uporabnika {profile.username}</h1>
            <br></br>

            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                <div>
                    <img src={`http://localhost:3001/images/${profile.avatar}`} alt="Avatar" width="100" height="100" style={{ borderRadius: '50%', border: '2px solid #ccc' }}/>
                </div>

                {isOwnProfile && (
                    <div>
                        <p>Spremeni avatarja:</p>
                        
                        <form onSubmit={handleAvatarUpload}>
                            <input type="file" name="avatar" ref={fileInputRef} accept="image/*" />

                            <button type="submit" className="btn btn-primary">Shrani sliko</button>
                        </form>
                    </div>
                )}
            </div>
            <br></br>
            
            <div style={{ backgroundColor: '#007bff', padding: '20px' }}>
                <p><strong>Uporabniško ime:</strong> {profile.username}</p>
                <p><strong>Email:</strong> {profile.email}</p>
                <p><strong>Število objav:</strong> {profile.posts}</p>
                <p><strong>Prejeti všečki:</strong> {profile.likesReceived}</p>
                <p><strong>Objavljeni komentarji:</strong> {profile.commentsPosted}</p>
            </div>
        </>
    );
}

export default Profile;