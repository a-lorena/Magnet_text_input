## Magnet text input
Beskontaktni unos teksta na mobilnom uređaju pomoću permanentnog magneta, senzora i random forest klasifikatora.

### Tehnologije i razvojni alati
- Android Studio, Java, Chaquopy
- JupyterNotebook, Python, scikit-learn

### Aplikacija za prikupljanje podataka
Android aplikacija izrađena za potrebe prikupljanja dataseta. Pomoću magnetometra ugrađenog u mobilni uređaj mjeri vrijednosti magnetskog polja te ih pohranjuje u lokalnu bazu uređaja. Podaci se zatim mogu pohraniti u JSON datoteke.

<p align="middle">
  <img src="/Images/DatasetApp.png" width="600" />
</p>

### Kalibracija
Izveden je eksperiment kojim je dokazano da kalibracija magnetnog senzora omogućava dobivanje istih rezultata neovisno o lokaciji na kojoj se pišu slova.

<p align="middle">
  <img src="/Images/Kalibracija.png" width="600" />
</p>

### Redukcija dimenzionalnosti
Prilikom određivanja položaja pisanja slova (iznad ili pored uređaja) korištena je metoda redukcije dimenzionalnosti kako bi se odredilo kod kojeg položaja su očitane vrijednosti za ista slova što sličnije. Od nekoliko isprobanih metoda najbolje rezultate je dala metoda t-SNE za slova pisana iznad uređaja.

<p align="middle">
  <img src="/Images/tSNE.png" width="600" />
</p>

### Klasifikacija
Za klasificiranje napisanog slova koristi se random forest klasifikator kojem se daju 3D podaci - X, Y, Z vrijednosti. Korišten je random forest klasifikator implementiran u scikit-learn knjižnici. U Android IME je klasifikator integriran pomoću Chaquopy SDK za pokretanje Python skripti u Android aplikacijama. Klasifikator se trenira na mobilnom uređaju.

<p align="middle">
  <img src="/Images/RFConfusionMatrix.png" width="600" />
</p>

### Android IME
Android IME za beskontakni unos teksta zasnovan na manipulaciji ambijentalnog magnetskog polja. Permanentnim magnetom se iznad mobilnog uređaja pišu slova i pomiče pokazivač po tipkama. Aktiviranjem senzora blizine potvrđuje se odabir tipke na kojoj se trenutno nalazi pokazivač (početak pisanja, slovo, razmak, brisanje itd.).

<p align="middle">
  <img src="/Images/LetterInput.png" width="600" />
</p>
