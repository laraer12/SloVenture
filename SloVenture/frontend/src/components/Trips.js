import { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { UserContext } from '../userContext';

const Trips = () => {
    const { user } = useContext(UserContext);
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        document.title = "Moji izleti"; // naslov zavihka

        const fetchTrips = async () => {
            if (!user)
                return;

            setLoading(true);

            try {
                const res = await axios.get(`http://localhost:3001/trips/user/${user._id}`);

                // sortiram potovanja po order naraščajoče
                const sortedTrips = res.data.sort((a, b) => {
                    const minOrderA = a.attractions && a.attractions.length > 0
                        ? Math.min(...a.attractions.map(attr => attr.order ?? Infinity))
                        : Infinity;

                    const minOrderB = b.attractions && b.attractions.length > 0
                        ? Math.min(...b.attractions.map(attr => attr.order ?? Infinity))
                        : Infinity;

                return minOrderA - minOrderB;
                });

                setTrips(sortedTrips);
            }
            catch (err) {
                console.error("Napaka pri nalaganju potovanj:", err);
            }
            finally {
                setLoading(false);
            }
        };

    fetchTrips();
    }, [user]);

    const getImageUrl = (url) => {
        if (!url) return
            null;

        if (url.startsWith('http://') || url.startsWith('https://'))
        return url;
    
        return `http://localhost:3001${url}`;
    };

    if (loading)
        return <p>Nalaganje...</p>;

    return (
        <div className="container mt-4">
            <h1 className="mb-4">Moja potovanja</h1>
            {trips.length === 0 ? (
                <p>Ni še dodanih potovanj.</p>
            ) : (

                /* prikaz informacij o izletu */
                trips.map((trip) => (
                    <div key={trip._id} className="card mb-3" style={{ cursor: 'pointer' }} onClick={() => navigate(`/trips/${trip._id}`)}>
                        <div className="row g-0 align-items-center">
                            <div className="col-md-8">
                                <div className="card-body">
                                    <h5 className="card-title">{trip.tripName || "Neimenovano potovanje"}</h5>
                                    {trip.startDate && trip.endDate && (
                                        <p className="card-text">
                                        <small className="text-muted">
                                            {new Date(trip.startDate).toLocaleDateString()} – {new Date(trip.endDate).toLocaleDateString()}
                                        </small>
                                        </p>
                                    )}
                                </div>
                            </div>
                            <div className="col-md-4">
                                {trip.firstImageUrl ? (
                                    <img
                                        src={getImageUrl(trip.firstImageUrl) || 'http://localhost:3001/images/ni_slike.jpg'}
                                        alt="Slika potovanja"
                                        className="img-fluid rounded-end"
                                        style={{ objectFit: 'cover', height: '100%', maxHeight: '200px', width: '100%' }}
                                    />
                                ) : (
                                    <img
                                        src='http://localhost:3001/images/ni_slike.jpg'
                                        alt="Ni slike"
                                        className="img-fluid rounded-end"
                                        style={{ objectFit: 'cover', height: '100%', maxHeight: '200px', width: '100%' }}
                                    />
                                )}
                            </div>
                        </div>
                    </div>
                ))
            )}
        </div>
    );
};

export default Trips;