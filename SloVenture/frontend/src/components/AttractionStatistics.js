import { useEffect, useState, useContext, useRef } from 'react';
import axios from 'axios';
import * as d3 from 'd3';

function AttractionStatistics() {
    const [visitsByAttraction, setVisitsByAttraction] = useState([]);
    const [ratingsByAttraction, setRatingsByAttraction] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadingVisits, setLoadingVisits] = useState(true);
    const [loadingRatings, setLoadingRatings] = useState(true);

    const [error, setError] = useState(null);
    const visitsChartRef = useRef();
    const ratingsChartRef = useRef();

    document.title = "Statistika znamenitosti"; 

    const fetchVisitsByAttraction = async () => {
        try {
            const response = await axios.get(`${process.env.REACT_APP_BACKEND_URL}/user-visit/visits-by-attraction`);
            setVisitsByAttraction(response.data);
        } catch (err) {
            setError('Napaka pri pridobivanju statistike znamenitosti.');
        } finally {
            setLoadingVisits(false);
        }
    };

    const fetchRatingsByAttraction = async () => {
        try {
            const response = await axios.get(`${process.env.REACT_APP_BACKEND_URL}/reviews/ratings-by-attraction`);
            setRatingsByAttraction(response.data);
            console.log(response.data);
        } catch (err) {
            setError('Napaka pri pridobivanju ocen znamenitosti.');
        } finally {
            setLoadingRatings(false);
        }
    };

    useEffect(() => {
        fetchVisitsByAttraction();
        fetchRatingsByAttraction();
    }, []);

    useEffect(() => {
        if(!loadingVisits && visitsByAttraction.length > 0 && visitsChartRef.current){
            drawVisitsChart();
        }
    }, [loadingVisits, visitsByAttraction, visitsChartRef.current]);

    useEffect(() => {
        if(!loadingRatings && ratingsByAttraction.length > 0 && ratingsChartRef.current){
            drawRatingsChart();
        }
    }, [loadingRatings, ratingsByAttraction, ratingsChartRef.current]);

    const drawVisitsChart = () => {
        const data = visitsByAttraction; 
        
        const width = 928;
        const height = 500;
        const marginTop = 40;
        const marginRight = 0;
        const marginBottom = 120;
        const marginLeft = 60;

        // horizontalna skala (x)
        const x = d3.scaleBand()
            .domain(d3.sort(data, d => -d.totalVisits).map(d => d.attractionName))
            .range([marginLeft, width - marginRight])
            .padding(0.1);

        const xAxis = d3.axisBottom(x)
          .tickSizeOuter(0)
          .tickFormat(d => d.length > 20 ? d.slice(0, 20) + '...' : d);

        // vertikalna skala (y)
        const y = d3.scaleLinear()
            .domain([0, d3.max(data, d => d.totalVisits)]).nice()
            .range([height - marginBottom, marginTop]);

        // SVG container s viewBox in responsive nastavitvijo
        d3.select(visitsChartRef.current).selectAll("*").remove();
        const svg = d3.create("svg")
            .attr("viewBox", [0, 0, width, height])
            .attr("width", width)
            .attr("height", height)
            .attr("style", "max-width: 100%; height: auto;")

        // Dodamo skupino za stolpce
        const barsGroup = svg.append("g")
            .attr("class", "bars")
            .attr("fill", "#E4A8F0")
            .style("cursor", "pointer");

        barsGroup.selectAll("rect")
            .data(data)
            .join("rect")
                .attr("x", d => x(d.attractionName))
                .attr("y", d => y(d.totalVisits))
                .attr("height", d => y(0) - y(d.totalVisits))
                .attr("width", x.bandwidth())
                .on("click", (event, d) => {
                    window.location.href = `/attractions/${d.attractionId}`;
                })
            .on("mouseover", function(event, d) {
                d3.select(this)
                    .attr("fill", "#D678E3")
                    .attr("transform", `scale(1.05)`)
                    .attr("transform-origin", `${x(d.attractionName) + x.bandwidth()/2} ${y(d.totalVisits) + (y(0) - y(d.totalVisits))/2 + ( 0.5 * (y(0) - y(d.totalVisits)))}`);
            })
            .on("mouseout", function(event, d) {
                d3.select(this)
                    .attr("fill", "#E4A8F0")
                    .attr("transform", "scale(1)");
            });

        // X os
        const xAxisGroup = svg.append("g")
            .attr("class", "x-axis")
            .attr("transform", `translate(0,${height - marginBottom})`)
            .call(xAxis)
            .selectAll("text")
                .attr("transform", "rotate(-45)")
                .style("text-anchor", "end")
                .style("font-size", "1.2em");

        // Y os z celoštevilskimi vrednostmi
        svg.append("g")
            .attr("class", "y-axis")
            .attr("transform", `translate(${marginLeft},0)`)
            .call(d3.axisLeft(y).ticks(5).tickFormat(d3.format("d")))
            .call(g => g.select(".domain").remove())
            .style("font-size", "0.8em");

        // Naslov grafa
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", marginTop / 2)
            .attr("text-anchor", "middle")
            .style("font-size", "1.5em")
            .text("Top 15 znamenitosti glede na število obiskov");

        // Vstavimo svg v element
        d3.select(visitsChartRef.current).node().appendChild(svg.node());
    };

    const drawRatingsChart = () => {
        const data = ratingsByAttraction; 
        
        const width = 928;
        const height = 500;
        const marginTop = 40;
        const marginRight = 0;
        const marginBottom = 120;
        const marginLeft = 60;

        // horizontalna skala (x)
        const x = d3.scaleBand()
            .domain(d3.sort(data, d => -d.avgRatingAll).map(d => d.attractionName))
            .range([marginLeft, width - marginRight])
            .padding(0.1);

        const xAxis = d3.axisBottom(x)
          .tickSizeOuter(0)
          .tickFormat(d => d.length > 20 ? d.slice(0, 20) + '...' : d);

        // vertikalna skala (y)
        const y = d3.scaleLinear()
            .domain([0, d3.max(data, d => d.avgRatingAll)]).nice()
            .range([height - marginBottom, marginTop]);

        // SVG container s viewBox in responsive nastavitvijo
        d3.select(ratingsChartRef.current).selectAll("*").remove();
        const svg = d3.create("svg")
            .attr("viewBox", [0, 0, width, height])
            .attr("width", width)
            .attr("height", height)
            .attr("style", "max-width: 100%; height: auto;")

        // Dodamo skupino za stolpce
        const barsGroup = svg.append("g")
            .attr("class", "bars")
            .attr("fill", "#FFD8BE")
            .style("cursor", "pointer");

        barsGroup.selectAll("rect")
            .data(data)
            .join("rect")
                .attr("x", d => x(d.attractionName))
                .attr("y", d => y(d.avgRatingAll))
                .attr("height", d => y(0) - y(d.avgRatingAll))
                .attr("width", x.bandwidth())
                .on("click", (event, d) => {
                    window.location.href = `/attractions/${d.attractionId}`;
                })
            .on("mouseover", function(event, d) {
                d3.select(this)
                    .attr("fill", "#FFB28D")
                    .attr("transform", `scale(1.05)`)
                    .attr("transform-origin", `${x(d.attractionName) + x.bandwidth()/2} ${y(d.avgRatingAll) + (y(0) - y(d.avgRatingAll))/2 + ( 0.5 * (y(0) - y(d.avgRatingAll)))}`);
            })
            .on("mouseout", function(event, d) {
                d3.select(this)
                    .attr("fill", "#FFD8BE")
                    .attr("transform", "scale(1)");
            });

        // X os
        const xAxisGroup = svg.append("g")
            .attr("class", "x-axis")
            .attr("transform", `translate(0,${height - marginBottom})`)
            .call(xAxis)
            .selectAll("text")
                .attr("transform", "rotate(-45)")
                .style("text-anchor", "end")
                .style("font-size", "1.2em");

        // Y os z celoštevilskimi vrednostmi
        svg.append("g")
            .attr("class", "y-axis")
            .attr("transform", `translate(${marginLeft},0)`)
            .call(d3.axisLeft(y).ticks(10))
            .call(g => g.select(".domain").remove())
            .style("font-size", "0.8em");

        // Naslov grafa
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", marginTop/2)
            .attr("text-anchor", "middle")
            .style("font-size", "1.5em")
            .text("Top 15 znamenitosti glede na povprečno oceno uporabnikov");

        // Vstavimo svg v element
        d3.select(ratingsChartRef.current).node().appendChild(svg.node());
    };  

    return (
        <div className="attraction-statistics">
            <h1>Statistika znamenitosti</h1>
            {(loadingRatings || loadingVisits) ? (
                <p>Nalaganje...</p>
            ) : error ? (
                <p className="error">{error}</p>
            ) : (
                <>
                    <div ref={visitsChartRef} className="visits-chart"></div>
                    <div ref={ratingsChartRef} className="ratings-chart"></div>
                </>
            )}
        </div>
    );
}

export default AttractionStatistics;