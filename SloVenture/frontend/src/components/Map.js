import React, { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';
import sloveniaGeoJson from '../slovenija.json';
import { useNavigate } from 'react-router-dom';

function Map() {
  const mapRef = useRef(); // za izris zemljevida
  const navigate = useNavigate(); // da lahko kliknem na piko in se mi odpre stran z znamenitostjo
  const [attractions, setAttractions] = useState([]);
  const [hoveredAttraction, setHoveredAttraction] = useState(null);
  const [hoverPosition, setHoverPosition] = useState({ x: 0, y: 0 });

  useEffect(() => {
    document.title = "Zemljevid"; // naslov zavihka

    // slika zemljevida v ozadju
    document.body.style.backgroundImage = "url('http://localhost:3001/images/world_map.jpg')";
    document.body.style.backgroundSize = 'cover';
    document.body.style.backgroundPosition = 'center';
    document.body.style.backgroundRepeat = 'no-repeat';
    
    // velikost zemljevida
    const width = 650;
    const height = 450;

    const svg = d3.select(mapRef.current)
      .append('svg')
      .attr('width', width)
      .attr('height', height);

    // kvader kjer se nahaja zemljevid slovenije
    svg.append('rect')
      .attr('width', width)
      .attr('height', height)
      .attr('fill', '#E0F7FA');

    // približno sredina slovenije, da bo prikazana na sredini kvadrata
    const projection = d3.geoMercator()
      .center([14.9, 46.1])
      .scale(10000) // povečam zemljevid, drugače se sploh ne bi videl
      .translate([width / 2, height / 2]);

    const path = d3.geoPath().projection(projection);
    const g = svg.append('g');

    // obris zemljevida ter barva ozadja, da ne bo "luknja"
    g.selectAll('path')
      .data(sloveniaGeoJson.features)
      .enter()
      .append('path')
      .attr('d', path)
      .attr('fill', '#90CAF9')
      .attr('stroke', '#0D47A1')
      .attr('stroke-width', 1)
      .attr('fill-opacity', 1);

    // opcija povečave/pomanjšave
    const zoom = d3.zoom()
      .scaleExtent([1, 8]) // 1 je min velikost, 8 je max
      .on('zoom', (event) => {
        g.attr('transform', event.transform);
      });
    svg.call(zoom);

    const fetchData = async () => {
      try {
        const [attractionsRes, imagesRes] = await Promise.all([
          fetch('http://localhost:3001/attractions'),
          fetch('http://localhost:3001/attraction-images')
        ]);

        const attractionsData = await attractionsRes.json();
        const imagesData = await imagesRes.json();
        const imageMap = {};

        imagesData.forEach(img => {
          const id = img.attractionId?.$oid || img.attractionId;

          if (id)
            imageMap[id] = img.url;
        });

        const parsedData = attractionsData.map((item, index) => {
          const attraction = item.attraction ?? item;
          const location = item.location ?? attraction.location;

          if (!location)
            return null;

          const lat = parseFloat(location.lat?.$numberDecimal ?? location.lat);
          const lon = parseFloat(location.lon?.$numberDecimal ?? location.lon);

          if (isNaN(lat) || isNaN(lon))
            return null;

          const id = attraction._id?.$oid || attraction._id;

          return {
            id,
            name: attraction.name,
            coordinates: [lon, lat],
            image: imageMap[id] || null
          };
        }).filter(Boolean);

        setAttractions(parsedData);

        // na zemljevidu izrišem tudi piko, ki predstavlja znamenitost glede na njene koordinate
        g.selectAll('circle.znamenitost')
          .data(parsedData)
          .enter()
          .append('circle')
          .attr('class', 'znamenitost')
          .attr('cx', d => projection(d.coordinates)[0])
          .attr('cy', d => projection(d.coordinates)[1])
          .attr('r', 4)
          .attr('fill', 'green')
          .style('cursor', 'pointer')
          .on('mouseover', (event, d) => {
            const [x, y] = d3.pointer(event);
            setHoverPosition({ x, y });
            setHoveredAttraction(d);
          })
          .on('mouseout', () => {
            setHoveredAttraction(null);
          })
          .on('click', (event, d) => {
            navigate(`/attractions/${d.id}`); // uporabnika preusmerim na stran, kjer si lahko ogleda podrobnosti znamenitosti
          });
      }
      catch (err) {
        console.error("Napaka pri fetchanju podatkov:", err);
      }
    };

    fetchData();

    // ker trenutno želim imeti sliko zemljevida v ozadju samo tu, returnam za ostale strani null, da tega več ne bo
    return () => {
      document.body.style.backgroundImage = null;
      document.body.style.backgroundSize = null;
      document.body.style.backgroundPosition = null;
      document.body.style.backgroundRepeat = null;
      d3.select(mapRef.current).selectAll('*').remove();
    };
  }, []);

  return (
    <div style={{ textAlign: 'center', position: 'relative' }}>
      <h3 id="map-title">Kam gremo pa danes? :-)</h3>
      <div id="map" ref={mapRef}></div>

      {/* S tem prikažem ob piki ime ter sliko znamenitosti */}
      {hoveredAttraction && (
        <div
          style={{
            position: 'absolute',
            left: hoverPosition.x + 350,
            top: hoverPosition.y - 20,
            backgroundColor: 'white',
            border: '1px solid gray',
            padding: '10px',
            borderRadius: '8px',
            boxShadow: '0 4px 8px rgba(0,0,0,0.2)',
            width: '200px',
            zIndex: 999
          }}
        >
          <strong>{hoveredAttraction.name}</strong>
          {hoveredAttraction.image && (
            <img
              src={hoveredAttraction.image}
              alt={hoveredAttraction.name}
              style={{
                width: '100%',
                marginTop: '10px',
                borderRadius: '6px',
                maxHeight: '120px',
                objectFit: 'cover'
              }}
            />
          )}
        </div>
      )}
    </div>
  );
}

export default Map;