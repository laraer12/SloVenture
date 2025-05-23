import { useParams } from 'react-router-dom';
import { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { UserContext } from '../userContext';

// za zvezdice pri review
import Rating from '@mui/material/Rating';
import Box from '@mui/material/Box';

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

  // review z zvezdicami
  const [rating, setRating] = useState(0);
  const [ratingFamilyFriendly, setRatingFamilyFriendly] = useState(0);
  const [ratingElderlyFriendly, setRatingElderlyFriendly] = useState(0);
  const [ratingAccessible, setRatingAccessible] = useState(0);
  const [ratingAverages, setRatingAverages] = useState(null);

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
      `http://localhost:3001/comments/attraction/${id}`, // pridobim komentarje za določeno znamenitost
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

    axios.delete(`http://localhost:3001/comments/${commentId}`, { withCredentials: true }) // pridobim komentarje za določeno znamenitost
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

  // review
  useEffect(() => {
    const fetchAverages = async () => {
      try {
        const res = await axios.get(`http://localhost:3001/reviews/averages/${id}`); // pridobim povprečne ocene (in ocene) za določeno znamenitost
        setRatingAverages(res.data);
      }
      catch (err) {
        console.error("Napaka pri nalaganju povprečnih ocen:", err);
      }
    };
    fetchAverages();
  }, [id]);

  function handleReviewSubmit(e) {
    e.preventDefault();

    // ocena ne sme biti manjša od 1
    if (rating < 1 || ratingFamilyFriendly < 1 || ratingElderlyFriendly < 1 || ratingAccessible < 1) {
      alert('Prosim, ocenite vse kategorije z vsaj eno zvezdico (1–5).');
      return;
    }
    const reviewData = {
      userId: user._id,
      attractionId: id,
      rating,
      ratingFamilyFriendly,
      ratingElderlyFriendly,
      ratingAccessible,
      createdAt: new Date(),
    };

    axios
      .post('http://localhost:3001/reviews', reviewData, { withCredentials: true }) // oddam oceno
      .then(() => {
        alert('Hvala za vašo oceno!');

        setRating(0);
        setRatingFamilyFriendly(0);
        setRatingElderlyFriendly(0);
        setRatingAccessible(0);

        return fetchUpdatedAverages(); // tu osvežim povprečne ocene, da so takoj vidne po oddani oceni
      })
      .catch((err) => {
        if (err.response && err.response.status === 409)
          alert('Oceno ste že oddali!');

        else {
          console.error("Napaka pri oddaji ocene:", err);
          alert("Napaka pri oddaji ocene.");
        }
      });
  }

  // funkcija, da se lahko prikažejo brez osvežitve nove povprečne ocene iz izrisi grafov
  const fetchUpdatedAverages = async () => {
    try {
      const res = await axios.get(`http://localhost:3001/reviews/averages/${id}`);
      setRatingAverages(res.data);
    }
    catch (err) {
      console.error("Napaka pri pridobivanju posodobljenih ocen:", err);
    }
  };

  // izgled in struktura grafov
  const RatingBar = ({ distribution, average, title }) => {

    // največji števec med vsemi ocenami (torej največ ljudi, ki je dalo določeno oceno)
    const maxCount = Math.max(...Object.values(distribution));

    return (
      <div style={{ width: '23%', minWidth: 250, marginBottom: '30px' }}>
        <h4>{title}</h4>

        <div style={{ fontWeight: 'bold', marginBottom: 10 }}>
          Povprečje: {average.toFixed(2)} / 5
        </div>
        
        {/* za vsako oceno (1 do 5) narišem vrstico, ki prikazuje, koliko obiskovalcev je dalo to oceno */}
        {Object.entries(distribution).map(([rating, count]) => (
          <div key={rating} style={{ display: 'flex', alignItems: 'center', marginBottom: 6, fontSize: 16, }}>
            <div style={{ width: 20 }}>
              {rating}
            </div>
            <div 
              style={{
                height: 24,
                width: `${(count / maxCount) * 150}px`, // širina vrstice je sorazmerna glede na največje število glasov (maxCount)
                backgroundColor: '#4CAF50',
                marginLeft: 12,
                borderRadius: 4,
                transition: 'width 0.3s ease',
              }}
            />

            {/* število glasov izpisano poleg grafa */}
            <div style={{ marginLeft: 12 }}>
              {count}
            </div>
          </div>
        ))}
      </div>
    );
  };

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
                <textarea className="form-control" rows="3" placeholder={`Komentiraš kot ${user.username}`} value={newComment} onChange={(e) => setNewComment(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary">Objavi komentar</button>
            </form>
          )}

          <br /><br />

          {/* seznam komentarjev */}
          <ul className="list-group mb-3">
            {comments.map((comment) => (
              <li key={comment._id} className="list-group-item d-flex justify-content-between align-items-start">

                {/* profilna slika in vsebina komentarja */}
                <div className="d-flex" style={{ gap: '10px', flex: 1 }}>
                  <img src={`http://localhost:3001/images/${comment.userId?.profilePicture}`} alt="Profilna slika" width="40" height="40" className="profile-picture-comment" onError={(e) => { e.target.onerror = null; e.target.src = 'http://localhost:3001/images/default-profile-picture.jpg'; }} />

                  <div>
                    <strong>{comment.userId?.username || 'Neznan uporabnik'}:</strong> {comment.text}
                    <div className="text-muted" style={{ fontSize: '0.8rem' }}>
                      {new Date(comment.createdAt).toLocaleString()}
                    </div>
                  </div>
                </div>

                {/* brisanje komentarja, gumb se prikaže samo lastniku komentarja */}
                {user && comment.userId?._id === user._id && (
                  <button onClick={() => handleDeleteComment(comment._id)} className="btn btn-sm btn-outline-danger">Izbriši</button>
                )}
              </li>
            ))}
          </ul>
        </div>
      </div>
      <div>
        <hr />
        {/* prikaz ocen, zvezdic, grafov */}
        <h3>Oceni znamenitost</h3>
          
        {/* če uporabnik ni prijavljen ne more podati ocene */}
        {!user && (
          <p style={{ fontStyle: 'italic', color: 'gray' }}>
            Za ocenjevanje morate biti prijavljeni.
          </p>
        )}

        {/* drugače se mu prikaže obrazec za ocene */}
        {user && (
          <>
            <form onSubmit={handleReviewSubmit} className="review-container">
              <div className="review-stars">
                <label>Splošna ocena:</label>
                <Box>
                  <Rating name="general-rating" value={rating} precision={1} onChange={(event, newValue) => setRating(newValue)} required />
                </Box>
              </div>
              <div className="review-stars">
                <label>Primerno za družine:</label>
                <Box>
                  <Rating name="family-rating" value={ratingFamilyFriendly} precision={1} onChange={(event, newValue) => setRatingFamilyFriendly(newValue)} required />
                </Box>
              </div>
              <div className="review-stars">
                <label>Primerno za starejše:</label>
                <Box>
                  <Rating  žname="elderly-rating" value={ratingElderlyFriendly} precision={1} onChange={(event, newValue) => setRatingElderlyFriendly(newValue)} required />
                </Box>
              </div>
              <div className="review-stars">
                <label>Dostopnost:</label>
                <Box>
                  <Rating name="accessible-rating" value={ratingAccessible} precision={1} onChange={(event, newValue) => setRatingAccessible(newValue)} required />
                </Box>
              </div>
            </form>

            <div style={{ marginTop: '8px' }}>
              <button type="submit" className="btn btn-success" onClick={handleReviewSubmit}>Oddaj oceno</button>
            </div>
          </>
        )}
      </div>

      <hr />
      
      {/* prikaz statistike ocen z grafi, povprečje ter število obiskovalcev, ki so glasovali */}
      <h3>Ocene obiskovalcev</h3>
      
      <div className="rating-bars-container">
        {/* se prikaže le v primeru, da je vsaj 1 uporabnik že podal oceno, izrišejo se grafi, izgled je definiran zgoraj, izpiše se povprečje in distrubcije glasov od 1 do 5 */}
        {ratingAverages && ratingAverages.count > 0 && ratingAverages.averages && ratingAverages.averages.rating !== null && ratingAverages.distributions ? (
          <>
            <RatingBar title="Splošna ocena" average={ratingAverages.averages.rating} distribution={ratingAverages.distributions.rating} />

            <RatingBar title="Družinam prijazno" average={ratingAverages.averages.ratingFamilyFriendly} distribution={ratingAverages.distributions.ratingFamilyFriendly} />

            <RatingBar title="Starejšim prijazno" average={ratingAverages.averages.ratingElderlyFriendly} distribution={ratingAverages.distributions.ratingElderlyFriendly} />
            
            <RatingBar title="Dostopnost" average={ratingAverages.averages.ratingAccessible} distribution={ratingAverages.distributions.ratingAccessible} />
          </>
        ) : (
          // v nasprotnem primeru se izpiše spodnje besedilo
          <p style={{ textAlign: 'center', color: 'gray', marginTop: '20px' }}>Trenutno še nihče ni podal ocene.</p>
        )}
      </div>

      {/* če je podan vsaj 1 glas se izpiše, koliko glasov je bilo že podanih za določeno znamenitost */}
      {ratingAverages?.count > 0 && (
        <div style={{ flexBasis: '100%', marginTop: '10px', textAlign: 'center', color: 'gray', }}>
          <strong>Število obiskovalcev, ki so glasovali:</strong> {ratingAverages.count}
        </div>
      )}
    </div>
  );
}

export default Attraction;