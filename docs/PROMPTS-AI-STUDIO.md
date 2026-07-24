# Prompts pour Google AI Studio — Construire Kabro Edu (Android natif)

Ces prompts sont **prêts à coller** dans Google AI Studio (onglet **Build** / création
d'application). Objectif : générer une **application Android native en Java/Kotlin**
avec Jetpack Compose, qui lit le contenu JSON depuis GitHub.

> **Technologie choisie** : Android natif (Kotlin principal + Java), Android Studio,
> Jetpack Compose, architecture MVVM, Room pour le local, Retrofit pour le réseau.

> **Comment procéder**
> 1. Ouvrez Google AI Studio → **Build** (créer une app).
> 2. Collez d'abord le **PROMPT 1 (§0.1)** et générez → squelette frontend.
> 3. Collez le **PROMPT 2 (§0.2)** pour peaufiner le design.
> 4. Quand l'interface vous plaît, collez le **PROMPT 3 (§3)** pour brancher le contenu réel.
> 5. Affinez avec les **prompts d'itération (§2)** pour chaque fonctionnalité.

---

## 0. PROMPTS DE DÉMARRAGE — Frontend « Kabro Edu » (Android natif Java/Kotlin)

Approche recommandée : construire d'abord une **belle interface avec des données de
démonstration**, puis brancher le contenu réel GitHub.

### 0.1 — Démarrage du frontend (à coller en premier)

```
Crée une application Android native nommée « Kabro Edu », en Kotlin (principal) et Java,
avec Jetpack Compose pour l'UI. C'est une app d'aide à la préparation du baccalauréat
tchadien, 100 % en français, pour les séries A4 (littéraire), C et D (scientifiques). Style
d'apprentissage inspiré de Duolingo (parcours par étapes, motivant) et d'Anki (mémorisation
par répétition espacée).

STACK TECHNIQUE :
- Langages : Kotlin (principal) + Java (modules utilitaires si besoin).
- UI : Jetpack Compose + Material 3.
- Architecture : MVVM (ViewModel + StateFlow/LiveData).
- Navigation : Navigation Compose (BottomNavigation avec 5 onglets).
- Stockage local : Room (SQLite) pour la progression, les intervalles de révision et le
  cache du contenu JSON ; DataStore pour les préférences (série choisie, thème, objectif).
- Réseau (à intégrer plus tard) : Retrofit + OkHttp + Gson pour charger le contenu JSON
  depuis GitHub.
- Pas de backend, pas de Firebase, pas de collecte de données utilisateur.

Pour cette première version, construis UNIQUEMENT le FRONTEND avec des DONNÉES DE
DÉMONSTRATION codées en dur (fichier DemoData.kt). L'objectif est une interface belle,
fluide et navigable sur Android.

IDENTITÉ VISUELLE :
- Nom affiché : « Kabro Edu ». Ton chaleureux, encourageant, épuré.
- Palette inspirée du drapeau tchadien :
  · Bleu profond #002664 → couleur primaire (Primary).
  · Jaune #FECB00 → accents, récompenses (Secondary/Tertiary).
  · Rouge #C60C30 → alertes, streak (Error/Custom).
- Fond clair, grandes cartes arrondies (RoundedCornerShape(16.dp)), boutons larges (48dp min),
  espacement généreux, adapté au tactile.
- Typographie Material 3 Typography par défaut, contrastes élevés.

NAVIGATION : BottomNavigation (NavigationBar Compose) avec 5 onglets :
🏠 Accueil · 📚 Matières · 🔁 Réviser · 📈 Progression · ⚙️ Paramètres.

ÉCRANS (avec données factices réalistes en français) :
1. Onboarding (affiché au 1er lancement, vérifié via DataStore « onboardingDone ») :
   - Écran de bienvenue : logo/nom « Kabro Edu » + message « Gratuit · Sans publicité ·
     Sans collecte de données ».
   - Choix de la série : 3 grandes cartes cliquables (A4, C, D) avec le titre de la série.
   - Mémorise le choix dans DataStore.
2. Accueil :
   - En-tête : streak (icône flamme + nombre de jours consécutifs).
   - Anneau de progression de l'objectif du jour (CircularProgressIndicator custom).
   - Bouton « Continuer » (reprend le dernier chapitre).
   - Carte « À réviser aujourd'hui » (ex. « 12 cartes à revoir »).
3. Matières (LazyColumn) :
   - Liste des matières de la série avec nom + barre de progression linéaire.
   - Ex. série D : Maths, Physique, Chimie, SVT, Histoire, Géographie, Français, Anglais.
4. Parcours d'une matière :
   - Chapitres en « chemin » vertical façon Duolingo :
     · Jalons ronds reliés par un trait (dessinés avec Canvas dans Compose).
     · Chapitre courant mis en avant (couleur primaire, taille plus grande).
     · Chapitres verrouillés grisés avec icône cadenas.
     · Pastille de progression (%) par chapitre.
5. Détail d'un chapitre :
   - Liste des étapes : Formulaire/Mémorisation · Quiz · Méthode · Exercices · Défi.
   - Section « Vidéos d'explication » (items avec titre + chaîne ; au clic, ouvre YouTube
     via Intent ACTION_VIEW).
   - Bouton « Démarrer ».
6. Lecteur d'activité (plein écran, sans BottomNav) :
   - LinearProgressIndicator en haut (progression dans l'étape).
   - Une activité à la fois.
   - Exemples de démo : QCM (choix unique avec options), flashcard (recto/verso + 4 boutons
     d'auto-évaluation : À revoir / Difficile / Correct / Facile).
   - Feedback immédiat (correct/incorrect + explication).
   - Écran de résultats en fin d'étape (score %, XP gagné, animation simple).
7. Réviser :
   - « À réviser aujourd'hui » (compte de cartes dues).
   - Révision par matière.
8. Progression :
   - Streak, XP total, cartes maîtrisées.
   - Barres de progression par matière.
9. Paramètres :
   - Changer de série.
   - Apparence : thème clair/sombre (toggle + isSystemInDarkTheme), taille de police.
   - Objectif quotidien (nombre d'activités).
   - Page « À propos » : « Kabro Edu est 100 % gratuit, sans publicité et ne collecte
     aucune donnée personnelle. »

STRUCTURE DU PROJET (packages) :
- com.kabroedu.app.ui.screens (un fichier par écran)
- com.kabroedu.app.ui.components (composables réutilisables : ActivityCard, ProgressRing,
  ChapterNode, FlashCard, QuizOption…)
- com.kabroedu.app.ui.theme (couleurs, typographie, thème Material 3)
- com.kabroedu.app.data.model (data classes Kotlin pour Manifest, Matiere, Unite, Etape,
  Activite, VideoRef…)
- com.kabroedu.app.data.repository (repository pattern)
- com.kabroedu.app.data.local (Room entities + DAO + Database)
- com.kabroedu.app.data.demo (DemoData.kt avec données factices)
- com.kabroedu.app.viewmodel (un ViewModel par feature)

QUALITÉ :
- Architecture MVVM propre, injection de dépendances manuelle ou Hilt.
- Composables réutilisables avec @Preview.
- Code lisible, commenté en français.
- AUCUNE publicité, AUCUN formulaire de collecte de données, AUCUN SDK de tracking.
```

### 0.2 — Peaufinage du design

```
Améliore le design de Kabro Edu pour le rendre plus engageant, façon Duolingo,
en utilisant les capacités d'animation de Jetpack Compose :
- Animations légères (animateColorAsState, AnimatedVisibility, animateFloatAsState) sur
  les bonnes/mauvaises réponses et sur les gains d'XP.
- Un emoji mascotte sympathique sur l'Accueil qui encourage l'élève (ex. 📚 ou 🎓).
- Le « chemin » des chapitres dessiné avec Canvas/drawCircle + drawLine :
  · Jalons ronds reliés par un trait.
  · Étape en cours animée avec un effet pulse (InfiniteTransition, scale).
  · Cadenas sur les verrouillés.
- Cartes de matière avec couleur et emoji distinct par matière (ex. 🧮 Maths bleu,
  ⚗️ Chimie violet, 🧬 SVT vert, ⚡ Physique orange…).
- LinearProgressIndicator fluide (animateFloatAsState) dans le lecteur d'activité.
- Écran de résultats festif : effet confettis discret (particules animées avec
  rememberInfiniteTransition ou LaunchedEffect + Canvas).
- Conserve la palette (#002664, #FECB00, #C60C30), Material 3, rendu épuré et lisible.
```

---

## 1. PROMPT MAÎTRE COMPLET (alternatif — tout-en-un)

Si vous préférez **un seul prompt** qui génère le squelette complet avec la logique métier :

```
Crée une application Android native nommée « Kabro Edu », en Kotlin et Java, avec Jetpack
Compose + Material 3. App de préparation au baccalauréat tchadien (séries A4, C, D),
en français, inspirée de Duolingo + Anki.

CONTRAINTES ABSOLUES :
- 100 % gratuite, AUCUNE publicité, AUCUNE collecte de données, AUCUN compte utilisateur.
- Progression stockée LOCALEMENT (Room/SQLite + DataStore).
- Contenu JSON chargé depuis GitHub et mis en cache local (fonctionne hors-ligne après).
- App légère (cibler des téléphones modestes et connexions lentes).

STACK : Kotlin + Java, Jetpack Compose, Material 3, MVVM, Navigation Compose,
Room, DataStore, Retrofit + OkHttp + Gson, WorkManager (mises à jour en arrière-plan).

SOURCE DU CONTENU :
- baseUrl = "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/"
- Point d'entrée : manifest.json (contentVersion, matieresPartagees, series[]).
- Une matière est soit { "ref": "<clef>" } (va dans matieresPartagees), soit propre avec
  { id, titre, type, version, chemin, fichiers[] }.
- Chaque matière a un "<chemin>/matiere.json" listant unites[] : { id, titre, ordre,
  fichier, prerequis[] }.
- Chaque unité est un JSON : { id, matiere, type, titre, ordre, source, reglesExercice,
  formulaire?, etapes[], banqueExercices?, videos? }.

NAVIGATION : NavigationBar (5 onglets) — Accueil · Matières · Réviser · Progression ·
Paramètres.

FONCTIONNALITÉS :
- Onboarding (1er lancement) : bienvenue + choix de la série + téléchargement du contenu.
- Accueil : streak, objectif du jour, « Continuer », « À réviser aujourd'hui ».
- Matières : liste avec progression ; parcours « chemin » Duolingo vertical avec prérequis.
- Détail d'unité : étapes, vidéos (Intent YouTube), formulaire de formules.
- Lecteur d'activité plein écran : une activité à la fois, feedback, correction, résultats.
- Répétition espacée SM-2 : cartes (formule, rappel_actif, citation) avec auto-évaluation
  (À revoir / Difficile / Correct / Facile), intervalles stockés en Room.
- Hors-ligne : cache Room des JSON, WorkManager vérifie contentVersion/version et ne
  retélécharge que le nécessaire.
- Progression : streak, XP, cartes maîtrisées, barres par matière.
- Paramètres : série, thème clair/sombre, taille de police, objectif, « À propos ».

TYPES D'ACTIVITÉS À GÉRER (chaque étape a « activites[] ») :
- "choix_unique" : { question, options[], reponse (index), explication }.
- "choix_multiple" : { question, options[], reponses[], explication }.
- "association" : { consigne, paires[{gauche,droite}], explication }.
- "classement" : { consigne, elements[], ordreCorrect[], explication }.
- "texte_a_completer" : { phrase ("___"), reponse, toleranceMots, explication }.
- "rappel_actif" : { question, reponseAttendue, modeReponse, explication } → flashcard.
- "reponse_courte" : { question, reponsesAcceptees[], explication }.
- "citation" : { mode, citation, options[], reponse, oeuvre, explication }.
- "formule" : { recto, verso, modeReponse:"auto_evaluation" } → flashcard formule.
- "resolution_ordonnee" : { enonce, etapes[], ordreCorrect[], explication }.
- banqueExercices[] : { id, enonce, difficulte, etapesCorrection[], reponseFinale,
  competenceId } → exercices corrigés.

RÈGLE DE CORRECTION : en cas d'échec, afficher D'ABORD reponseFinale puis dérouler
etapesCorrection pas à pas. Cf. reglesExercice de chaque unité.

IDENTITÉ : palette bleu #002664, jaune #FECB00, rouge #C60C30 ; Material 3 ; coins
arrondis ; boutons larges ; lisibilité sur écrans modestes.
```

---

## 2. Prompts d'itération (à envoyer un par un)

### 2.1 Écran d'accueil & onboarding
```
Améliore l'onboarding Android : un écran de bienvenue « Kabro Edu » expliquant que l'app
est gratuite, sans publicité et sans collecte de données, puis un choix de série (A4, C, D)
avec de grandes cartes Material 3, puis un écran de téléchargement du contenu avec
CircularProgressIndicator. Mémorise la série dans DataStore. Sur l'Accueil, affiche :
le streak (flamme + jours), un objectif quotidien (CircularProgressIndicator custom),
un bouton « Continuer » (dernier chapitre), et une Card « À réviser aujourd'hui ».
```

### 2.2 Parcours de matière façon Duolingo
```
Transforme la liste des unités en un « chemin » vertical façon Duolingo dans Compose :
un LazyColumn avec un composable ChapterNode qui dessine un jalon rond (Canvas drawCircle)
relié au suivant par un trait. L'unité courante est mise en avant (scale + couleur primaire),
les unités verrouillées sont grisées avec une icône cadenas (Icons.Default.Lock),
et chaque jalon affiche une pastille de progression. Au clic, naviguer vers le détail.
```

### 2.3 Lecteur d'activités + règle de correction
```
Implémente un lecteur d'activités plein écran (sans BottomNav) qui enchaîne les activités
d'une étape une par une. LinearProgressIndicator en haut. Pour chaque type, un composable
dédié. Feedback immédiat (couleur + explication). RÈGLE : pour les exercices corrigés
(banqueExercices), l'élève tente d'abord ; en cas d'échec, afficher reponseFinale en grand
puis dérouler etapesCorrection étape par étape (expand/collapse). À la fin, écran de
résultats (score %, XP gagné, seuilReussite atteint ou non, bouton « Continuer »).
```

### 2.4 Répétition espacée (Anki)
```
Ajoute un moteur de répétition espacée (algorithme SM-2 simplifié) avec Room. Chaque carte
(formule, rappel_actif, citation, définitions) a une entité Room CardState { id, ease,
interval, nextReviewDate }. Après révélation, 4 boutons (À revoir=1 / Difficile=2 /
Correct=3 / Facile=4) mettent à jour ease et interval. L'onglet Réviser propose :
« À réviser aujourd'hui » (toutes matières), et la révision par matière. Affiche le nombre
de cartes dues sur l'Accueil.
```

### 2.5 Vidéos d'explication
```
Dans le détail d'une unité, si le champ "videos" existe et n'est pas vide, affiche une
section « Vidéos d'explication » : pour chaque VideoRef, un item Material 3 avec le titre,
la chaîne (auteur), et une icône (vidéo ou playlist). Au clic, ouvre l'URL via Intent
(ACTION_VIEW, Uri.parse(url)). Gère proprement l'absence de vidéos (section masquée).
```

### 2.6 Hors-ligne & cache
```
Rends Kabro Edu fonctionnelle hors-ligne. À la première ouverture (ou quand en ligne),
télécharge le manifest et les JSON (Retrofit), stocke-les dans Room. Aux lancements suivants,
si hors-ligne, charge tout depuis Room. Si en ligne, compare contentVersion et les versions
de matières : ne retélécharge que ce qui a changé. Utilise WorkManager pour une vérification
périodique en arrière-plan (1x/jour). Affiche un indicateur « Contenu à jour ✓ » ou
« Mise à jour disponible » dans les Paramètres.
```

### 2.7 Progression & paramètres
```
Onglet Progression : streak (avec réinitialisation si un jour est manqué), XP total,
nombre de cartes maîtrisées, progression par matière (LinearProgressIndicator).
Onglet Paramètres : changer de série (relance le chargement), gérer le contenu (vérifier /
retélécharger), objectif quotidien (slider 5-30 activités), thème clair/sombre (toggle),
taille de police (slider), page « À propos » (texte gratuit/sans pub/sans collecte + crédits).
```

---

## 3. Prompt d'intégration des données réelles (GitHub)

```
Branche Kabro Edu sur le contenu réel. Écris un module de données Kotlin (package
com.kabroedu.app.data.remote) qui :
1. Définit un service Retrofit ContentApi avec :
   - GET("manifest.json") → ManifestDto
   - GET("{path}") → corps brut (String) pour les fichiers JSON individuels.
2. Définit les data classes : ManifestDto, MatiereDto, UniteDto, EtapeDto,
   ActiviteDto (sealed class/union sur le champ "type"), VideoRefDto.
3. Implémente un ContentRepository qui :
   - Charge le manifest depuis baseUrl = "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/"
   - Pour la série choisie, résout chaque matière (si "ref", va dans matieresPartagees).
   - Charge chaque matiere.json puis chaque unité via le champ "fichier" (préfixé par baseUrl).
   - Stocke tout dans Room (entités ContentCache : path, json, version).
   - Au prochain lancement, ne retélécharge que si contentVersion ou version a augmenté.
   - Si hors-ligne, retourne le cache Room (fallback gracieux).
4. Gère les erreurs réseau (réessai exponentiel avec Retrofit interceptor + fallback cache).
5. Expose les données au ViewModel via Flow<List<Matiere>>, Flow<Unite>, etc.
```

---

## 4. Conseils d'utilisation d'AI Studio

- Générez d'abord le squelette avec le **PROMPT 0.1**, testez la navigation dans
  l'émulateur, puis ajoutez les fonctionnalités **une à une** (§2) pour garder un
  projet stable.
- Après chaque itération, demandez : « corrige les erreurs de compilation et vérifie
  que l'app se lance sur l'émulateur Android ».
- Si une génération casse quelque chose, revenez à l'étape précédente et reformulez
  (une seule fonctionnalité à la fois).
- Pour le contenu réel, n'utilisez le **prompt §3** qu'une fois l'interface stable.
- Publiez l'APK sur le Play Store (gratuit pour les développeurs individuels) ou en APK
  direct (sideload) — cohérent avec l'objectif « gratuit, sans serveur ».
