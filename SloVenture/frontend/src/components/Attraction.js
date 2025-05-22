import { useParams } from 'react-router-dom';
import { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { UserContext } from '../userContext';

function Attraction() {
  const { id } = useParams();
  const [attraction, setAttraction] = useState(null);
  const [imageUrl, setImageUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // za komentarje
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const { user } = useContext(UserContext);

  // za pridobivanje znamenitosti
  useEffect(() => {
    document.title = "Nalaganje znamenitosti..."; // naslov zavihka, dokler se znamenitost ne naloži

    const fetchAttraction = async () => {
      try {
        const response = await axios.get(`http://localhost:3001/attractions/${id}`);
        const data = response.data;
        const fullAttraction = data.attraction || data;
        const images = data.images || fullAttraction.images || [];

        setAttraction(fullAttraction);

        const mainImage = images.length > 0 && images[0].url;
        setImageUrl(mainImage);

        // ko se naloži za naslov uporabim ime znamenitosti
        if (fullAttraction.name)
          document.title = fullAttraction.name;
        
        else
          document.title = "Znamenitost";
      }
      catch (err) {
        setError('Napaka pri nalaganju znamenitosti.');
        document.title = "Napaka"; // če se kaj zalomi izpišem napako
      }
      finally {
        setLoading(false);
      }
    };
    fetchAttraction();
  }, [id]);

  // za pridobivanje komentarjev
  useEffect(() => {
    const fetchComments = async () => {
      try {
        const res = await axios.get(`http://localhost:3001/comments/attraction/${id}`);
        setComments(res.data);
      }
      catch (err) {
        console.error("Napaka pri nalaganju komentarjev:", err);
      }
    };

    fetchComments();
  }, [id]);

  // za pravilen izpis naslova
  function formatAddress(address) {
    if (!address)
      return '';

    const parts = [address.street, address.city, address.postalCode, address.country]
      .filter(part => part && part.trim() !== '');
    return parts.join(', ');
  }

  // za pravilen izpis odpiralnega časa
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

  // objava komentarja
  function handleCommentSubmit(e) {
    e.preventDefault();

    axios.post(
      `http://localhost:3001/comments/attraction/${id}`,
      { text: newComment },
      { withCredentials: true }
    )
    .then(() => {
      return axios.get(`http://localhost:3001/comments/attraction/${id}`);
    })
    .then((res) => {
      setComments(res.data);
      setNewComment('');
    })
    .catch((err) => {
      console.error("Napaka pri pošiljanju komentarja:", err);
      alert("Napaka pri pošiljanju komentarja.");
    });
  }

  // brisanje komentarja
  function handleDeleteComment(commentId) {
    if (!window.confirm("Ali ste prepričani, da želite izbrisati ta komentar?")) // če si uporabnik premisli lahko komentar obdrži
      return;

    axios.delete(`http://localhost:3001/comments/${commentId}`, { withCredentials: true })
      .then(() => {
        return axios.get(`http://localhost:3001/comments/attraction/${id}`);
      })
      .then(res => {
        setComments(res.data);
      })
      .catch(err => {
        console.error("Napaka pri brisanju komentarja:", err);
        alert("Napaka pri brisanju komentarja.");
      });
  }

  if (loading)
    return <p>Nalaganje...</p>;

  if (error)
    return <p style={{ color: 'red' }}>{error}</p>;

  if (!attraction)
    return <p>Znamenitost ni bila najdena.</p>;

  return (
    <div>
      <div className="attraction-details-container">
        {/* Ime znamenitosti */}
        <h1>{attraction.name}</h1>

        {/* Slika */}
        {imageUrl && (
          <img src={imageUrl} alt={attraction.name || ''} class="attraction-image-one" onError={(e) => { e.target.style.display = 'none'; }} />
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
            <button type="button" className="btn btn-primary" onClick={() => window.open(attraction.googleMapsLink, '_blank', 'noopener,noreferrer')}>
              Odpri v Google Maps
            </button>
          )}
        </div>
      </div>
      <div>
        <hr />
        {/* del za komentarje */}
        <div>
          <h3>Komentarji</h3>

          <br />
          
          {/* ni še komentarjev */}
          {comments.length === 0 && <p>Ni še komentarjev.</p>}

          {/* če uporabnik ni prijavljen, pokažem obvestilo */}
          {!user && (
            <p style={{ fontStyle: 'italic', color: 'gray' }}>Za komentiranje morate biti prijavljeni.</p>
          )}

          {/* obrazec za objavo komentarja, če je uporabnik prijavljen */}
          {user && (
            <form onSubmit={handleCommentSubmit}>
              <div className="mb-3">
                <textarea className="form-control" rows="3" placeholder="Dodaj komentar..." value={newComment} onChange={(e) => setNewComment(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary">Objavi komentar</button>
            </form>
          )}

          <br /><br />

          {/* seznam komentarjev */}
          <ul className="list-group mb-3">
            {comments.map((comment) => (
              <li key={comment._id} className="list-group-item d-flex justify-content-between align-items-center">
                <div>
                  <strong>{comment.userId?.username || 'Neznan uporabnik'}:</strong> {comment.text}
                  <div className="text-muted" style={{ fontSize: '0.8rem' }}>
                    {new Date(comment.createdAt).toLocaleString()}
                  </div>
                </div>

                {/* brisanje komentarja, gumb se prikaže samo lastniku komentarja */}
                {user && comment.userId?._id === user._id && (
                  <button onClick={() => handleDeleteComment(comment._id)} className="btn btn-sm btn-outline-danger">
                    Izbriši
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Attraction;