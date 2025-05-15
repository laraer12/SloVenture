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

                setLoading(false);
            }
            catch (error) {
                setError('Failed to fetch attractions');
                setLoading(false);
            }
        };
        fetchAttractions();
    }, []);

    if (loading)
        return <div>Loading...</div>;

    if (error)
        return <div>{error}</div>;

    return (
        <div className="attractions-container">
            {attractions.length === 0 ? (
                <div>No attractions available</div>
            ) : (
                attractions.map((item) => {
                    const attraction = item.attraction;
                    const imageUrl = item.images && item.images[0] && item.images[0].url
                        ? item.images[0].url
                        : "/path/to/default-image.jpg";

                    return (
                        <Link 
                            to={`/attractions/${attraction._id}`} 
                            key={attraction._id}
                            className="attraction-card-link"
                        >
                            <div className="attraction-card">
                                <img src={imageUrl} alt={attraction.name} className="attraction-image"/>
                                <div className="attraction-details">
                                    <h3 className="attraction-name">{attraction.name}</h3>
                                    <p className="attraction-locationType">{attraction.locationType}</p>
                                    <p className="attraction-description">{attraction.description}</p>
                                </div>
                            </div>
                        </Link>
                    );
                })
            )}
        </div>
    );
}

export default Attractions;