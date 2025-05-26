import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { ChevronLeft, ChevronRight } from 'lucide-react';

function Attractions() {
  const [attractions, setAttractions] = useState([]);
  const [imageIndexes, setImageIndexes] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAttractions = async () => {
      try {
        const response = await axios.get(`${process.env.REACT_APP_BACKEND_URL}/attractions`);

        console.log("Fetching from:", `${process.env.REACT_APP_BACKEND_URL}/attractions`);


        if (response.data.message)
          setError(response.data.message);

        else {
          setAttractions(response.data);

          const initialIndexes = {};

          response.data.forEach((item) => {
            const id = item.attraction?._id;

            if (id)
              initialIndexes[id] = 0;
          });
          setImageIndexes(initialIndexes);
        }
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

  /* klik puščic pri pomikanju slik za znamenitosti */
  const handlePrev = (id, length) => {
    setImageIndexes((prev) => ({
      ...prev,
      [id]: (prev[id] - 1 + length) % length,
    }));
  };

  const handleNext = (id, length) => {
    setImageIndexes((prev) => ({
      ...prev,
      [id]: (prev[id] + 1) % length,
    }));
  };

  const getImageUrl = (item) => {
    const attractionId = item.attraction?._id;
    const images = item?.images || [];
    const index = imageIndexes[attractionId] || 0;

    return images.length > 0 && images[index]?.url
      ? images[index].url
      : `${process.env.REACT_APP_BACKEND_URL}/images/ni_slike.jpg`;
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

        const images = item?.images || [];
        const imageUrl = getImageUrl(item);

        return (
          <Link 
            to={`/attractions/${attraction._id}`}
            key={attraction._id}
            className="attraction-card-link"
          >
            <div className="attraction-card">
              <div className="image-wrapper">

                {/* Slika */}
                <img src={imageUrl} alt={attraction.name || 'Znamenitost'} className={`attraction-image fade-image`} key={imageUrl} />
                {images.length > 1 && (
                  <>
                  
                  {/* Prikaz puščic */}
                    <button className="nav-button left" onClick={(e) => { e.preventDefault(); handlePrev(attraction._id, images.length); }}>
                      <ChevronLeft />
                    </button>
                    <button className="nav-button right" onClick={(e) => { e.preventDefault(); handleNext(attraction._id, images.length); }}>
                      <ChevronRight />
                    </button>
                  </>
                )}
              </div>

              {/* Ostali podatki */}
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