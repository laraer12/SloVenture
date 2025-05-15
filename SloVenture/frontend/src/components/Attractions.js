import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

function Attractions() {
    const [attractions, setAttractions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchAttractions = async () => {
            try {
                const response = await axios.get('http://localhost:3001/attractions');

                if (response.data.message)
                    setError(response.data.message);
                
                else
                    setAttractions(response.data);
            }
            catch (err) {
                setError('Napaka pri pridobivanju znamenitosti.');
            }
            finally {
                setLoading(false);
            }
        };
        fetchAttractions();
    }, []);

    const getImageUrl = (item) => {
        const image = item?.images?.[0]?.url;
        return image ? image : '/images/default-image.jpg';
    };

    if (loading)
        return <div>Nalaganje...</div>;

    if (error)
        return <div style={{ color: 'red' }}>{error}</div>;

    if (!Array.isArray(attractions) || attractions.length === 0)
        return <div>Ni razpoložljivih znamenitosti.</div>;

    return (
        <div className="attractions-container">
            {attractions.map((item) => {
                const attraction = item.attraction;

                if (!attraction)
                    return null;

                return (
                <Link
                    to={`/attractions/${attraction._id}`}
                    key={attraction._id}
                    className="attraction-card-link"
                >
                    <div className="attraction-card">
                        <img src={getImageUrl(item)} alt={attraction.name || 'Znamenitost'} className="attraction-image" />
                        <div className="attraction-details">
                            <h3 className="attraction-name">{attraction.name || 'Neznano ime'}</h3>
                            <p className="attraction-locationType">{attraction.locationType || 'Neznan tip lokacije'}</p>
                            <p className="attraction-description">{attraction.description || 'Opis ni na voljo.'}</p>
                        </div>
                    </div>
                </Link>
                );
            })}
        </div>
    );
}

export default Attractions;