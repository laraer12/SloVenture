import { useParams, Link, useNavigate } from 'react-router-dom';
import { useEffect, useState, useContext, useRef } from 'react';
import axios from 'axios';
import { UserContext } from '../userContext';

// za zvezdice pri review
import Rating from '@mui/material/Rating';
import Box from '@mui/material/Box';

// podobno kot pri Attractions.js da imam navigacijske puščice da vidim vse slike
import { ChevronLeft, ChevronRight } from 'lucide-react';

// za izris vremena kot graf
import { Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, ComposedChart, Bar } from 'recharts';

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

  // vreme
  const [weatherData, setWeatherData] = useState(null);
  const [loadingWeather, setLoadingWeather] = useState(true);
  const [errorWeather, setErrorWeather] = useState(null);

  // da uporabnik lahko shrani obiskano znamenitost
  const [visitDate, setVisitDate] = useState('');
  const [visitSuccess, setVisitSuccess] = useState(false);
  const [userVisits, setUserVisits] = useState([]);

  // potovanje
  const [tripName, setTripName] = useState('');
  const [tripDescription, setTripDescription] = useState('');
  const [order, setOrder] = useState('');
  const [plannedVisitTime, setPlannedVisitTime] = useState('');
  const [trips, setTrips] = useState([]);

  useEffect(() => {
    document.title = "Nalaganje znamenitosti..."; // naslov zavihka, dokler se znamenitost ne naloži

    // za pridobivanje znamenitosti
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

    // za pridobivanje vremena
    const fetchWeather = async () => {
      try {
        const res = await axios.get(`http://localhost:3001/weather-data/by-attraction/${id}`);
        const forecast = res.data.forecast;

        if (!forecast || forecast.length === 0)
          setErrorWeather("Trenutno vremenska napoved ni na voljo");
        
        else
          setWeatherData(res.data);
      }
      catch (error) {
        if (error.response?.status === 404)
          setErrorWeather("Trenutno vremenska napoved ni na voljo");
        
        else
          setErrorWeather("Napaka pri nalaganju vremenskih podatkov.");
      }
      finally {
        setLoadingWeather(false);
      }
    };

    fetchAttraction();
    fetchWeather();
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
    if (images.length === 0) return null;

    const url = images[currentImageIndex]?.url;

    if (!url) return null;

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
    if (!user || !user.isAdmin) {
      setError('Nimate dovoljenja za brisanje slike.');
      return;
    }
    if (!window.confirm("Ali ste prepričani, da želite izbrisati to sliko?"))
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

      if (!res.ok) {
        const errData = await res.json();
        throw new Error(errData.message || 'Napaka pri brisanju slike.');
      }
      const newImages = images.filter((_, i) => i !== currentImageIndex);
      setImages(newImages);
      setCurrentImageIndex((prev) =>
        newImages.length === 0 ? 0 : Math.max(0, prev - 1)
      );
    }
    catch (err) {
      console.error(err);
      setError(err.message || 'Napaka pri brisanju slike.');
    }
  };

  // pridobi obstoječe obiske tega uporabnika
  useEffect(() => {
    const fetchUserVisits = async () => {
      try {
        const res = await axios.get(`http://localhost:3001/user-visit/user/${user._id}`, {
          withCredentials: true,
        });

        setUserVisits(res.data);
      }
      catch (err) {
        console.error('Napaka pri pridobivanju obiskov:', err);
      }
    };

    if (user)
      fetchUserVisits();
  }, [user]);

  // shranjevanje obiska
  const handleVisitSubmit = async () => {
    if (!visitDate) {
      alert('Prosim izberite datum obiska.');
      return;
    }

    // datum obiska znamenitosti
    const alreadyVisited = userVisits.some((visit) => {
      const visitedDate = new Date(visit.visitDate).toISOString().split('T')[0];
      return visit.attractionId === attraction._id && visitedDate === visitDate;
    });

    // če je na izbran datum že shranil obisk je napaka
    if (alreadyVisited) {
      alert('Na ta datum ste že označili znamenitost kot obiskano.');
      return;
    }

    // drugače shranim
    try {
      await axios.post(
        'http://localhost:3001/user-visit',
        {
          userId: user._id,
          attractionId: attraction._id,
          visitDate: visitDate,
        },
        { withCredentials: true }
      );

      alert('Obisk uspešno shranjen!');
      setVisitSuccess(true);
      setVisitDate('');
      setUserVisits([...userVisits, { attractionId: attraction._id, visitDate }]);
    }
    catch (err) {
      if (err.response && err.response.status === 409)
        alert('Na ta datum ste že označili znamenitost kot obiskano.');
      
      else {
        console.error('Napaka pri shranjevanju obiska:', err);
        alert('Napaka pri shranjevanju obiska.');
      }
    }
  };

  // potovanje
  const handleAddTrip = async (e) => {
    e.preventDefault();

    if (!tripName.trim() || !tripDescription.trim() || !order || !plannedVisitTime) {
      alert("Prosim izpolnite vsa polja.");
      return;
    }

    // preverim, če je uporabnik že izbral številko zaporedja izleta
    const isOrderTaken = trips.some(trip => trip.order === Number(order));

    if (isOrderTaken) {
      alert("Izbrano zaporedje je že zasedeno. Prosim izberite drugo številko.");
      return;
    }

    // preverim, če je na ta datum že neko drugo potovanje
    const isDateTaken = trips.some(trip =>
      trip.startDate &&
      new Date(trip.startDate).toDateString() === new Date(plannedVisitTime).toDateString()
    );

    if (isDateTaken) {
      alert("Na ta datum že imate izlet. Prosim izberite drug datum.");
      return;
    }

    try {
      const tripRes = await axios.post(
        'http://localhost:3001/trips',
        {
          userId: user._id,
          tripName,
          tripDescription,
          startDate: plannedVisitTime,
          endDate: null,
          isPublic: false,
          createdAt: new Date(),
        },
        {withCredentials: true }
      );

      const newTripId = tripRes.data._id;

      await axios.post(
        'http://localhost:3001/trip-attractions',
        {
          tripId: newTripId,
          attractionId: attraction._id,
          order: Number(order),
          tripDescription,
          plannedVisitTime
        },
        { withCredentials: true }
      );

      alert("Izlet uspešno shranjen!");
      setTripName('');
      setTripDescription('');
      setOrder('');
      setPlannedVisitTime('');
    }
    catch (err) {
      console.error("Napaka pri dodajanju izleta:", err);
      alert("Napaka pri shranjevanju izleta. Poskusi znova.");
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
          {getImageUrl() && (
            <img key={getImageUrl()} src={getImageUrl()} alt={attraction.name || 'Znamenitost'} className="attraction-images fade-image" />
          )}

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
          {user && user.isAdmin && images.length > 0 && (
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

      <hr />
      
      {/* Vreme */}
      {loadingWeather ? (
        <p>Nalaganje vremenskih podatkov...</p>
      ) : errorWeather ? (
        <p>{errorWeather}</p>
      ) : !weatherData?.forecast?.length ? (
        <p>Ni vremenskih podatkov za to znamenitost.</p>
      ) : (
        <>
          <h2>Vremenska napoved</h2>
          <p>Zadnja posodobitev: {new Date(weatherData.lastUpdated).toLocaleString()}</p>

          <ResponsiveContainer width="100%" height={350}> {/* graf je prilagodljiv glede na velikost ekrana */}
            <ComposedChart /* kombiniram lahko črte, stolpce ipd. */

              /* vsi potrebni podakti za vreme */
              data={weatherData.forecast.map(entry => ({
                date: new Date(entry.date).toLocaleDateString(),
                maxTemperature: entry.maxTemperature,
                minTemperature: entry.minTemperature,
                condition: entry.condition,
                precipitationProbability: entry.precipitationProbabilityMax
              }))}
            >
              <CartesianGrid strokeDasharray="3 3" /> {/* mreža */}

              {/* katere vrednosti so prikazane na kateri osi */}
              <XAxis dataKey="date" />
              <YAxis yAxisId="left" label={{ value: 'Temperatura (°C)', angle: -90, position: 'insideLeft' }} />
              <YAxis yAxisId="right" orientation="right" domain={[0, 100]} label={{ value: 'Padavine (%)', angle: -90, position: 'insideRight' }} />
              
              {/* ko uporabnik "hovera" o grafu se mu izpiše ta vsebina */}
              <Tooltip
                content={({ active, payload, label }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;

                    return (
                      <div style={{ backgroundColor: "#FFF", border: "1px solid #CCC", padding: "10px" }}>
                        <strong>{label}</strong><br />
                        <span style={{ color: "#FF7300" }}>Max temp.: {data.maxTemperature}°C</span><br />
                        <span style={{ color: "#387908" }}>Min temp.: {data.minTemperature}°C</span><br />
                        <span style={{ color: "#3498db" }}>Možnost padavin: {data.precipitationProbability}%</span><br />
                        <span>Vreme: {data.condition}</span>
                      </div>
                    );
                  }
                  return null;
                }}
              />

              {/* legenda, ki prikazuje kaj kaj pomeni na grafu */}
              <Legend />
              <Line yAxisId="left" type="monotone" dataKey="maxTemperature" name="Max temp." stroke="#FF7300" />
              <Line yAxisId="left" type="monotone" dataKey="minTemperature" name="Min temp." stroke="#387908" />
              <Bar yAxisId="right" dataKey="precipitationProbability" name="Padavine (%)" fill="#3498db" barSize={20} />
            </ComposedChart>
          </ResponsiveContainer>
        </>
      )}

      {/* Obisk znamenitosti */}
      {user && (
        <div className="visit-section">
          <hr />

          <h4>Ste obiskali to znamenitost?</h4>

          <br />

          <input type="date" value={visitDate} onChange={(e) => setVisitDate(e.target.value)} />
          <button onClick={handleVisitSubmit} className="btn btn-success" style={{ marginLeft: 10 }}>
            Označi kot obiskano
          </button>
          <hr />
        </div>
      )}

      {/* Izlet */}
      {user && (
        <>
        <div className="add-trip-container container mt-4 p-4 border rounded bg-light">
          <h2 className="mb-4">Dodaj izlet</h2>
          <form onSubmit={handleAddTrip}>
            <div className="mb-3">
              <label htmlFor="tripName" className="form-label">Ime izleta:</label>
              <input type="text" id="tripName" className="form-control" value={tripName} onChange={e => setTripName(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label htmlFor="tripDescription" className="form-label">Opis izleta:</label>
              <textarea id="tripDescription" className="form-control" rows="3" value={tripDescription} onChange={e => setTripDescription(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label htmlFor="order" className="form-label">Zaporedje:</label>
              <input type="number" id="order" className="form-control" value={order} onChange={e => setOrder(e.target.value)} required />
            </div>
            <div className="mb-4">
              <label htmlFor="plannedVisitTime" className="form-label">Načrtovani datum obiska:</label>
              <input type="date" id="plannedVisitTime" className="form-control" value={plannedVisitTime} onChange={e => setPlannedVisitTime(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary w-100">Shrani izlet</button>
          </form>
        </div>
        <br />
        <hr />
        </>
      )}

      <div>
        {/* obrazec, kamor uporabnik lahko doda svojo sliko znamenitosti, prikaz samo prijavljenemu uporabniku */}
        {user && (
          <div className="image-upload-form">
            <h3>Dodaj svojo sliko znamenitosti</h3>

            <br />

            <input type="file" accept="image/*" ref={fileInputRef} onChange={(e) => setSelectedFile(e.target.files[0])} style={{ marginRight: '15px'}}/>

            <button className="btn btn-primary" onClick={handleAttractionImageUpload} disabled={uploading}>
              {uploading ? 'Nalaganje...' : 'Dodaj sliko'}
            </button>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            <hr />
          </div>
        )}

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
        <hr />
      </div>

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