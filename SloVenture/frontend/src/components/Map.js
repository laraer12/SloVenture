function Map() {
  return (
    <div id="map">
      <img
        src="http://localhost:3001/images/placeholder_slovenija.jpg"
        alt="Zemljevid Slovenije"
        style={{ width: '100%', maxWidth: '600px', borderRadius: '12px' }}
      />
      <p style={{ marginTop: '8px' }}>To je začasni prikaz – končni zemljevid bo interaktiven z D3.js.</p>
    </div>
  );
}

export default Map;