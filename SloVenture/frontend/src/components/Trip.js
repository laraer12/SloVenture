import { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams, useNavigate, Link } from 'react-router-dom';

const Trip = () => {
    const { id } = useParams();
    const [trip, setTrip] = useState(null);
    const [attractions, setAttractions] = useState([]);
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        tripName: '',
        tripDescription: '',
        startDate: '',
        endDate: '',
        isPublic: false
    });

    // pridobim slike znamenitosti
    const getImageUrl = (url) => {
        if (!url)
            return `${process.env.REACT_APP_BACKEND_URL}/images/ni_slike.jpg`;

        if (url.startsWith('http://') || url.startsWith('https://'))
            return url;

        return `${process.env.REACT_APP_BACKEND_URL}/images/ni_slike.jpg`;
    };

    useEffect(() => {
        const fetchTrip = async () => {
            try {
                const res = await axios.get(`${process.env.REACT_APP_BACKEND_URL}/trips/${id}`);
                setTrip(res.data.trip);
                setAttractions(res.data.attractions);

                setFormData({
                    tripName: res.data.trip.tripName || '',
                    tripDescription: res.data.trip.tripDescription || '',
                    startDate: res.data.trip.startDate ? res.data.trip.startDate.slice(0, 10) : '',
                    endDate: res.data.trip.endDate ? res.data.trip.endDate.slice(0, 10) : '',
                    isPublic: res.data.trip.isPublic || false
                });
            }
            catch (err) {
                console.error("Napaka pri nalaganju potovanja:", err);
            }
        };

        if (!trip)
            document.title = "Nalaganje izleta...";

        fetchTrip();
        }, [id]);

        useEffect(() => {
            if (trip)
                document.title = trip.tripName || "Moje potovanje";
    }, [trip]);

    // sprememba podatkov izleta
    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;

        setFormData((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    // shranim spremembe
    const handleSave = async (e) => {
        e.preventDefault();

        const { tripName, tripDescription, startDate, endDate } = formData;

        if (!tripName.trim() || !tripDescription.trim() || !startDate) {
            alert('Prosim izpolnite vsa obvezna polja.');
            return;
        }
        if (endDate && new Date(endDate) < new Date(startDate)) {
            alert('Datum konca izleta ne more biti manjši od datuma začetka.');
            return;
        }
        try {
            await axios.put(`${process.env.REACT_APP_BACKEND_URL}/trips/${id}`, formData);
            alert('Potovanje posodobljeno!');
            navigate('/trips');
        }
        catch (err) {
            console.error("Napaka pri shranjevanju:", err);
            alert('Pri shranjevanju je prišlo do napake. Poskusite znova.');
        }
    };

    if (!trip)
        return <p className="text-center mt-4">Nalaganje...</p>;

    return (
        <div className="container mt-5">
            <div className="row">

                {/* podatki o izletu */}
                <div className="col-md-4 d-flex flex-column justify-content-start">
                    <h1 className="mb-4">{trip.tripName}</h1>
                    <p className="lead" style={{ wordWrap: 'break-word', overflowWrap: 'break-word', whiteSpace: 'normal', }}>
                        {trip.tripDescription}
                    </p>
                    <p><strong>Začetek izleta:</strong> {new Date(trip.startDate).toLocaleDateString()}</p>
                    <p><strong>Konec izleta:</strong> {trip.endDate ? new Date(trip.endDate).toLocaleDateString() : 'Ni določen'}</p>
                </div>

                {/* znamenitost */}
                <div className="col-md-8">
                    {attractions.length === 0 ? (
                        <p>Ni dodanih znamenitosti.</p>
                    ) : (
                        <Link to={`/attractions/${attractions[0].attraction._id}`} className="text-decoration-none text-dark">
                        <div className="card">
                            <img 
                                src={getImageUrl(attractions[0]?.images?.[0]?.url)} 
                                className="card-img-top"
                                alt={attractions[0]?.attraction?.name || 'Znamenitost'}
                                style={{ objectFit: 'cover', height: '300px' }}
                            />
                            <div className="card-body">
                            <h5 className="card-title">{attractions[0].attraction.name}</h5>
                            </div>
                        </div>
                        </Link>
                    )}
                </div>
            </div>

            <hr className="my-5" />
                
            {/* urejanje podatkov izleta */}
            <h2 className="h4 mt-5 mb-3">Uredi podatke potovanja</h2>
            <form onSubmit={handleSave}>
                <div className="mb-3">
                    <label className="form-label">Naziv</label>
                    <input type="text" name="tripName" className="form-control" value={formData.tripName} onChange={handleChange} required />
                </div>

                <div className="mb-3">
                    <label className="form-label">Opis</label>
                    <textarea name="tripDescription" className="form-control" value={formData.tripDescription} onChange={handleChange} rows="3" />
                </div>

                <div className="mb-3">
                    <label className="form-label">Začetek izleta</label>
                    <input type="date" name="startDate" className="form-control" value={formData.startDate} onChange={handleChange} />
                </div>

                <div className="mb-3">
                    <label className="form-label">Konec izleta</label>
                    <input type="date" name="endDate" className="form-control" value={formData.endDate} onChange={handleChange} />
                </div>

                <button type="submit" className="btn btn-primary">Shrani spremembe</button>
            </form>
        </div>
    );
};

export default Trip;