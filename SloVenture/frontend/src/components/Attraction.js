import { useParams } from 'react-router-dom';

function Attraction() {
  const { id } = useParams();

  // testni podatki
  const attractionData = [
    {
      _id: '6825026b5fd5eb530a5b82b4',
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

  const attraction = attractionData.find(a => a._id === id);

  if (!attraction) return <p>Znamenitost ni bila najdena.</p>;

  return (
    <div>
      <h1>{attraction.name}</h1>
      <img src={attraction.url} alt={attraction.name} style={{ width: '400px' }} />
      <p><strong>Lokacija:</strong> {attraction.location}</p>
      <p>{attraction.description}</p>
    </div>
  );
}

export default Attraction;