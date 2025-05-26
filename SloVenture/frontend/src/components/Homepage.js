import { useEffect } from 'react';

export default function Homepage() {
  useEffect(() => {
    document.title = "Domača stran"; // naslov zavihka
  }, []);

  return (
    <>
      <h1>Dobrodošli na spletni strani najlepših slovenskih znamenitosti!</h1>
      <br />
      <h2>Kar pogumno kliknite na želeno destinacijo v navigacijski vrstici :-)</h2>
    </>
  );
}