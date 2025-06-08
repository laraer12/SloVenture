import { useEffect, useRef, useState } from "react";
import * as d3 from "d3";

function D3Calendar({ visits }) {
  const svgRef = useRef();
  const [year, setYear] = useState(new Date().getFullYear());

  const years = Array.from(
    new Set(visits.map((v) => new Date(v.visitDate).getFullYear()))
  ).sort();

  // dnevi obiskov
  const visitsByDate = visits.reduce((acc, visit) => {
    const day = new Date(visit.visitDate).toISOString().slice(0, 10); // "YYYY-MM-DD"

    if (!acc[day])
      acc[day] = [];

    acc[day].push(visit); // dodam obisk na ta datum

    return acc;
  }, {});

  useEffect(() => {
    if (!visits || visits.length === 0)
      return;

    // dimenzije in razmiki
    const height = 170;
    const cellSize = 20;
    const monthGap = 20;
    const marginX = 40;
    const monthLabel = 43;

    // pred vsakim izrisom počistim prejšnji prikaz koledarja
    d3.select(svgRef.current).selectAll("*").remove();

    const format = d3.timeFormat("%Y-%m-%d");
    const timeWeek = d3.timeFormat("%U");
    const dayOfWeek = (d) => d.getDay();

    const days = d3.timeDays(new Date(year, 0, 1), new Date(year + 1, 0, 1));
    const maxVisits = d3.max(Object.values(visitsByDate), (v) => v.length) || 1;

    const colorScale = d3.scaleLinear()
      .domain([0, maxVisits])
      .range(["#cce5ff", "#004085"]);

    const tooltip = d3
      .select("body")
      .append("div")
      .attr("class", "calendar-tooltip")
      .style("position", "absolute")
      .style("padding", "8px")
      .style("background", "rgba(0,0,0,0.7)")
      .style("color", "white")
      .style("border-radius", "4px")
      .style("pointer-events", "none")
      .style("opacity", 0);

    const monthNames = [ "Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Avg", "Sep", "Okt", "Nov", "Dec" ];
    const monthDates = d3.timeMonths(new Date(year, 0, 1), new Date(year + 1, 0, 1));

    // izračun x pozicij mesecev
    const monthXPositions = monthDates.map((d, i) => {
      const startWeek = + timeWeek(d);
      return startWeek * cellSize + i * monthGap;
    });

    // izračun širine SVG glede na število tednov in razmik med meseci
    const totalWeeks = 53;
    const svgWidth = totalWeeks * cellSize + (monthDates.length - 1) * monthGap + marginX + 40;

    const svg = d3.select(svgRef.current)
      .attr("width", "100%")
      .attr("height", height)
      .attr("viewBox", `0 0 ${svgWidth} ${height}`)
      .attr("font-family", "sans-serif")
      .attr("font-size", 12);
    
    // prikaz dnevov kot kvadratki
    const dayRects = svg.append("g").attr("transform", `translate(${marginX}, 30)`);

    dayRects
      .selectAll("rect")
      .data(days)
      .join("rect")
      .attr("width", cellSize)
      .attr("height", cellSize)
      .attr("x", (d) => {
        const monthIndex = d.getMonth();
        const startWeek = +timeWeek(d);
        return startWeek * cellSize + monthIndex * monthGap;
      })
      .attr("y", (d) => dayOfWeek(d) * cellSize)
      .attr("fill", (d) => {
        const key = format(d);
        const visitsForDay = visitsByDate[key];

        if (!visitsForDay)
          return "#eee";

        return colorScale(visitsForDay.length);
      })
      .attr("stroke", "#ccc")
      .style("cursor", "pointer")
      .on("mouseover", (event, d) => {
        const key = format(d);
        const visitsForDay = visitsByDate[key];

        if (!visitsForDay) {
          tooltip.style("opacity", 0);
          return;
        }
        const htmlContent = `<strong>${key}</strong><br/>Obiski: <ul style="margin:0; padding-left:18px;">` +
          visitsForDay
            .map((v) => `<li>${v.attractionId?.name || "Neznana znamenitost"}</li>`)
            .join("") +
          "</ul>";

        tooltip
          .html(htmlContent)
          .style("opacity", 1)
          .style("left", event.pageX + 10 + "px")
          .style("top", event.pageY + 10 + "px");
      })
      .on("mousemove", (event) => {
        tooltip.style("left", event.pageX + 10 + "px").style("top", event.pageY + 10 + "px");
      })
      .on("mouseout", () => {
        tooltip.style("opacity", 0);
      });

    // prikaz mesecev
    svg.append("g")
      .attr("transform", `translate(${marginX}, 15)`)
      .selectAll("text")
      .data(monthDates)
      .join("text")
      .attr("x", (d, i) => monthXPositions[i] + monthLabel)
      .attr("y", 0)
      .text((d, i) => monthNames[i])
      .attr("font-weight", "bold");

    const daysOrdered = ["N", "P", "T", "S", "Č", "P", "S"];

    svg
      .append("g")
      .selectAll("text")
      .data([1, 2, 3, 4, 5, 6, 0])
      .join("text")
      .attr("y", (d) => ((d === 0 ? 6 : d - 1) * cellSize) + 43)
      .attr("x", 10)
      .text((d, i) => daysOrdered[i])
      .attr("font-weight", "bold")
      .attr("text-anchor", "middle");

    return () => {
      tooltip.remove();
    };
  }, [visits, year, visitsByDate]);

  return (
    <div>
      <div style={{ marginBottom: 10, display: "flex", alignItems: "center", gap: "10px" }}>
        <button
          className="btn btn-primary"
          onClick={() => setYear((y) => (years.includes(y - 1) ? y - 1 : y))}
          disabled={!years.includes(year - 1)}
        >
          Prejšnje leto
        </button>
        <select value={year} onChange={(e) => setYear(Number(e.target.value))} className="form-select" style={{ width: "120px" }}>
          {years.map((y) => (
            <option key={y} value={y}>
              {y}
            </option>
          ))}
        </select>
        <button
          className="btn btn-primary"
          onClick={() => setYear((y) => (years.includes(year + 1) ? year + 1 : y))}
          disabled={!years.includes(year + 1)}
        >
          Naslednje leto
        </button>
      </div>

      <svg ref={svgRef}></svg>

      <style>{`
        .calendar-tooltip {
          font-size: 12px;
          max-width: 250px;
          pointer-events: none;
          z-index: 10;
        }
      `}</style>
    </div>
  );
}

export default D3Calendar;