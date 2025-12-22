# Projekt - RRI: Delovni načrt
### Projekt:
**Ime:** SloVenture
**Člani:** Lara Erzar, Nika Vuk, Katja Korc
### Cilj:
V okviru projekta bomo razvili interaktivni 3D prikaz Slovenije, ki bo na zemljevidu prikazoval turistične znamenitosti ter animirano “gnečo” obiskovalcev. Aplikacija bo zgrajena v LibGDX, podatke o znamenitostih pa bomo pridobivali prek našega API-ja iz predmeta Spletno programiranje.

## 1. Priprava podatkov in 3D zemljevida
Najprej bomo pridobili podatke o nadmorski višini za celotno Slovenijo. Uporabili bomo Copernicus GLO-30 preko OpenTopography ter prenesli GeoTIFF datoteko z višinskimi podatki. Te podatke bomo pretvorili v heightmap (8- ali 16-bit PNG), primeren za nadaljnjo uporabo v LibGDX.
Na osnovi heightmapa bomo v LibGDX generirali 3D mrežo terena, prilagodili razmerja, višinsko skalo in dodali osnovno osvetlitev. Za vizualni prikaz bomo na površino terena naložili rastrske map tiles iz Mapboxa ali Geoapify ter poskrbeli, da se pravilno teksturirajo na celoten model.

## 2. Pridobivanje podatkov iz našega API-ja
Vzpostavili bomo povezavo z API-jem iz predmeta Spletno programiranje. Prek HTTP GET zahtevkov bomo pridobivali podatke o znamenitostih, kot so koordinate, kategorija, opis in drugi atributi. Poleg tega bomo prejeli tudi informacijo o številu obiskovalcev oziroma gneči ter morebitne dodatne parametre, ki bodo relevantni za prikaz. Pridobljene podatke bomo preoblikovali in pripravili v format, primeren za prikaz na 3D terenu.

## 3. Vizualizacija znamenitosti in animiranje gneče
Lokacije znamenitosti bomo označili z majhnimi 3D markerji oziroma enostavnimi modeli. Dinamični del vizualizacije bodo animirane figure (majhni “ljudje”), ki bodo predstavljale gostoto obiskovalcev oziroma gnečo. Število prikazanih figur bo simbolično in manjše od dejanskega števila obiskovalcev, da ohranimo učinkovitost in berljivost.

## 4. Kontrole kamere in interakcija uporabnika
Aplikacija bo omogočala osnovne interakcije, kot so povečava (zoom), rotacija pogleda in premik po zemljevidu. Uporabnik bo lahko prosto raziskoval 3D teren, se približeval posameznim znamenitostim in rotiral kamero okoli Slovenije. Poleg tega pa bomo omogočili tudi filtriranje znamenitosti (npr. po tipu, gneči ali priljubljenosti) in klik na znamenitost, ki bo odprl informacijsko okno z dodatnimi podrobnostmi.

 ## 5. Dodatne funkcionalnosti 
Kot dodatno funkcionalnost bomo implementirali napredni 3D prikaz zemljevida in objektov. Če se bo izkazalo kot potrebno, bomo dodali tudi predpomnjenje map tiles, da optimiziramo hitrost nalaganja in izrisa zemljevida.

## 6. Testiranje in zaključek
Zaključna faza bo zajemala testiranje pravilnega prikaza koordinat, delovanja interakcij, API komunikacije in hitrosti izrisa pri različnih nastavitvah. Po uspešnem preizkušanju bomo pripravili dokumentacijo, poročilo in predstavitev aplikacije.

