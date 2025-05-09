# SloVenture načrt pri predmetu Sistemska administracija
## 1. Osnovni podatki
Ime projekta: **SloVenture**
Člani projekta: Lara Erzar, Katja Korc, Nika Vuk
Mentor: Uroš Mlakar

## 2. Opis in cilj projekta
SloVenture je študentski projekt, ki ga razvijamo tri študentke z namenom ustvariti sodoben, interaktiven in informativen spletni portal, namenjen raziskovanju kulturnih in naravnih znamenitosti po celotni Sloveniji. Naš cilj je oblikovati digitalni dvojček Slovenije, ki bo obiskovalcem omogočil poglobljeno spoznavanje posameznih regij, znamenitosti in destinacij, bodisi za sprotno načrtovanje izletov, bodisi za navdih in ideje za prihodnje obiske.

Naš cilj je ustvariti digitalno platformo, ki bo domačim in morda tudi tujim obiskovalcem ponudila centraliziran in zanesljiv vir informacij o lepotah Slovenije. SloVenture bo uporabniku prijazen, estetsko dodelan in informativno bogat portal, ki bo združeval tako funkcionalnost digitalnega vodiča kot tudi družbeno komponento aktivne skupnosti. S tem želimo prispevati k promociji slovenskega turizma ter spodbuditi ljudi, da raziskujejo naravne in kulturne zaklade naše dežele.

## 3. Orodja in jeziki za razvoj
Za razvoj našega projekta predvidevamo uporabo naslednjih orodij:

### 3.1. Podatkovna baza (MongoDB)
Za shranjevanje podatkov o znamenitostih, vremenu, uporabnikih, komentarjih in infrastrukturnih informacijah uporabljamo nerelacijsko podatkovno bazo **MongoDB**. Zaradi njene fleksibilnosti in podpore za dokumentno orientirano strukturo je idealna za projekt, kjer se posamezni zapisi med seboj razlikujejo po vsebini in obsegu – npr. naravna znamenitost z višinami in potmi, ali muzej z odpiralnimi časi in vstopnino.

### 3.2. Čelni del
-	**Jetpack Compose** in **Kotlin**
Za razvoj grafičnega vmesnika, s katerim bomo potem lažje upravljali podatkovno bazo, bomo uporabili programski jezik Kotlin v kombinaciji z Jetpack Compose.

-	**React**
Za spletno aplikacijo, namenjeno končnim uporabnikom, smo izbrali React. Omogoča interaktivno prikazovanje zemljevida Slovenije, filtriranje in iskanje znamenitosti, prikaz vremenskih podatkov ter vključevanje uporabniških vsebin. Prav tako omogoča integracijo zunanjih API-jev i gradnjo dinamičnih komponent, kot so ocene, komentarji, zasedenost lokacij ipd.

### 3.3. Zaledni Del
**Node.js** + **Express**
Na strežniški strani bomo uporabili Node.js skupaj z ogrodjem Express, ki skrbi za:
-	povezavo s podatkovno bazo MongoDB,
-	upravljanje z REST API-ji za komunikacijo z uporabniškim vmesnikom,
-	preverjanje uporabniške prijave in avtorizacije,
-	pridobivanje realnočasovnih podatkov, kot je vreme,
-	integracijo z domensko specifičnim jezikom (DSL) za opisovanje infrastrukturnih podatkov
### 3.4. Domensko specifični jezik
Za strukturirano opisovanje mestne infrastrukture bomo razvile lasten domensko specifični jezik. **DSL** bo omogočal enostaven opis lokacij v obliki tekstovnih zapisov (npr. "Lokacija: Blejski grad; tip: kulturna; primernost: družine, otroci..."), ki se nato samodejno pretvorijo v podatke za našo bazo. 
### 3.5. Orodja za razvoj in spremljanje projekta
-	**Jira**
Za načrtovanje, razdeljevanje nalog in spremljanje napredka bomo uporabljali Jiro, ki omogoča jasno strukturo projekta, sledenje sprintom in sprotno komunikacijo znotraj ekipe.

-	**GitHub**
Razvojno okolje bomo vodili preko GitHub-a, kjer bomo hranili izvorno kodo, verzije in sodelovali pri razvoju z uporabo vej, pull request-ov in review-jev.

-	**Docker** in **Azure**
Za zagotavljanje enostavne postavitve okolja bomo uporabili Docker, s katerim bomo vse komponente projekta (čelni del, zaledni del, baza, parser za DSL) zapakirali v ločene kontejnerje znotraj ene aplikacije.
