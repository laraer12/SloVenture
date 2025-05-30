import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';

function Profile() {
    const { id } = useParams(); // pridobim id iz URL-ja, če obstaja
    const [profile, setProfile] = useState(null);
    const fileInputRef = useRef();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentUser, setCurrentUser] = useState(null); // trenutno prijavljen uporabnik

    useEffect(() => {
        document.title = "Profil"; // naslov zavihka

        const fetchProfile = async () => {
            try {
                const resMe = await fetch('http://localhost:3001/users/profile', { // pridobim trenutnega uporabnika
                    credentials: 'include'
                });

                let me = null;

                if (resMe.ok)
                    me = await resMe.json();

                setCurrentUser(me);

                // če pa obstaja id v URL-ju, prikažem profil drugega uporabnika
                const profileUrl = id ? `http://localhost:3001/users/${id}` : 'http://localhost:3001/users/profile';

                const resProfile = await fetch(profileUrl, {
                    credentials: 'include'
                });

                if (resProfile.ok) {
                    const data = await resProfile.json();
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

    const handleRemoveProfilePicture = async () => {
        if (!window.confirm("Ali ste prepričani, da želite izbrisati profilno sliko tega uporabnika?")) // če si admin premisli se slika uporabnika ne bo izbrisala
            return;
            
        const userId = id || profile._id;

        const res = await fetch(`http://localhost:3001/users/${userId}/remove-profile-picture`, {
            method: 'PUT',
            credentials: 'include',
        });

        if (res.ok) {
            const data = await res.json();
            setProfile(data.user);
        }
        else {
            const errorData = await res.json();
            alert('Napaka: ' + errorData.message);
        }
    };

    const isOwnProfile = currentUser && profile && currentUser.username === profile.username;

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
                    {isOwnProfile && (
                        <div>
                            <p>Spremeni profilno sliko:</p>
                            
                            <form onSubmit={handleProfilePictureUpload}>
                                <input type="file" name="profilePicture" ref={fileInputRef} accept="image/*" style={{ marginRight: '15px' }} />
                                <button type="submit" className="btn btn-primary">Shrani sliko</button>
                            </form>
                        </div>
                    )}

                    {/* gumb za izbris slike, prikaže se samo adminu */}
                    {currentUser?.isAdmin && profile.profilePicture !== 'default-profile-picture.jpg' && (
                        <button onClick={handleRemoveProfilePicture} className="btn btn-warning" style={{ marginTop: '10px' }}>
                            Izbriši profilno sliko uporabnika
                        </button>
                    )}
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