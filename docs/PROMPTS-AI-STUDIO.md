# Prompts Google AI Studio — Kabro Edu (Android natif Java/Kotlin)

Prompts **prêts à coller** dans Google AI Studio (onglet **Build**).
Cible : **application Android native** en Kotlin + Java, Jetpack Compose.

> **Ordre d'utilisation**
> 1. **PROMPT 1** (§1) → génère le frontend avec données de démonstration.
> 2. **PROMPT 2** (§2) → peaufine le design façon Duolingo.
> 3. **Prompts d'itération** (§4) → une fonctionnalité à la fois.
> 4. **PROMPT 3** (§3) → branche le contenu réel GitHub.
> 5. Voir `PROMPT-CORRECTION-CHARGEMENT.md` en cas de bug « 0/0 Unités ».

---

## 1. PROMPT 1 — Démarrage du frontend

```
Crée une application Android native nommée « Kabro Edu », en Kotlin (principal) et Java,
avec Jetpack Compose pour l'UI. C'est une app d'aide à la préparation du baccalauréat
tchadien, 100 % en français, pour les séries A4 (littéraire), C et D (scientifiques). Style
d'apprentissage inspiré de Duolingo (parcours par étapes) et d'Anki (mémorisation espacée).

STACK TECHNIQUE :
- Langages : Kotlin (principal) + Java (utilitaires si besoin).
- UI : Jetpack Compose + Material 3.
- Architecture : MVVM (ViewModel + StateFlow/LiveData).
- Navigation : Navigation Compose (BottomNavigation 5 onglets).
- Stockage local : Room (progression, cache, intervalles de révision) + DataStore (préférences).
- Réseau (plus tard) : Retrofit + OkHttp + Gson.
- Pas de backend, pas de Firebase, pas de collecte de données.

Pour cette première version, construis UNIQUEMENT le FRONTEND avec des DONNÉES DE
DÉMONSTRATION codées en dur (DemoData.kt). Objectif : interface belle, fluide, navigable.

IDENTITÉ VISUELLE — Kabro Edu :
- Bleu profond #002664 (Primary), jaune #FECB00 (accents/récompenses), rouge #C60C30 (streak/alertes).
- Fond clair, cartes arrondies (RoundedCornerShape(16.dp)), boutons larges (48dp min),
  typographie Material 3 lisible, contrastes élevés.

NAVIGATION : NavigationBar avec 5 onglets —
Accueil · Matières · Réviser · Progression · Paramètres.

ÉCRANS (données factices réalistes en français) :
1. Onboarding (1er lancement, vérifié via DataStore) : bienvenue « Kabro Edu » + message
   « Gratuit · Sans publicité · Sans collecte de données », puis choix de la série
   (3 grandes cartes : A4, C, D), mémorisé dans DataStore.
2. Accueil : streak (flamme + jours), anneau de progression de l'objectif du jour
   (CircularProgressIndicator), bouton « Continuer », carte « À réviser aujourd'hui ».
3. Matières (LazyColumn) : matières de la série + barre de progression (ex. série D : Maths,
   Physique, Chimie, SVT, Histoire, Géographie, Français, Anglais).
4. Parcours d'une matière : chapitres en « chemin » vertical façon Duolingo (jalons ronds
   dessinés au Canvas, reliés par un trait), chapitre courant mis en avant, verrouillés
   grisés avec cadenas, pastille de progression par chapitre.
5. Détail d'un chapitre : liste des étapes (Mémorisation · Quiz · Méthode · Exercices · Défi),
   section « Vidéos d'explication » (items titre + chaîne, ouvre YouTube via Intent),
   bouton « Démarrer ».
6. Lecteur d'activité (plein écran, sans BottomNav) : LinearProgressIndicator en haut, une
   activité à la fois, exemple de QCM (choix unique) et de flashcard (recto/verso + 4 boutons
   d'auto-évaluation : À revoir / Difficile / Correct / Facile), feedback immédiat +
   explication, puis écran de résultats (score %, XP gagné).
7. Réviser : « À réviser aujourd'hui » + révision par matière.
8. Progression : streak, XP total, cartes maîtrisées, barres par matière.
9. Paramètres : changer de série, apparence (thème clair/sombre, taille de police),
   objectif quotidien, page « À propos » (gratuit, sans pub, sans collecte de données).

PACKAGES : com.kabroedu.app → ui.screens, ui.components (ActivityCard, ProgressRing,
ChapterNode, FlashCard…), ui.theme, data.model, data.repository, data.local (Room),
data.demo (DemoData.kt), viewmodel (un ViewModel par feature).

QUALITÉ : MVVM propre, composables réutilisables avec @Preview, code commenté en français.
AUCUNE publicité, AUCUN formulaire de collecte de données, AUCUN SDK de tracking.
```



---

## 2. PROMPT 2 — Peaufinage du design (façon Duolingo)

```
Améliore le design de Kabro Edu façon Duolingo avec Jetpack Compose :
- Animations légères (animateColorAsState, AnimatedVisibility, animateFloatAsState) sur les
  bonnes/mauvaises réponses et les gains d'XP.
- Un emoji mascotte sympathique sur l'Accueil qui encourage l'élève.
- Le « chemin » des chapitres dessiné au Canvas (drawCircle + drawLine) : jalons ronds
  reliés, étape en cours animée (effet pulse via InfiniteTransition + scale), cadenas sur
  les verrouillés.
- Cartes de matière avec couleur/emoji distinct par matière (Maths, Chimie, SVT, Physique…).
- LinearProgressIndicator fluide (animateFloatAsState) dans le lecteur.
- Écran de résultats festif : confettis discrets (particules animées au Canvas).
Conserve la palette (#002664, #FECB00, #C60C30), Material 3, rendu épuré et lisible.
```

---

## 3. PROMPT 3 — Brancher le contenu réel GitHub

```
Branche Kabro Edu sur le contenu réel. Écris un module Kotlin (com.kabroedu.app.data.remote) :
1. Service Retrofit ContentApi : GET("manifest.json") → ManifestDto ; GET("{path}") → JSON brut.
2. Data classes : ManifestDto, MatiereDto, UniteDto, EtapeDto, ActiviteDto (sealed class sur
   le champ "type"), VideoRefDto.
3. ContentRepository :
   - baseUrl = "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/"
   - Charge le manifest ; pour la série choisie, résout chaque matière (si "ref" → matieresPartagees).
   - Charge chaque matiere.json puis chaque unité via son champ "fichier" (préfixé par baseUrl).
   - Stocke tout dans Room (entité ContentCache : path, json, version).
   - Ne retélécharge que si contentVersion (global) ou version (matière) a augmenté.
   - Hors-ligne : fallback sur le cache Room.
4. Gestion des erreurs réseau (réessai + repli sur cache).
5. Expose les données au ViewModel via Flow.
```

---

## 4. Prompts d'itération (un par un, après le PROMPT 1)

### 4.1 Onboarding & accueil
```
Améliore l'onboarding Android : écran de bienvenue « Kabro Edu » (gratuit, sans pub, sans
collecte de données), choix de série (A4/C/D) en grandes cartes Material 3, puis écran de
téléchargement avec CircularProgressIndicator. Mémorise la série dans DataStore. Accueil :
streak (flamme + jours), objectif quotidien (anneau), bouton « Continuer », Card
« À réviser aujourd'hui ».
```

### 4.2 Parcours Duolingo
```
Transforme la liste des unités en un « chemin » vertical façon Duolingo : LazyColumn +
composable ChapterNode dessinant un jalon rond (Canvas drawCircle) relié au suivant par un
trait. Unité courante mise en avant (scale + couleur primaire), verrouillées grisées avec
Icons.Default.Lock, pastille de progression. Les unités de révision (estRevision=true)
ont une icône distincte. Au clic → détail de l'unité.
```

### 4.3 Lecteur d'activités + règle de correction
```
Implémente un lecteur d'activités plein écran qui enchaîne les activités une par une
(LinearProgressIndicator en haut, un composable par type). Feedback immédiat (couleur +
explication). RÈGLE : pour les exercices (banqueExercices), l'élève tente d'abord ; en cas
d'échec, afficher reponseFinale en grand puis dérouler etapesCorrection pas à pas
(expand/collapse). Fin d'étape : écran de résultats (score %, XP, seuilReussite, « Continuer »).
```

### 4.4 Répétition espacée (Anki / SM-2)
```
Ajoute un moteur de répétition espacée (SM-2 simplifié) avec Room. Entité CardState { cardId,
ease, interval, repetitions, nextReview }. Après révélation, 4 boutons (À revoir=0 /
Difficile=1 / Correct=2 / Facile=3) ajustent ease et interval. Onglet Réviser :
« À réviser aujourd'hui » (toutes matières) + révision par matière. Affiche le nombre de
cartes dues sur l'Accueil. Carte « maîtrisée » quand interval >= 21 jours.
(Le code complet est fourni dans docs/code-kotlin/.)
```

### 4.5 Vidéos d'explication
```
Dans le détail d'une unité, si "videos" existe, affiche une section « Vidéos d'explication » :
chaque item Material 3 avec titre + chaîne (auteur) + icône (vidéo/playlist). Au clic, ouvre
l'URL via Intent (ACTION_VIEW, Uri.parse(url)). Masque la section s'il n'y a pas de vidéo.
```

### 4.6 Hors-ligne & cache
```
Rends Kabro Edu fonctionnelle hors-ligne : télécharge manifest + JSON (Retrofit), stocke en
Room. Si hors-ligne, charge depuis Room. Si en ligne, compare contentVersion et versions de
matières et ne retélécharge que le nécessaire. WorkManager pour une vérification quotidienne.
Indicateur « Contenu à jour » / « Mise à jour disponible » dans les Paramètres.
```

### 4.7 Progression & paramètres
```
Onglet Progression : streak (réinitialisé si un jour manqué), XP total, cartes maîtrisées,
barres par matière. Paramètres : changer de série, gérer le contenu, objectif quotidien
(slider 5-30), thème clair/sombre, taille de police, page « À propos » (gratuit/sans pub/
sans collecte + crédits des chaînes vidéo).
```

---

## 5. Conseils d'utilisation

- Générez le squelette avec le **PROMPT 1**, testez dans l'émulateur, puis ajoutez les
  fonctionnalités **une à une** (§4) pour garder un projet stable.
- Après chaque itération : « corrige les erreurs de compilation et vérifie que l'app se
  lance sur l'émulateur Android ».
- Si une génération casse quelque chose, revenez en arrière et reformulez (une seule
  fonctionnalité à la fois).
- N'utilisez le **PROMPT 3** (contenu réel) qu'une fois l'interface stable.
- Publiez l'APK/AAB signé sur le Play Store, ou en APK direct (sideload).
