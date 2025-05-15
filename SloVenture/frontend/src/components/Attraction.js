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
        const response = await axios.get(`http://localhost:3001/attractions/${id}`);

        const data = response.data;
        const fullAttraction = data.attraction || data;
        const images = data.images || fullAttraction.images || [];

        setAttraction(fullAttraction);

        const mainImage = images.length > 0 && images[0].url
          ? images[0].url
          : '/images/default-image.jpg';

        setImageUrl(mainImage);
        setLoading(false);
      }
      catch (err) {
        setError('Napaka pri nalaganju znamenitosti.');
        setLoading(false);
      }
    };

    fetchAttraction();
  }, [id]);

  if (loading)
    return <p>Nalaganje...</p>;

  if (error)
    return <p>{error}</p>;

  if (!attraction)
    return <p>Znamenitost ni bila najdena.</p>;

  return (
    <div className="attraction-details-container">
      <h1>{attraction.name}</h1>
      <img src={imageUrl} alt={attraction.name} style={{ width: '400px', borderRadius: '10px', marginBottom: '1rem' }} onError={() => console.error(`Slika se ni naložila: ${imageUrl}`)} />
      <p><strong>Opis:</strong> {attraction.description}</p>
      <p><strong>Naslov:</strong> {attraction.address}</p>
      <p><strong>Tip lokacije:</strong> {attraction.locationType}</p>
      <p><strong>Višina:</strong> {attraction.elevation} m</p>
      <p><strong>Vstopnina:</strong> {attraction.entryFee} €</p>
      <p><strong>Potrebna rezervacija:</strong> {attraction.requiresReservation ? 'Da' : 'Ne'}</p>
      <p><strong>Ocena:</strong> {attraction.rating}/5</p>
      <p><strong>Dostopnost:</strong> {attraction.ratingAccessible}/5</p>
      <p><strong>Primerno za družine:</strong> {attraction.ratingFamilyFriendly}/5</p>
      <p><strong>Primerno za starejše:</strong> {attraction.ratingElderlyFriendly}/5</p>
    </div>
  );
}

export default Attraction;