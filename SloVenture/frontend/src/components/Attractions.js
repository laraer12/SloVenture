import { Link } from 'react-router-dom';

function Attractions() {
    // testni podatki da vidim, kak bi zgledalo to v karticah
  const attractions = [
    {
        _id: '1',
        name: 'Triglav',
        url: 'http://localhost:3001/images/triglav_test.webp',
        location: 'Julijske Alpe',
        description: 'Najvišja gora v Sloveniji, priljubljena destinacija za planinarjenje.',
    },
    {
        _id: '2',
        name: 'Bled',
        url: 'http://localhost:3001/images/bled_test.jpg',
        location: 'Bled',
        description: 'Znamenito jezero s čudovitim otočkom in gradom.',
    },
    {
        _id: '3',
        name: 'Postojnska jama',
        url: 'http://localhost:3001/images/postojnska-jama_test.webp',
        location: 'Postojna',
        description: 'Ena največjih kraških jam v Evropi.',
    },
  ];

  return (
    <div className="attractions-container">
        {attractions.map((attraction) => (
            <Link to={`/attractions/${attraction._id}`} key={attraction._id} className="attraction-card-link">
                <div className="attraction-card">
                    <img src={attraction.url} alt={attraction.name} className="attraction-image" />
                    <h3 className="attraction-name">{attraction.name}</h3>
                    <p className="attraction-location">{attraction.location}</p>
                    <p className="attraction-description">{attraction.description}</p>
                </div>
            </Link>
        ))}
    </div>
  );
}

export default Attractions;