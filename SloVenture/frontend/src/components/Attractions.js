import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { ChevronLeft, ChevronRight } from 'lucide-react';

function Attractions() {
  const [attractions, setAttractions] = useState([]);
  const [imageIndexes, setImageIndexes] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // filter po regijah
  const [regions, setRegions] = useState([]);
  const [selectedRegionIds, setSelectedRegionIds] = useState([]);
  const [filteredAttractions, setFilteredAttractions] = useState([]);

  useEffect(() => {
    document.title = "Znamenitosti"; // naslov zavihka

    // pridobim in znamenitosti in regije
    const fetchData = async () => {
      try {
        const [regionsRes, attractionsRes] = await Promise.all([
          axios.get('http://localhost:3001/regions'),
          axios.get('http://localhost:3001/attractions'),
        ]);

        setRegions(regionsRes.data);
        setAttractions(attractionsRes.data);

        const initialIndexes = {};
        attractionsRes.data.forEach((item) => {
          const id = item.attraction?._id;

          if (id)
            initialIndexes[id] = 0;
        });
        
        setImageIndexes(initialIndexes);
      }
      catch (err) {
        setError('Napaka pri pridobivanju podatkov.');
        console.error(err);
      }
      finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // filtriranje na podlagi izbranih regij
  useEffect(() => {
    if (selectedRegionIds.length === 0)
      setFilteredAttractions(attractions);
    
    else {
      const filtered = attractions.filter((item) =>
        selectedRegionIds.includes(item.attraction?.regionId?._id)
      );
      setFilteredAttractions(filtered);
    }
  }, [selectedRegionIds, attractions]);

  const handleRegionToggle = (regionId) => {
    setSelectedRegionIds((prev) =>
      prev.includes(regionId)
        ? prev.filter((id) => id !== regionId)
        : [...prev, regionId]
    );
  };

  // klik puščic pri pomikanju slik za znamenitosti
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

  // prilagojena funkcija prikazu slik, da se prikažejo tudi slike, dodane od uporabnikov
  const getImageUrl = (item) => {
    const attractionId = item.attraction?._id;
    const images = item?.images || [];
    const index = imageIndexes[attractionId] || 0;

    if (images.length === 0)
      return 'http://localhost:3001/images/ni_slike.jpg';

    const url = images[index]?.url;

    if (!url)
      return 'http://localhost:3001/images/ni_slike.jpg';

    return url.startsWith('http://') || url.startsWith('https://')
      ? url
      : `http://localhost:3001${url}`;
  };

  if (loading)
    return <div>Nalaganje...</div>;

  if (error)
    return <div style={{ color: 'red' }}>{error}</div>;

  return (
    <>
      {/* filter po regijah */}
      <div className="region-filter">
        <h3>Išči po regijah:</h3>

        {/* checkboxi prikazani kot gumbi za izbor regij */}
        <div className="region-buttons">
          {regions.map((region) => {
            const isChecked = selectedRegionIds.includes(region._id);
            return (
              <label key={region._id} className={`region-label ${isChecked ? 'checked' : ''}`}>
                <input type="checkbox" value={region._id} checked={isChecked} onChange={() => handleRegionToggle(region._id)} />
                <span>{region.name}</span>
              </label>
            );
          })}
        </div>
      </div>

      {/* znamenitosti */}
      <div className="attractions-container" style={{ padding: '2rem' }}>
        {filteredAttractions.length === 0 ? (
          <div>Ni znamenitosti za izbrane regije.</div>
        ) : (
          filteredAttractions.map((item) => {
            const attraction = item.attraction;

            if (!attraction)
              return null;

            const images = item?.images || [];
            const imageUrl = getImageUrl(item);

            return (
              <Link to={`/attractions/${attraction._id}`} key={attraction._id} className="attraction-card-link">
                <div className="attraction-card">
                  <div className="image-wrapper">
                    <img src={imageUrl} alt={attraction.name || 'Znamenitost'} className="attraction-image fade-image" key={imageUrl} />
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
          })
        )}
      </div>
    </>
  );
}

export default Attractions;