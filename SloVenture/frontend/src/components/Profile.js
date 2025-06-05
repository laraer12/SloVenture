import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ChevronLeft, ChevronRight } from "lucide-react";
import axios from 'axios';

function Profile() {
    const { id } = useParams(); // pridobim id iz URL-ja, če obstaja
    const [profile, setProfile] = useState(null);
    const fileInputRef = useRef();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentUser, setCurrentUser] = useState(null); // trenutno prijavljen uporabnik

    // za prikaz obiskanih znamenitosti
    const [visitedAttractions, setVisitedAttractions] = useState([]);
    const [imageIndexes, setImageIndexes] = useState({});
    const [visitedLoading, setVisitedLoading] = useState(true);

    useEffect(() => {
        document.title = "Profil"; // naslov zavihka

        const fetchProfile = async () => {
            try {
                const resMe = await fetch(`${process.env.REACT_APP_BACKEND_URL}/users/profile`, { // pridobim trenutnega uporabnika
                    credentials: 'include'
                });

                let me = null;

                if (resMe.ok)
                    me = await resMe.json();

                setCurrentUser(me);

                // če pa obstaja id v URL-ju, prikažem profil drugega uporabnika
                const profileUrl = id ? `${process.env.REACT_APP_BACKEND_URL}/users/${id}` : `${process.env.REACT_APP_BACKEND_URL}/users/profile`;

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

    // posodobitev profilne slike
    const handleProfilePictureUpload = async (e) => {
        e.preventDefault();

        const file = fileInputRef.current.files[0];

        if (!file)
            return;

        const formData = new FormData();
        formData.append('profilePicture', file);

        const res = await fetch(`${process.env.REACT_APP_BACKEND_URL}/users/upload-profile-picture`, {
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
    

    // funkcija za brisanje profilne slike (na voljo samo adminu)
    const handleRemoveProfilePicture = async () => {
        if (!window.confirm("Ali ste prepričani, da želite izbrisati profilno sliko tega uporabnika?")) // če si admin premisli se slika uporabnika ne bo izbrisala
            return;
            
        const userId = id || profile._id;

        const res = await fetch(`${process.env.REACT_APP_BACKEND_URL}/users/${userId}/remove-profile-picture`, {
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

    // pridobim informacije o obiskih uporabnika
    useEffect(() => {
        const fetchVisitedAttractions = async () => {
            if (!profile?._id) {
                setVisitedLoading(false);
                return;
            }

            setVisitedLoading(true);
            try {
                const res = await axios.get(`${process.env.REACT_APP_BACKEND_URL}/user-visit/user/${profile._id}`);
                setVisitedAttractions(res.data);
            } catch (err) {
                console.error('Napaka pri nalaganju obiskov:', err);
            } finally {
                setVisitedLoading(false);
            }
        };

        fetchVisitedAttractions();
    }, [profile?._id]);

    // za prikaz slik znamenitosti
    useEffect(() => {
        const initialIndexes = {};

        visitedAttractions.forEach((visit) => {
            if (visit._id) 
                initialIndexes[visit._id] = 0;
        });
        setImageIndexes(initialIndexes);
    }, [visitedAttractions]);

    // puščice
    const handlePrev = (visitId, length) => {
        setImageIndexes((prev) => ({
        ...prev,
        [visitId]: (prev[visitId] - 1 + length) % length,
        }));
    };

    const handleNext = (visitId, length) => {
        setImageIndexes((prev) => ({
        ...prev,
        [visitId]: (prev[visitId] + 1) % length,
        }));
    };

    // pridobivanje slik
    const getImageUrl = (visit) => {
        const images = visit.attractionImages && visit.attractionImages.length > 0
            ? visit.attractionImages
            : (visit.images || []);
        const index = imageIndexes[visit._id] || 0;

        if (images.length === 0)
            return `${process.env.REACT_APP_BACKEND_URL}/images/ni_slike.jpg`;

        const url = images[index]?.url || images[index];

        if (!url)
            return `${process.env.REACT_APP_BACKEND_URL}/images/ni_slike.jpg`;

        return url.startsWith('http://') || url.startsWith('https://')
            ? url
            : `${process.env.REACT_APP_BACKEND_URL}${url}`;
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
                    <img src={`${process.env.REACT_APP_BACKEND_URL}/images/${profile.profilePicture}`} alt="Profilna slika" width="100" height="100" className="profile-picture-profile" />
                </div>

                <div>
                    {isOwnProfile && (
                        <div>
                            <p>Spremeni profilno sliko:</p>
                            <p>TESTIRAM (IZBRISI TO)</p>
                            
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
            
            <hr />

            <h2>Obiskane znamenitosti</h2>

            <div className="attractions-container" style={{ padding: "2rem" }}>
            {visitedLoading ? (
                <p>Nalaganje obiskov...</p>
            ) : visitedAttractions.length === 0 ? (
                <div>Ni obiskane znamenitosti.</div>
            ) : (
                visitedAttractions.map((visit) => {
                            const attraction = visit.attractionId;

                            if (!attraction)
                                return null;

                            const images = visit.attractionImages && visit.attractionImages.length > 0
                                ? visit.attractionImages
                                : (visit.images || []);

                            const imageUrl = getImageUrl(visit);
                            const visitDateStr = new Date(visit.visitDate).toLocaleDateString();

                            return (
                                <Link to={`/attractions/${attraction._id}`} key={`${attraction._id}-${visit._id}`} className="attraction-card-link">
                                    <div className="attraction-card">
                                        
                                        {/* prikaz slik ter možnost pomikanja med njimi */}
                                        <div className="image-wrapper">
                                            <img src={imageUrl} alt={attraction.name || 'Znamenitost'} className="attraction-image fade-image" key={imageUrl} />
                                            {images.length > 1 && (
                                                <>
                                                    {/* puščice za pomikanje med slikami */}
                                                    <button className="nav-button left" onClick={(e) => { e.preventDefault(); handlePrev(visit._id, images.length); }}>
                                                        <ChevronLeft />
                                                    </button>
                                                    <button className="nav-button right" onClick={(e) => { e.preventDefault(); handleNext(visit._id, images.length); }}>
                                                        <ChevronRight />
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                        
                                        {/* informacije o znamenitosti ter datum obiska */}
                                        <div className="attraction-details">
                                            <h3 className="attraction-name">{attraction.name || 'Neznano ime'}</h3>
                                            <p className="attraction-locationType">{attraction.locationType || 'Neznan tip lokacije'}</p>
                                            <strong className="attraction-visited">Obiskano:</strong> <span className="attraction-visit-date">{visitDateStr}</span>
                                        </div>
                                    </div>
                                </Link>
                            );
                        })
                    )}
            </div>
        </>
    );
}

export default Profile;