import React, { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';
import sloveniaGeoJson from '../slovenija.json';
import { useNavigate } from 'react-router-dom';

function Map() {
  const mapRef = useRef(); // za izris zemljevida
  const navigate = useNavigate(); // da lahko kliknem na piko in se mi odpre stran z znamenitostjo
  const [attractions, setAttractions] = useState([]);
  const [loading, setLoading] = useState(true);

  // za slike znamenitosti
  const [hoveredAttraction, setHoveredAttraction] = useState(null);
  const [hoverPosition, setHoverPosition] = useState({ x: 0, y: 0 });
  const [popupPos, setPopupPos] = useState({ left: 0, top: 0 });
  const popupRef = useRef(null);
  
  // barve pikic po regijah
  const [regionColors, setRegionColors] = useState({});
  const [uniqueRegionIds, setUniqueRegionIds] = useState([]);
  const [regionIdToName, setRegionIdToName] = useState({});

  // filtri za klasifikacije
  const [selectedClassification, setSelectedClassification] = useState('');
  const [classifications, setClassifications] = useState([]);

  // pridobim klasifikacije
  useEffect(() => {
    const fetchClassifications = async () => {
      try {
        const res = await fetch(`${process.env.REACT_APP_BACKEND_URL}/attractions/classifications`);
        const data = await res.json();

        setClassifications(data);
      }
      catch (err) {
        console.error("Napaka pri pridobivanju klasifikacij:", err);
      }
    };

    fetchClassifications();
  }, []);

  useEffect(() => {
    document.title = "Zemljevid"; // naslov zavihka
    d3.select(mapRef.current).selectAll('*').remove(); // da lahko prikazujem za posamezne klasifikacije pike

    // velikost zemljevida
    const width = window.innerWidth;
    const height = window.innerHeight;

    // sedaj povečano na celoten ekran in responsive
    const svg = d3.select(mapRef.current)
      .append('svg')
      .attr('width', '100%')
      .attr('height', '100%')
      .attr('viewBox', `0 0 ${width} ${height}`)
      .attr('preserveAspectRatio', 'xMidYMid meet');

    // približno sredina slovenije, da bo prikazana na sredini kvadrata
    const projection = d3.geoMercator()
      .center([14.9, 46.1])
      .scale(17000) // povečam zemljevid, drugače se sploh ne bi videl
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
        const attractionsRes = await fetch(
          selectedClassification
            ? `${process.env.REACT_APP_BACKEND_URL}/attractions/classification/${encodeURIComponent(selectedClassification)}` // klasifikacije
            : `${process.env.REACT_APP_BACKEND_URL}/attractions` // znamenitosti
        );
        const imagesRes = await fetch(`${process.env.REACT_APP_BACKEND_URL}/attraction-images`); // slike znamenitosti
        const regionsRes = await fetch(`${process.env.REACT_APP_BACKEND_URL}/regions`); // pridobim še regije

        const attractionsData = await attractionsRes.json();
        const imagesData = await imagesRes.json();
        const regionsData = await regionsRes.json();

        // da se na legendi izpiše ime regije, ne pa njen ID
        const regionIdToName = {};
        regionsData.forEach(region => {
          const extractId = (val) => {
            if (!val)
              return null;

            if (typeof val === 'object')
              return val.$oid || val._id || null;
            
            return val;
          };

          const id = extractId(region._id || region.id);
          const name = region.name || region.naziv || "Nepoznana regija";
          regionIdToName[id] = name;
        });

        // slike
        const imageMap = {};

        imagesData.forEach(img => {
          const id = img.attractionId?.$oid || img.attractionId;

          if (!id)
            return;

          if (!img.url || !(img.url.startsWith('http://') || img.url.startsWith('https://')))
            return;

          if (!imageMap[id])
            imageMap[id] = [];

          imageMap[id].push(img.url);
        });

        // znamenitosti
        const parsedData = attractionsData.map(item => {
          const attraction = item.attraction ?? item;
          const location = item.location ?? attraction.location;

          if (!location)
            return null;

          const lat = parseFloat(location.lat?.$numberDecimal ?? location.lat);
          const lon = parseFloat(location.lon?.$numberDecimal ?? location.lon);

          if (isNaN(lat) || isNaN(lon))
            return null;

          const id = attraction._id?.$oid || attraction._id;

          // pridobim ID iz regije
          const extractId = (val) => {
            if (!val)
              return null;

            if (typeof val === 'object') {
              if ('$oid' in val)
                return val.$oid;

              if ('_id' in val)
                return val._id;
            }
            return val;
          };

          const regionId = extractId(attraction.regionId)?.toString();
          const firstImage = imageMap[id]?.[0] || null;

          return {
            id,
            name: attraction.name,
            coordinates: [lon, lat],
            regionId,
            image: firstImage
          };
        }).filter(Boolean);

        setAttractions(parsedData);

        // nastavim različne barve za različne regije
        const uniqueRegionIds = [...new Set(parsedData.map(d => d.regionId).filter(Boolean))];
        setUniqueRegionIds(uniqueRegionIds);

        const colorScale = d3.scaleOrdinal(d3.schemeCategory10).domain(uniqueRegionIds);

        const regionColors = Object.fromEntries(uniqueRegionIds.map(id => [id, colorScale(id)]));
        setRegionColors(regionColors);

        setRegionIdToName(regionIdToName);
        setLoading(false);

        // na zemljevidu izrišem tudi piko, ki predstavlja znamenitost glede na njene koordinate
        g.selectAll('circle.znamenitost')
          .data(parsedData)
          .enter()
          .append('circle')
          .attr('class', 'znamenitost')
          .attr('cx', d => projection(d.coordinates)[0])
          .attr('cy', d => projection(d.coordinates)[1])
          .attr('r', 4)

          // vsaki regiji dodam svojo barvo, če slučajno nima podane regije se pika obarva črno
          .attr('fill', d => regionColors[d.regionId] || 'black')
          .style('cursor', 'pointer')
          .on('mouseover', (event, d) => {
            const [x, y] = d3.pointer(event);

            setHoverPosition({ x, y });
            setHoveredAttraction(d);
          })
          .on('mouseout', () => {
            setHoveredAttraction(null); // da ni prikazana prejšnja znamenitost, kjer je uporabnik šel čez z miško
          })
          .on('click', (event, d) => {
            navigate(`/attractions/${d.id}`); // uporabnika preusmerim na stran, kjer si lahko ogleda podrobnosti znamenitosti
          });
      }
      catch (err) {
        console.error("Napaka pri fetchanju podatkov:", err);
        setLoading(false);
      }
    };

    fetchData();

    return () => {
      d3.select(mapRef.current).selectAll('*').remove();
    };
  }, [selectedClassification]);

  return (
    <div id="map">
      <div ref={mapRef} style={{ width: '100%', height: '100%' }} />

      {/* Legenda */}
      <div id="legenda">
        <h3 style={{ marginTop: 0, marginBottom: 10 }}>Legenda regij</h3>

        {loading && <div>Nalaganje...</div>}
        {!loading && uniqueRegionIds.length === 0 && <div>Ni regij za prikaz</div>}

        {/* izpis seznama regij */}
        {!loading && uniqueRegionIds.length > 0 && (
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {uniqueRegionIds.map(regionId => (
              <li key={regionId} style={{ display: 'flex', alignItems: 'center', marginBottom: 6 }}>
                <div id="region" style={{ backgroundColor: regionColors[regionId] || 'black' }} />
                <span>{regionIdToName[regionId] || regionId}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
      
      {/* Filtri */}
      <div id="filtri">
        <span style={{ fontWeight: 'bold', marginBottom: '5px' }}>Filtriraj po klasifikaciji:</span>
        <select value={selectedClassification} onChange={(e) => setSelectedClassification(e.target.value)} style={{ width: '100%' }}>
          <option value="">Vse</option>
          {classifications.map(c => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>
      
      {/* S tem prikažem ob piki ime ter sliko znamenitosti */}
      {hoveredAttraction && (
        <div id="attraction-info"
          ref={popupRef}
          style={{ left: hoverPosition.x + 10, top: hoverPosition.y - 20, }}
        >
          <strong>{hoveredAttraction.name}</strong>
          {hoveredAttraction.image && (
            <img id="attraction-info-image" src={hoveredAttraction.image} alt={hoveredAttraction.name} />
          )}
        </div>
      )}
    </div>
  );
}

export default Map;