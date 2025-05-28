import { useParams, Link } from 'react-router-dom';
import { useEffect, useState, useContext, useRef } from 'react';
import axios from 'axios';
import { UserContext } from '../userContext';

// za zvezdice pri review
import Rating from '@mui/material/Rating';
import Box from '@mui/material/Box';

// podobno kot pri Attractions.js da imam navigacijske puščice da vidim vse slike
import { ChevronLeft, ChevronRight } from 'lucide-react';

function Attraction() {
  const { id } = useParams();
  const [attraction, setAttraction] = useState(null);

  // da lahko prikažem vse slike imam sedaj polje in indexe slik
  const [images, setImages] = useState([]);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
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
  
  // da uporabnik lahko doda svojo sliko znamenitosti
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef();

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
        setImages(images);  // shranim vse slike
        setCurrentImageIndex(0); // nastavim prvo sliko kot trenutno

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

  // puščice za navigacijo med slikami znamenitosti
  const handlePrev = () => {
    setCurrentImageIndex((prev) =>
      (prev - 1 + images.length) % images.length
    );
  };

  const handleNext = () => {
    setCurrentImageIndex((prev) =>
      (prev + 1) % images.length
    );
  };

  // pridobim slike iz baze
  const getImageUrl = () => {
    if (images.length === 0)
      return 'http://localhost:3001/images/ni_slike.jpg';

    const url = images[currentImageIndex]?.url;

    if (!url)
      return 'http://localhost:3001/images/ni_slike.jpg';

    if (url.startsWith('http://') || url.startsWith('https://'))
      return url; // že popoln URL

    return `http://localhost:3001${url}`;
  };

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

  // da uporabnik doda svojo sliko znamenitosti
  const handleAttractionImageUpload = async (e) => {
    e.preventDefault();

    // če ni dodal nobene slike
    if (!fileInputRef.current || !fileInputRef.current.files[0])
      return;

    const file = fileInputRef.current.files[0];

    const formData = new FormData();
    formData.append('source', file);
    formData.append('attractionId', id); // dodam id znamenitosti, da se bo vedelo, kam je dodal sliko
    formData.append('uploadedBy', user._id); // in pridobim id uporabnika, da se ve, kdo je objavil sliko

    setUploading(true);
    setError(null);

    try {
      const res = await fetch('http://localhost:3001/attraction-images/upload-attraction-image', { // api za dodajanje slike
        method: 'POST',
        body: formData,
        credentials: 'include',
      });

      if (!res.ok)
        throw new Error('Napaka pri nalaganju slike');

      const data = await res.json();

      // doda se nova slika v images, da ni potreben refresh za njen prikaz
      setImages(prevImages => [...prevImages, data]);

      // resetiram input, da uporabnik ne bo spammal te slike
      fileInputRef.current.value = '';

      setSelectedFile(null);
    }
    catch (err) {
      setError(err.message);
    }
    finally {
      setUploading(false);
    }
  };

  // funkcija za admina, s katero lahko izbriše sliko
  const handleDeleteImage = async () => {
    if (!window.confirm("Ali ste prepričani, da želite izbrisati to sliko?")) // če si admin premisli lahko sliko obdrži
      return;

    const imageToDelete = images[currentImageIndex];

    if (!imageToDelete || !imageToDelete._id) {
      setError('Slika ni veljavna.');
      return;
    }
    try {
      const res = await fetch(`http://localhost:3001/attraction-images/${imageToDelete._id}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!res.ok)
        throw new Error('Napaka pri brisanju slike.');

      // posodobim seznam slik in indeks
      const newImages = images.filter((img, i) => i !== currentImageIndex);
      setImages(newImages);

      setCurrentImageIndex((prev) =>
        newImages.length === 0 ? 0 : Math.max(0, prev - 1)
      );
    }
    catch (err) {
      setError(err.message);
    }
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

        {/* Slike */}
        <div className="image-wrapper relative">
          <img key={getImageUrl()} src={getImageUrl()} alt={attraction.name || 'Znamenitost'} className="attraction-images fade-image" />

          {/* Puščici */}
          {images.length > 1 && (
            <>
              <button className="nav-button left" onClick={handlePrev}>
                <ChevronLeft />
              </button>
              <button className="nav-button right" onClick={handleNext}>
                <ChevronRight />
              </button>
            </>
          )}

          <br />
          {/* gumb za izbris slike, ki se prikaže samo adminu */}
          {user?.isAdmin && images.length > 0 && (
            <div className="delete-button">
              <button className="btn btn-danger" onClick={handleDeleteImage}>
                Izbriši trenutno prikazano sliko
              </button>
            </div>
          )}
        </div>

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
        
        {/* obrazec, kamor uporabnika lahko doda svojo sliko znamenitosti, prikaz samo prijavljenemu uporabniku */}
        {user && (
          <div className="image-upload-form">
            <h3>Dodaj svojo sliko znamenitosti</h3>

            <br />

            <input type="file" accept="image/*" ref={fileInputRef} onChange={(e) => setSelectedFile(e.target.files[0])} style={{ marginRight: '15px'}}/>

            <button className="btn btn-primary" onClick={handleAttractionImageUpload} disabled={uploading}>
              {uploading ? 'Nalaganje...' : 'Dodaj sliko'}
            </button>

            {error && <p style={{ color: 'red' }}>{error}</p>}
          </div>
        )}

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
                    <strong>
                      <Link to={`/profile/${comment.userId?._id}`} className="profile-link">
                        {comment.userId?.username || 'Neznan uporabnik'}
                      </Link>
                    </strong>: {comment.text}
                    <div className="text-muted" style={{ fontSize: '0.8rem' }}>
                      {new Date(comment.createdAt).toLocaleString()}
                    </div>
                  </div>
                </div>

                {/* brisanje komentarja, gumb se prikaže samo lastniku komentarja ali adminu */}
                {user && (comment.userId?._id === user._id || user.isAdmin) && (
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
                  <Rating name="elderly-rating" value={ratingElderlyFriendly} precision={1} onChange={(event, newValue) => setRatingElderlyFriendly(newValue)} required />
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