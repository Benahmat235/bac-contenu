# Prompts pour Google AI Studio — Construire l'application

Ces prompts sont **prêts à coller** dans Google AI Studio (onglet **Build** / création
d'application). Objectif : générer une **web app mobile-first (PWA installable,
hors-ligne)** en React + TypeScript, qui lit le contenu JSON depuis GitHub.

> **Comment procéder**
> 1. Ouvrez Google AI Studio → **Build** (créer une app).
> 2. Collez d'abord le **PROMPT MAÎTRE** (§1) et générez.
> 3. Affinez ensuite fonctionnalité par fonctionnalité avec les **prompts d'itération** (§2).
> 4. Utilisez le **prompt d'intégration des données** (§3) pour brancher le contenu réel.
>
> Remarque : AI Studio produit une **app web**. Pour un rendu « mobile », on vise une
> **PWA installable et hors-ligne**. La même logique/contenu pourra être réutilisée
> plus tard pour une app native (ex. Flutter/React Native).

---

## 0. PROMPTS DE DÉMARRAGE — Frontend « Kabro Edu » (recommandé)

Approche recommandée : construire d'abord une **belle interface avec des données de
démonstration**, puis brancher le contenu réel. Collez ces prompts dans l'ordre.

### 0.1 — Démarrage du frontend (à coller en premier)
```
Crée une application web mobile-first (PWA installable) nommée « Kabro Edu », en React +
TypeScript + Tailwind CSS. C'est une app d'aide à la préparation du baccalauréat tchadien,
100 % en français, pour les séries A4 (littéraire), C et D (scientifiques). Style
d'apprentissage inspiré de Duolingo (parcours par étapes, motivant) et d'Anki (mémorisation).

Pour cette première version, construis UNIQUEMENT le FRONTEND avec des DONNÉES DE
DÉMONSTRATION en local (pas encore d'appel réseau). L'objectif est une interface belle,
fluide et navigable.

IDENTITÉ VISUELLE :
- Nom affiché : « Kabro Edu ». Ton chaleureux, encourageant, épuré.
- Palette inspirée du drapeau tchadien, utilisée avec sobriété : bleu profond #002664
  (principale), jaune #FECB00 (accents/récompenses), rouge #C60C30 (alertes/streak).
  Fond clair, grandes cartes arrondies, coins doux, boutons larges (tactiles).
- Typographie lisible, contrastes élevés, icônes simples.

NAVIGATION : barre d'onglets en bas — 🏠 Accueil · 📚 Matières · 🔁 Réviser ·
📈 Progression · ⚙️ Paramètres.

ÉCRANS (avec données factices réalistes en français) :
1. Onboarding : bienvenue « Kabro Edu » + message « Gratuit · Sans publicité · Sans
   collecte de données », puis choix de la série (3 grandes cartes : A4, C, D).
2. Accueil : streak (flamme + jours), anneau de progression de l'objectif du jour,
   bouton « Continuer », carte « À réviser aujourd'hui ».
3. Matières : liste des matières de la série avec barre de progression (ex. série D :
   Maths, Physique, Chimie, SVT, Histoire, Géographie, Français, Anglais).
4. Parcours d'une matière : chapitres en « chemin » vertical façon Duolingo (jalons
   reliés), chapitre courant mis en avant, verrouillés grisés, pastille de progression.
5. Détail d'un chapitre : étapes (Formulaire/Mémorisation · Quiz · Méthode · Exercices ·
   Défi), section « 🎥 Vidéos d'explication » (items titre + chaîne), bouton « Démarrer ».
6. Lecteur d'activité plein écran : barre de progression, une activité à la fois, exemple
   de QCM et exemple de flashcard (recto/verso + auto-évaluation À revoir/Difficile/
   Correct/Facile), feedback immédiat + explication, puis écran de résultats (score %, XP).
7. Réviser : « À réviser aujourd'hui » + révision par matière.
8. Progression : streak, XP total, cartes maîtrisées, progression par matière.
9. Paramètres : changer de série, apparence (thème clair/sombre, taille de police),
   objectif quotidien, page « À propos » (gratuit, sans pub, sans collecte de données).

QUALITÉ : TypeScript propre et modulaire (un composant par écran et par type d'activité),
routing entre écrans, états vides gérés, responsive mobile d'abord. Mémorise la série et le
thème en localStorage. AUCUNE publicité, AUCUN formulaire de collecte de données.
```

### 0.2 — Peaufinage du design
```
Améliore le design de Kabro Edu façon Duolingo : animations légères sur les réponses et les
gains d'XP ; une mascotte/emoji sympathique qui encourage sur l'Accueil ; le « chemin » des
chapitres avec jalons ronds reliés, étape en cours animée et cadenas sur les verrouillés ;
cartes de matière avec couleur/emoji distinct ; barre de progression fluide et écran de
résultats festif (confettis discrets). Conserve la palette (#002664, #FECB00, #C60C30) et un
rendu épuré et lisible.
```

Ensuite, quand l'interface vous convient, branchez le contenu réel avec le
**prompt d'intégration des données (§3)**.

---

## 1. PROMPT MAÎTRE (génération initiale)

```
Crée une application web mobile-first (PWA installable et fonctionnelle hors-ligne)
nommée « Kabro Edu », en React + TypeScript + Tailwind CSS. C'est une app d'aide à la
préparation du baccalauréat tchadien, en français, pour les séries A4 (littéraire),
C et D (scientifiques). Style d'apprentissage : progression par étapes façon Duolingo
+ mémorisation par répétition espacée façon Anki.

CONTRAINTES ABSOLUES :
- 100 % gratuite, AUCUNE publicité, AUCUNE collecte de données, AUCUN compte utilisateur.
- Toute la progression est stockée LOCALEMENT (localStorage/IndexedDB).
- Contenu chargé depuis GitHub (JSON en lecture seule) et mis en cache pour l'hors-ligne.
- App légère et rapide (cibler des téléphones modestes et des connexions lentes).

SOURCE DU CONTENU (à lire au démarrage) :
- baseUrl = "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/"
- Point d'entrée = baseUrl + "manifest.json"
- manifest.json contient : contentVersion (nombre), matieresPartagees (objet),
  et series[] (chaque série a id "a4"|"c"|"d", titre, et matieres[]).
- Une matière est soit { "ref": "<clef de matieresPartagees>" }, soit une matière propre
  avec { id, titre, type, version, chemin, fichiers[] }.
- Chaque matière a un fichier "<chemin>/matiere.json" listant unites[] :
  { id, titre, ordre, fichier, prerequis[] }.
- Chaque unité "<fichier>" est un JSON : { id, matiere, type, titre, ordre, source,
  reglesExercice, formulaire?, etapes[], banqueExercices?, videos? }.

NAVIGATION (barre d'onglets en bas) : Accueil · Matières · Réviser · Progression · Paramètres.
- Onboarding au 1er lancement : bienvenue + choix de la série + téléchargement du contenu.
- Accueil : objectif du jour, streak (jours consécutifs), bouton « Continuer » (dernier
  chapitre), bloc « À réviser aujourd'hui » (répétition espacée).
- Matières : liste des matières de la série choisie, avec progression ; en ouvrant une
  matière, afficher ses unités ordonnées sous forme d'un « chemin » (style Duolingo),
  avec verrouillage tant que les prérequis ne sont pas validés.
- Unité : liste des étapes ; si l'unité a un champ "videos", afficher une section
  « Vidéos d'explication » (liens ouvrant YouTube dans un nouvel onglet, avec titre,
  chaîne/auteur) ; si l'unité a un "formulaire", proposer un deck de formules à réviser.
- Lecteur d'étape : présenter les activités UNE PAR UNE, plein écran, avec validation
  et feedback immédiat, puis un écran de résultats (score, XP gagné).

RÈGLE DE CORRECTION (importante) : quand l'élève échoue à un exercice, l'app doit
D'ABORD révéler la « réponse finale » (reponseFinale) puis afficher la correction pas à
pas (etapesCorrection). Cf. le champ reglesExercice de chaque unité.

TYPES D'ACTIVITÉS À GÉRER (chaque étape a "categorie" et "activites[]") :
- "choix_unique" : { question, options[], reponse (index), explication } → QCM 1 bonne réponse.
- "choix_multiple" : { question, options[], reponses[] (indices), explication }.
- "association" : { consigne, paires[{gauche,droite}], explication } → relier les paires.
- "classement" : { consigne, elements[], ordreCorrect[], explication } → remettre dans l'ordre.
- "texte_a_completer" : { phrase (avec "___"), reponse, toleranceMots, explication }.
- "rappel_actif" : { question, reponseAttendue, modeReponse, explication } → flashcard :
  révéler la réponse puis auto-évaluation (À revoir / Difficile / Correct / Facile).
- "reponse_courte" : { question, reponsesAcceptees[], explication }.
- "citation" : { mode:"attribuer", citation, options[], reponse (index), oeuvre, explication }.
- "formule" : { recto, verso, modeReponse:"auto_evaluation", explication } → flashcard.
- "exercice_corrige" (et objets de banqueExercices) : { enonce, difficulte,
  etapesCorrection[], reponseFinale } → l'élève cherche, puis on révèle la réponse et la correction.
- "resolution_ordonnee" : { enonce, etapes[], ordreCorrect[], explication } → remettre dans l'ordre.

RÉPÉTITION ESPACÉE : toute carte de mémorisation vue (formule, rappel_actif, citation,
et les définitions) entre dans une file de révision gérée par un algorithme type SM-2 /
Leitner. L'auto-évaluation met à jour l'intervalle et la prochaine échéance. L'onglet
« Réviser » et le bloc « À réviser aujourd'hui » regroupent les cartes dues.

GAMIFICATION LÉGÈRE : XP par activité réussie, streak quotidien, objectif quotidien
paramétrable, déblocage progressif des unités.

HORS-LIGNE & MISES À JOUR : mettre en cache le manifest et les JSON téléchargés ;
au lancement, si en ligne, comparer contentVersion (global) et version (par matière)
au cache et ne retélécharger que ce qui a changé. Fonctionner entièrement sans réseau
une fois le contenu téléchargé (service worker + IndexedDB).

QUALITÉ : code TypeScript propre et modulaire (composant par type d'activité),
typage des données du manifest et des unités, gestion des états vides et des erreurs
réseau, interface sobre et lisible, thème clair par défaut. Fournir des données de
démonstration si le réseau est indisponible pendant le développement.
```

---

## 2. Prompts d'itération (à envoyer un par un, après le prompt maître)

### 2.1 Écran d'accueil & onboarding
```
Améliore l'onboarding : un écran de bienvenue expliquant que l'app est gratuite, sans
publicité et sans collecte de données, puis un choix de série (A4, C, D) avec de grandes
cartes, puis un écran de téléchargement du contenu avec barre de progression. Mémorise la
série choisie en local. Sur l'Accueil, affiche : le streak (jours consécutifs), un objectif
quotidien (nombre d'activités), un bouton « Continuer » qui reprend le dernier chapitre, et
un bloc « À réviser aujourd'hui » indiquant le nombre de cartes dues.
```

### 2.2 Parcours de matière façon Duolingo
```
Transforme la liste des unités d'une matière en un « chemin » vertical façon Duolingo :
des jalons reliés, l'unité courante mise en avant, les unités verrouillées grisées tant
que leurs prerequis ne sont pas validés, et une pastille de progression par unité
(pourcentage d'étapes réussies). Au clic sur une unité ouverte, aller au détail de l'unité.
```

### 2.3 Lecteur d'activités + règle de correction
```
Implémente un lecteur d'activités plein écran qui enchaîne les activités d'une étape une
par une, avec barre de progression en haut. Pour chaque type, un composant dédié. Feedback
immédiat (correct/incorrect + explication). RÈGLE IMPORTANTE : pour les exercices
(exercice_corrige et banqueExercices), l'élève tente d'abord ; en cas d'échec, révéler la
reponseFinale puis dérouler etapesCorrection étape par étape. À la fin de l'étape, afficher
un écran de résultats (score en %, XP gagné, seuilReussite atteint ou non).
```

### 2.4 Répétition espacée (Anki)
```
Ajoute un moteur de répétition espacée (algorithme SM-2 simplifié). Chaque carte
(formule, rappel_actif, citation, et définitions) a un état local { facilite, intervalle,
prochaineRevision }. Après révélation, l'élève s'auto-évalue via 4 boutons
(À revoir / Difficile / Correct / Facile) qui ajustent l'intervalle. L'onglet « Réviser »
propose : « À réviser aujourd'hui » (toutes matières), et la révision par matière.
Persiste tout en local.
```

### 2.5 Vidéos d'explication
```
Dans le détail d'une unité, si le champ "videos" existe, affiche une section « Vidéos
d'explication » : pour chaque vidéo, un item cliquable avec le titre, la chaîne/auteur, et
une icône type (vidéo ou playlist). Le clic ouvre l'URL YouTube dans un nouvel onglet.
Gère proprement l'absence de vidéos.
```

### 2.6 Révision par catégorie (v2)
```
Exploite le champ "categorie" des étapes (definitions, citations, these, antithese,
exemples, methode). Dans l'onglet Réviser, ajoute des filtres permettant de réviser
uniquement une catégorie (ex. « seulement les citations » en philosophie, « seulement les
formules » en sciences).
```

### 2.7 Hors-ligne & PWA
```
Rends l'app installable (PWA) et pleinement fonctionnelle hors-ligne : service worker qui
met en cache l'app et les JSON de contenu, stockage du contenu en IndexedDB, écran
« Gérer le contenu » dans les Paramètres (vérifier les mises à jour, retélécharger, taille
du cache, vider le cache). Au lancement en ligne, comparer contentVersion et les versions
de matières au cache et ne mettre à jour que le nécessaire.
```

### 2.8 Progression & paramètres
```
Onglet Progression : streak, XP total, nombre de cartes maîtrisées, progression par
matière (barres). Onglet Paramètres : changer de série, gérer le contenu, objectif
quotidien et rappels (notifications locales), thème clair/sombre, taille de police, et
page « À propos » (gratuit, sans pub, sans collecte de données + crédits des chaînes vidéo).
```

---

## 3. Prompt d'intégration des données réelles (GitHub)

```
Branche l'app sur le contenu réel. Écris un module de données TypeScript qui :
1. Récupère le manifest : GET "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/manifest.json".
2. Pour la série choisie, résout chaque matière : si "ref", va la chercher dans
   manifest.matieresPartagees ; sinon utilise la matière propre.
3. Pour chaque matière, charge son "matiere.json" (chemin + "matiere.json") et ses unités
   via le champ "fichier" de chaque unité (préfixé par baseUrl).
4. Définit des types TypeScript pour Manifest, Matiere, Unite, Etape, Activite (union
   discriminée sur "type"), et VideoRef { titre, url, type, chaine, auteur }.
5. Met en cache manifest + JSON en IndexedDB avec leur version ; au prochain lancement,
   ne retélécharge que si contentVersion (ou version de matière) a augmenté.
Gère les erreurs réseau (réessai + fallback sur le cache).
```

---

## 4. Conseils d'utilisation d'AI Studio

- Générez d'abord le squelette avec le **prompt maître**, testez la navigation, puis
  ajoutez les fonctionnalités **une à une** (2.1 → 2.8) pour garder des générations stables.
- Après chaque itération, demandez explicitement : « corrige les erreurs TypeScript et
  vérifie que l'app se lance ».
- Si une génération casse quelque chose, revenez à l'étape précédente et reformulez plus
  précisément (une seule fonctionnalité à la fois).
- Pour le contenu réel, n'utilisez le **prompt §3** qu'une fois l'interface stable.
- Publiez la PWA (hébergement statique gratuit : GitHub Pages, Netlify, Vercel) — cohérent
  avec l'objectif « gratuit, sans serveur, sans collecte ».
```
