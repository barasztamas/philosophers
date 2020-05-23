This is a university assignment for the course Concurrent Programming.
The assignment is in Hungarian:

Oldd meg az étkező filozófusok problémájának az alábbiakban leírt, módosított változatát.

A program indításakor, ha kap parancssori paramétert, az szabja meg a filozófusok számát, nevezzük ezt n-nek. Ha a program nem kap paramétert, akkor n legyen 5.

A feladathoz itt is tartozik egy kerekasztal n székkel és egy-egy villával a székek között az asztalon. Ezenkívül a rendszerhez tartozik egy külön szék is.

1. A filozófusok kezdetben nem az asztalnál ülnek, hanem kívülről érkeznek, és először a külön székre ülnek le. (Minden székre, erre is igaz, hogy legfeljebb egy filozófus ülhet rajta egyszerre.)
1. Amikor a külön székre leül egy filozófus, eldönti, hogy a jobb vagy a bal oldali villáját preferálja-e innentől. Ezután kiül az asztal mellé egy üres székre.
    - A döntésnél a filozófus figyel arra, hogy elkerülje a potenciális holtpontot: nem dönthet úgy, hogy mindenkinek egyforma legyen a preferenciája.
1. Amikor az asztalnál ül a filozófus, az alábbi lehetőségek közül választ véletlenszerűen.
    - Eszik. Ehhez felemeli a preferált villáját, majd a másikat, majd eszik, majd visszateszi a villákat az asztalra. - Egy villa értelemszerűen csak egy filozófusnál lehet egyszerre, egyébként az asztalon van, és felvehető.
    - Kiül a külön székre, innentől lásd előző pont.
    - Átül egy másik üres székre, ha van ilyen az asztalnál.

A program fejeződjön be természetes módon (minden filozófus tevékenysége fejeződjön be "erőszakmentesen", pl. ```Thread.stop``` hívása nélkül), miután mindegyikük pontosan n-szer evett. A főprogram a végrehajtás utolsó lépéseként írja ki, milyen sorrendben történtek meg a filozófusok étkezései (ki mikor evett, a program indításától számított ezredmásodpercekben).

A filozófusok minden döntési lépés meghozatala és minden lépés megtétele előtt várakozzanak véletlenszerűen választott, rövid ideig (pl. 0,5 és 2 másodperc közötti időtartamig).

A program naplózhatja a döntések, lépések részletes adatait egy naplófájlba. Jeles érdemjegyhez ez kötelező feltétel.
