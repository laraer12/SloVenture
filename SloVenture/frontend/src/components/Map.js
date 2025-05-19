import React, { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import sloveniaGeoJson from '../slovenija.json';
import { useNavigate } from 'react-router-dom';

function Map() {
  const mapRef = useRef(); // za izris zemljevida
  const navigate = useNavigate(); // da lahko kliknem na piko in se mi odpre stran z znamenitostjo

  useEffect(() => {
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

    // kličem api za znamenitosti, da ko kliknem na piko se mi prikaže stran za tisto znamenitost
    fetch('http://localhost:3001/attractions')
      .then(response => response.json())
      .then(data => {
        const parsedData = data.map(item => {
          let coordinates = [0, 0];

          try {
            const locObj = JSON.parse(item.attraction.location);
            const [lat, lon] = locObj.location.coordinates; // vrstni red koordinat je trebalo spremenit, ker so v slovenija.json shranjene drugače kot pa imajo znamenitosti v bazi to shranjeno
            coordinates = [lon, lat];
          } 
          catch (err) {
            console.error('Napaka pri JSON.parse(location):', err);
          }
          return {
            id: item.attraction._id,
            name: item.attraction.name,
            coordinates
          };
        });

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
          .on('click', (event, d) => {
            navigate(`/attractions/${d.id}`); // uporabnika preusmerim na stran, kjer si lahko ogleda podrobnosti znamenitosti
          })
          .append('title') // to se prikaže ko z miško "hover-aš" prek pike, kasneje lahko naredim tak, da bo prikazano kot neka kartica ali okvirček, da se prikažeta slika in ime
          .text(d => d.name);
      })
      .catch(error => {
        console.error('Napaka pri pridobivanju znamenitosti:', error);
      });

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
    <div style={{textAlign: 'center'}}>
      <h3 id="map-title">Kam gremo pa danes? :-)</h3>
      <div id="map" ref={mapRef}></div> {/* tu se dejansko prikaže zemljevid */}
    </div>
  );
}

export default Map;