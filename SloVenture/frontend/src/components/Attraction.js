import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';

function Attraction() {
  const { id } = useParams();
  const [attraction, setAttraction] = useState(null);
  const [imageUrl, setImageUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAttraction = async () => {
      try {
        const response = await axios.get(`${process.env.REACT_APP_BACKEND_URL}/attractions/${id}`);
        const data = response.data;
        const fullAttraction = data.attraction || data;
        const images = data.images || fullAttraction.images || [];

        setAttraction(fullAttraction);

        const mainImage = images.length > 0 && images[0].url;
        setImageUrl(mainImage);
      }
      catch (err) {
        setError('Napaka pri nalaganju znamenitosti.');
      }
      finally {
        setLoading(false);
      }
    };
    fetchAttraction();
  }, [id]);

  function formatAddress(address) {
    if (!address)
      return '';

    const parts = [address.street, address.city, address.postalCode, address.country]
      .filter(part => part && part.trim() !== '');
    return parts.join(', ');
  }

  function renderOpeningHours(hours) {
    if (!hours)
      return null;

    const days = Object.entries(hours).filter(([day, value]) => value && value.trim() !== '');
    
    if (days.length === 0)
      return null;

    return (
      <div>
        <strong>Odpiralni čas:</strong>
        <ul>
          {days.map(([day, time]) => (
            <li key={day}>
              {day.charAt(0).toUpperCase() + day.slice(1)}: {time}
            </li>
          ))}
        </ul>
      </div>
    );
  }
  if (loading)
    return <p>Nalaganje...</p>;

  if (error)
    return <p style={{ color: 'red' }}>{error}</p>;

  if (!attraction)
    return <p>Znamenitost ni bila najdena.</p>;

  return (
    <div className="attraction-details-container">
      {/* Ime znamenitosti */}
      <h1>{attraction.name}</h1>

      {/* Slika */}
      {imageUrl && (
        <img src={imageUrl} alt={attraction.name || ''} style={{ width: '400px', borderRadius: '10px', marginBottom: '1rem' }} onError={(e) => { e.target.style.display = 'none'; }} />
      )}

      {/* Podatki o znamenitosti */}
      <div className="attraction-info">

        {/* Regija */}
        {attraction.regionId && (
          <p><strong>Regija:</strong> {attraction.regionId.name}</p>
        )}

        {/* Opis */}
        {attraction.description && (
          <p><strong>Opis:</strong> {attraction.description}</p>
        )}

        {/* Naslov */}
        {attraction.address && (
          <p><strong>Naslov:</strong> {formatAddress(attraction.address)}</p>
        )}

        {/* Klasifikacija */}
        {attraction.classification && (
          <p><strong>Klasifikacija:</strong> {attraction.classification}</p>
        )}

        {/* Tip lokacije */}
        {attraction.locationType && (
          <p><strong>Tip lokacije:</strong> {attraction.locationType}</p>
        )}

        {/* Višina */}
        {attraction.elevation > 0 && (
          <p><strong>Višina:</strong> {attraction.elevation} m</p>
        )}

        {/* Možnosti dostopa */}
        {attraction.accessibilityOptions && (
          <div>
            <strong>Možnosti dostopa:</strong>
            <ul>
              {attraction.accessibilityOptions
                .split(',')
                .map((option, index) => (
                  <li key={index}>{option.trim()}</li>
                ))}
            </ul>
          </div>
        )}

        {/* Dostopnost */}
        {attraction.ratingAccessible > 0 && (
          <p><strong>Dostopnost:</strong> {attraction.ratingAccessible}/5</p>
        )}

        {/* Primerno za družine */}
        {attraction.ratingFamilyFriendly > 0 && (
          <p><strong>Primerno za družine:</strong> {attraction.ratingFamilyFriendly}/5</p>
        )}

        {/* Primerno za starejše */}
        {attraction.ratingElderlyFriendly > 0 && (
          <p><strong>Primerno za starejše:</strong> {attraction.ratingElderlyFriendly}/5</p>
        )}
        
        {/* Ocena */}
        {attraction.rating > 0 && (
          <p><strong>Ocena:</strong> {attraction.rating}/5</p>
        )}

        {/* Potrebna rezervacija */}
        {typeof attraction.requiresReservation === 'boolean' && (
          <p><strong>Potrebna rezervacija:</strong> {attraction.requiresReservation ? 'Da' : 'Ne'}</p>
        )}

        {/* Odpiralni časi */}
        {renderOpeningHours(attraction.openingHours)}

        {/* Vstopnina */}
        {attraction.entryFee > 0 && (
          <p><strong>Vstopnina:</strong> {attraction.entryFee} €</p>
        )}

        {/* Ustvarjeno */}
        {attraction.createdAt && (
          <p><strong>Ustvarjeno:</strong> {new Date(attraction.createdAt).toLocaleDateString()}</p>
        )}

        {/* Preverjeno */}
        {typeof attraction.verified === 'boolean' && (
          <p><strong>Preverjeno:</strong> {attraction.verified ? 'Da' : 'Ne'}</p>
        )}

        {/* Google maps */}
        {attraction.googleMapsLink && (
          <button type="button" className="btn btn-primary" onClick={() => window.open(attraction.googleMapsLink, '_blank', 'noopener,noreferrer')}>Odpri v Google Maps</button>
        )}
      </div>
    </div>
  );
}

export default Attraction;