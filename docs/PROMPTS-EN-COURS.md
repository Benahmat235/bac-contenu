# Prompts en cours — Kabro Edu

Tous les prompts à coller dans Google AI Studio, dans l'ordre d'application.
Chacun est autonome et rappelle le skill `mobile-android-design`.

Installation du skill (déjà fait) :

```bash
npx skills add https://github.com/wshobson/agents --skill mobile-android-design
```

## Ordre recommandé

| Étape | Prompt | Type | Pourquoi cet ordre |
|---|---|---|---|
| 1 | [Étapes vides](#bug-1--liste-des-étapes-vide-bloquant) | Bug bloquant | L'élève ne peut pas apprendre |
| 2 | [Compteurs / markFirstSeen](#bug-2--compteurs-faux-cause-racine) | Bug (cause racine) | Fausse aussi les intervalles |
| 3 | [Palette et thème](#design-16--palette-et-thème) | Design | Base de tout le reste |
| 4 | [Logo](#design-26--intégration-du-logo) | Design | |
| 5 | [Onglets de chapitres](#design-36--onglets-de-chapitres-et-progression-globale) | Design | |
| 6 | [Chemin en zigzag](#design-46--chemin-en-zigzag-et-jalons) | Design | Inclut le verrouillage |
| 7 | [Notes autocollantes](#design-56--notes-autocollantes) | Design | |
| 8 | [Autres écrans](#design-66--alignement-des-autres-écrans) | Design | Inclut les badges |

> Traiter les **deux bugs d'abord** : sinon le redesign réécrit les écrans et il faut tout reprendre.

## Rappels factuels sur le contenu

Vérifications faites directement dans le dépôt — à ne pas contredire :

- Le champ `prerequis` est un tableau **vide `[]`** pour les 441 unités → le verrouillage
  doit s'appuyer sur le champ `ordre`.
- Les cartes utilisent **`recto` / `verso`** (et `question` / `reponseAttendue` pour les
  activités `rappel_actif`). Il n'existe **aucun** champ `prompt`, `answer` ou `hint`.
- Chaque unité porte `source.chapitre` → sert à regrouper les unités en onglets de chapitres.
- Le champ `fichier` d'une unité contient **déjà le chemin complet**
  (ex. `partage/histoire/unite-001.json`) : il suffit de le concaténer au `baseUrl`.
- L'énumération `io.github.openspacedrepetition.State` contient exactement
  `LEARNING`, `REVIEW`, `RELEARNING` — il n'y a **pas** de valeur `NEW`.
- `Scheduler.reviewCard()` retourne `CardAndReviewLog` (et non une `Map`).


---

# BUG 1/2 — Liste des étapes vide (bloquant)

```
Sur l'écran de détail d'un chapitre de Kabro Edu, la section « Étapes de mémorisation &
entraînement : » est VIDE, et le badge affiche « 0 Étapes ». L'élève ne peut donc pas voir
ni lancer les étapes.

Or chaque unité du contenu possède bien un tableau "etapes" non vide. Exemple réel,
partage/histoire/unite-001.json :
    "etapes": [
      { "id": "histoire-u001-memo", "titre": "Mémoriser", "categorie": "memorisation",
        "seuilReussite": 80, "competenceId": "...", "activites": [ ... 4 activités ... ] },
      { "id": "histoire-u001-quiz", "titre": "Vérifier", "categorie": "quiz",
        "seuilReussite": 70, "competenceId": "...", "activites": [ ... 3 activités ... ] }
    ]

À FAIRE :
1. Vérifie le DTO de désérialisation de l'unité : le champ s'appelle exactement "etapes"
   (sans accent, au pluriel). Chaque étape a : id, titre, categorie, seuilReussite,
   competenceId, activites.
2. Journalise (Log.d) le nombre d'étapes parsées juste après la désérialisation, pour
   confirmer que ce n'est pas un problème de nom de champ.
3. Affiche la liste : pour chaque étape, une carte cliquable avec
     - l'icône selon "categorie" : memorisation -> Icons.Default.Style,
       quiz -> Icons.Default.Quiz, methode -> Icons.Default.Lightbulb,
       exercices -> Icons.Default.Edit, defi -> Icons.Default.EmojiEvents,
       revision -> Icons.Default.Autorenew
     - le "titre" de l'étape
     - le nombre d'activités : "N activités"
     - l'état (à faire / en cours / réussie) et le seuilReussite
4. Le badge doit afficher le nombre RÉEL : "2 Étapes" et non "0 Étapes".
5. « Démarrer l'activité » doit lancer la PREMIÈRE étape non terminée.

VALIDATION : ouvrir l'unité 1 d'Histoire -> deux cartes visibles, « Mémoriser » (4 activités)
et « Vérifier » (3 activités), et le badge indique « 2 Étapes ».
```

---

# BUG 2/2 — Compteurs faux (cause racine)

```
Deux incohérences dans Kabro Edu qui ont la MÊME cause racine :
  - l'écran de révision affiche « 0 7 1 » : zéro carte « à apprendre » alors que l'élève
    démarre une unité de 4 cartes jamais vues ;
  - l'écran Progression affiche « 28 cartes maîtrisées » alors que toutes les matières sont
    à 0 % de progression.

=== CAUSE RACINE ===
markFirstSeen() appelle scheduler.reviewCard(card, Rating.GOOD) pour enregistrer une carte
vue pour la première fois. C'est FAUX : cela NOTE la carte et la fait progresser, au lieu de
simplement l'enregistrer comme neuve.

=== CORRECTION 1 : markFirstSeen ne doit RIEN noter ===
markFirstSeen(cardId, matiereId, uniteId) doit :
  - créer la carte avec Card.builder().build() (une carte neuve est due immédiatement) ;
  - la sérialiser telle quelle (card.toJson()) et l'insérer en base ;
  - NE JAMAIS appeler scheduler.reviewCard() ;
  - être idempotent : si la carte existe déjà, ne rien faire (ne pas la réinitialiser).
Après markFirstSeen, la carte doit avoir lastReview == null et state == State.LEARNING.

=== CORRECTION 2 : compteurs réellement réactifs ===
L'énumération io.github.openspacedrepetition.State contient exactement LEARNING, REVIEW,
RELEARNING — il n'existe PAS de valeur NEW. Les catégories se déduisent ainsi :
    À APPRENDRE (bleu #2196F3) : card.getLastReview() == null
    À REVOIR   (rouge #C60C30) : lastReview != null ET state ∈ {LEARNING, RELEARNING}
    APPRISES   (vert #2E7D32)  : state == State.REVIEW

Ces informations sont dans la colonne cardJson, non interrogeable en SQL. Ajoute donc deux
colonnes dénormalisées à CardStateEntity, mises à jour à chaque écriture :
    val stateValue: Int,          // 1=LEARNING, 2=REVIEW, 3=RELEARNING
    val hasBeenReviewed: Boolean  // false tant que lastReview == null

    @Query("SELECT COUNT(*) FROM card_states WHERE uniteId = :uniteId AND hasBeenReviewed = 0")
    fun countNew(uniteId: String): Flow<Int>
    @Query("SELECT COUNT(*) FROM card_states WHERE uniteId = :uniteId AND hasBeenReviewed = 1 AND stateValue IN (1,3)")
    fun countLearning(uniteId: String): Flow<Int>
    @Query("SELECT COUNT(*) FROM card_states WHERE uniteId = :uniteId AND stateValue = 2")
    fun countReview(uniteId: String): Flow<Int>

Incrémente la version de la base Room et conserve fallbackToDestructiveMigration().

=== CORRECTION 3 : « cartes maîtrisées » ===
Une carte n'est « maîtrisée » que si state == State.REVIEW ET son échéance dépasse 21 jours.
Une carte jamais réellement révisée ne doit JAMAIS être comptée comme maîtrisée.

=== VALIDATION ===
Ouvrir une micro-leçon de 4 cartes neuves -> affichage « 4 0 0 » (4 en bleu).
Noter la 1re carte « Correct » -> « 3 1 0 » sans quitter l'écran.
Écran Progression sur une installation neuve -> « 0 cartes maîtrisées ».
```


---

# DESIGN 1/6 — Palette et thème

```
Applique le skill mobile-android-design (Material Design 3 + Jetpack Compose).
Rappel des règles du skill : thème Material 3 centralisé via colorScheme, accès aux couleurs
uniquement par MaterialTheme.colorScheme, @Preview clair et sombre, cibles tactiles de 48 dp.

OBJECTIF : abandonner le thème sombre en verre (bleu marine #002664 + jaune drapeau) au
profit d'un thème CLAIR aligné sur le logo de Kabro Edu.

CONTEXTE : le logo « Kabro Edu » est MONOCHROME (blanc sur fond noir) — livre ouvert, toque
de diplômé, typographie épaisse dans un cadre en biseau. L'identité repose donc sur le noir
et blanc ; le bleu et le jaune ne sont que des couleurs FONCTIONNELLES.

Refonds com.kabroedu.app.ui.theme :

    // Identité (logo)
    val KabroBlack      = Color(0xFF111111)   // texte principal, logo
    val KabroWhite      = Color(0xFFFFFFFF)   // fond principal

    // Fonctionnel
    val KabroBlue       = Color(0xFF4A9DF7)   // jalons actifs, progression, onglet actif
    val KabroBlueDark   = Color(0xFF2B7FD9)   // anneaux et bordures de jalons
    val KabroNoteHeader = Color(0xFFFFE24D)   // bandeau des notes autocollantes
    val KabroNoteBody   = Color(0xFFFFF6C2)   // corps des notes
    val KabroNoteLine   = Color(0xFFE8C84A)   // filets des notes
    val KabroNoteText   = Color(0xFF7A5B14)   // texte sur note (brun foncé, lisible)

    // Neutres
    val KabroSurface    = Color(0xFFF5F6F8)   // barres d'onglets, surfaces secondaires
    val KabroTrack      = Color(0xFFE8EAED)   // pistes de progression
    val KabroLocked     = Color(0xFF9AA0A6)   // éléments verrouillés
    val KabroRedDot     = Color(0xFFEA4335)   // point d'attention

ColorScheme Material 3 — renseigne TOUS les rôles :
    primary = KabroBlue, onPrimary = KabroWhite
    secondary = KabroNoteHeader, onSecondary = KabroNoteText
    background = KabroWhite, onBackground = KabroBlack
    surface = KabroWhite, onSurface = KabroBlack
    surfaceVariant = KabroSurface, onSurfaceVariant = Color(0xFF5F6368)
    outline = Color(0xFFDADCE0), outlineVariant = KabroTrack
    error = Color(0xFFEA4335)

RÈGLE IMPÉRATIVE : dynamicColor = false. Material You dériverait la palette du fond d'écran
de l'élève et effacerait l'identité du logo.

SUPPRESSIONS :
  - Supprime GlassSurface.kt et GlassBackground.kt.
  - Retire tous les dégradés sombres et halos radiaux : le fond des écrans devient blanc uni.
  - Recherche et élimine tout Color(0xFF...) codé en dur dans les écrans : seul le fichier
    de thème doit contenir des valeurs hexadécimales.

Prévois un thème sombre cohérent (fond #121212, notes conservant leur jaune) mais ne le
priorise pas : le thème clair est la cible.

VALIDATION : l'application compile, tous les écrans ont un fond blanc, aucune surface en
verre sombre ne subsiste, et une recherche de « Color(0xFF » hors du dossier theme ne
retourne rien.
```

---

# DESIGN 2/6 — Intégration du logo

```
Applique le skill mobile-android-design (Material Design 3 + Jetpack Compose).
Rappel : contentDescription sur tous les éléments visuels porteurs de sens.

OBJECTIF : intégrer le logo de Kabro Edu dans l'application.

Le logo fourni est BLANC sur fond noir (livre ouvert + toque de diplômé + « Kabro Edu »
dans un cadre en biseau). Il faut donc deux variantes :

  res/drawable/logo_kabro_dark.xml   -> version NOIRE, pour le thème clair (usage principal)
  res/drawable/logo_kabro_light.xml  -> version BLANCHE, pour fond sombre et splash screen

Privilégie le format vector drawable (XML) pour la netteté à toutes les tailles. Si seule une
image bitmap est disponible, place-la en densité xxxhdpi et documente-le.

EMPLACEMENTS :
  - En-tête de l'Accueil : logo NOIR sur fond blanc, hauteur 32 dp, aligné à gauche,
    contentDescription = "Kabro Edu".
  - Écran de démarrage (splash) : logo BLANC centré sur fond KabroBlack, via l'API
    androidx.core:core-splashscreen, avec le slogan « Réussis ton bac, étape par étape ».
  - Icône de l'application (adaptive icon) : foreground = le livre et la toque du logo,
    background = KabroBlack uni. Fournis aussi une version monochrome pour les thèmes
    dynamiques d'Android 13+.

VALIDATION : le logo noir apparaît sur l'Accueil, le logo blanc au démarrage, et l'icône de
l'application s'affiche correctement dans le lanceur (formes ronde, carrée et squircle).
```


---

# DESIGN 3/6 — Onglets de chapitres et progression globale

```
Applique le skill mobile-android-design (Material Design 3 + Jetpack Compose).
Rappel des règles du skill : LazyRow/LazyColumn pour les collections, cibles tactiles de
48 dp, contentDescription, état hissé (state hoisting).

OBJECTIF : ajouter en haut de l'écran d'une matière une barre d'onglets de CHAPITRES avec
cadenas, surmontant une barre de progression globale — comme la rangée
« A1 · A2 · B1 · B1+ · B2 » d'une application d'apprentissage de langues.

=== CORRESPONDANCE AVEC LE CONTENU (point factuel important) ===
Chaque unité du contenu possède un champ "source": { "chapitre": "..." }.
Regroupe les unités par cette valeur pour construire les onglets — n'invente pas de
regroupement. Exemple réel pour Histoire : 5 chapitres (Seconde Guerre mondiale, ONU,
Guerre froide, Décolonisation, OUA et Union Africaine).

=== BARRE D'ONGLETS ===
  - Conteneur : LazyRow, fond KabroSurface, RoundedCornerShape(28.dp), hauteur 56 dp,
    contentPadding horizontal de 6 dp.
  - Onglet ACTIF : pastille pleine KabroBlue, texte blanc, FontWeight.Bold,
    RoundedCornerShape(24.dp), padding horizontal 24 dp.
  - Onglet DÉVERROUILLÉ non actif : fond transparent, texte KabroBlack.
  - Onglet VERROUILLÉ : icône Lock (16 dp) + libellé, tous deux en KabroLocked,
    clickable(enabled = false).
  - Libellés longs : maxLines = 1 avec ellipsis, et affichage du nom complet dans une
    Snackbar au toucher.
  - Un chapitre est déverrouillé si sa PREMIÈRE unité est déverrouillée.

=== BARRE DE PROGRESSION GLOBALE (juste sous les onglets) ===
Disposition sur une seule ligne : [icône] 116/651  [====------------]

  - À gauche : icône de livres, puis « cartesApprises/cartesTotales » en KabroBlack,
    FontWeight.Bold, maxLines = 1, softWrap = false (ne jamais laisser ce texte se couper).
  - À droite : LinearProgressIndicator occupant l'espace restant,
        progress = { if (total == 0) 0f else apprises.toFloat() / total.toFloat() },
        hauteur 10 dp, clip RoundedCornerShape(5.dp),
        color = KabroBlue, trackColor = KabroTrack, strokeCap = StrokeCap.Round
    Utilise la surcharge lambda (« progress = { ... } ») et protège la division par zéro.
  - Les nombres viennent du moteur de répétition espacée : « apprises » = cartes en
    State.REVIEW pour le chapitre courant ; « totales » = nombre total de cartes du chapitre.

VALIDATION : sur Histoire, 5 onglets apparaissent, le premier actif en bleu et les suivants
avec un cadenas gris ; la barre affiche un ratio cohérent et reste vide à 0 %.
```

---

# DESIGN 4/6 — Chemin en zigzag et jalons

```
Applique le skill mobile-android-design (Material Design 3 + Jetpack Compose).
Rappel des règles du skill : LazyColumn obligatoire pour les longues listes (une matière
peut compter 58 unités), n'animer que des propriétés accélérées par le GPU (alpha, scale,
rotation via graphicsLayer), jamais d'animation permanente sur la mise en page.

OBJECTIF : remplacer le chemin vertical actuel par un chemin en ZIGZAG alternant gauche et
droite, avec des jalons circulaires.

=== A. GROS JALON DE CHAPITRE (centré horizontalement) ===
Marque le début d'un nouveau chapitre dans le chemin.
  - Cercle de 96 dp, fond KabroBlue plein, numéro du chapitre en blanc, 36 sp, Bold.
  - Verrouillé : fond KabroLocked, icône Lock à la place du numéro.

=== B. PETIT JALON D'UNITÉ (alterné gauche / droite) ===
  - Cercle de 72 dp, fond KabroBlue, anneau extérieur de 3 dp en KabroBlueDark.
  - Icône centrale blanche selon le type d'unité :
        micro-leçon (défaut)             -> Icons.Default.Edit
        unité de révision (estRevision)  -> Icons.Default.Autorenew
        unité d'exercices (estExercices) -> Icons.Default.Calculate
  - BADGE DE POURCENTAGE : petite pastille bleue chevauchant le bas du cercle, texte blanc
    11 sp (ex. « 100% »). Ne l'affiche QUE si la progression est supérieure à 0.

  QUATRE ÉTATS VISUELS DISTINCTS :
    TERMINÉE    : cercle plein KabroBlue, icône Check, badge « 100% »
    EN COURS    : cercle KabroBlue, anneau animé sur l'ALPHA uniquement,
                  badge du pourcentage réel
    VERROUILLÉE : cercle KabroLocked, icône Lock, AUCUN badge,
                  clickable(enabled = false),
                  Snackbar au toucher : « Termine la leçon précédente pour débloquer »
    DEMAIN      : cercle KabroLocked, icône Schedule, badge « DEMAIN », clic désactivé

=== C. LIAISON ENTRE LES JALONS ===
Trait vertical de 2 dp en KabroTrack, dessiné au Canvas et suivant le zigzag entre les
cercles successifs.

=== D. RÈGLE DE DÉVERROUILLAGE (point factuel important) ===
Dans le contenu, le champ "prerequis" est un tableau VIDE ([]) pour les 441 unités : ne
t'appuie PAS dessus, il ne verrouillerait jamais rien. Utilise le champ "ordre" (entier
croissant présent dans chaque unité du matiere.json) :
  - unité d'ordre 1 : déverrouillée ;
  - unité d'ordre N : déverrouillée seulement si l'unité N-1 est TERMINÉE (toutes ses étapes
    ont atteint leur seuilReussite) ;
  - si "prerequis" devient non vide dans une future version du contenu, il prend la priorité.

L'état de verrouillage doit être un Flow combinant la liste des unités et la progression
stockée en Room, afin que terminer une unité déverrouille IMMÉDIATEMENT la suivante sans
redémarrer l'application.

VALIDATION : le chemin alterne bien gauche/droite ; sur Histoire, seule l'unité 1 est
cliquable et les unités 2 à 20 affichent un cadenas ; terminer l'unité 1 déverrouille
l'unité 2 sans relancer l'app.
```


---

# DESIGN 5/6 — Notes autocollantes

```
Applique le skill mobile-android-design (Material Design 3 + Jetpack Compose).
Rappel des règles du skill : contraste minimal 4.5:1, jamais de Modifier.alpha() sur un
conteneur contenant du texte (appliquer la couleur sur chaque Text), @Preview clair et sombre.

OBJECTIF : ajouter dans le chemin d'apprentissage des cartes d'information en forme de NOTE
AUTOCOLLANTE jaune, placées du côté opposé au jalon correspondant.

APPARENCE :
  - Largeur d'environ 60 % de l'écran, légère rotation de -2° via
    graphicsLayer { rotationZ = -2f } pour l'effet « collé à la main ».
  - Ombre douce : Modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(4.dp)).

  BANDEAU SUPÉRIEUR : fond KabroNoteHeader, hauteur 64 dp, contenant deux lignes :
      ligne 1 : « CHAPITRE N », 11 sp, letterSpacing 1.sp, KabroNoteText
      ligne 2 : titre du chapitre, 16 sp, Bold, KabroNoteText, maxLines = 2, ellipsis

  CORPS : fond KabroNoteBody, trois lignes maximum, chacune préfixée d'une puce et
  soulignée d'un filet KabroNoteLine :
      « N cartes à mémoriser »
      « N questions de vérification »
      « N leçons dans ce chapitre »
  Ces nombres sont calculés à partir des unités du chapitre (comptage des activités de
  catégorie « memorisation » et « quiz »).

  AIMANTS : trois petites pastilles rondes en haut de la note — deux noires de 10 dp et une
  rouge KabroRedDot de 14 dp au centre.

RÈGLE DE LISIBILITÉ IMPÉRATIVE : tout texte posé sur la note utilise KabroNoteText (brun
foncé). Jamais de blanc, de gris clair, ni d'opacité réduite sur le fond jaune — c'est
précisément ce qui avait rendu l'application illisible lors de la version précédente.

VALIDATION : la note apparaît en jaune, légèrement inclinée, avec ses trois aimants, et son
texte brun est parfaitement lisible. Vérifie aussi le rendu en thème sombre (la note conserve
son jaune et son texte brun).
```

---

# DESIGN 6/6 — Alignement des autres écrans

```
Applique le skill mobile-android-design (Material Design 3 + Jetpack Compose).
Rappel des règles du skill : couleurs via MaterialTheme.colorScheme uniquement, cibles
tactiles de 48 dp, contentDescription partout, LazyColumn pour les listes, @Preview clair
et sombre.

OBJECTIF : aligner les écrans restants sur la nouvelle identité claire de Kabro Edu.

=== ÉCRANS À ALIGNER ===
  - ACCUEIL : fond blanc, logo noir en en-tête. Cartes blanches avec bordure outline de 1 dp
    et RoundedCornerShape(16.dp). Bouton principal « Continuer » en KabroBlue à texte blanc
    (il était jaune).
  - LISTE DES MATIÈRES : cartes blanches bordées ; icône de matière dans un cercle
    KabroSurface ; badge de coefficient sur fond KabroNoteHeader avec texte KabroNoteText.
    ATTENTION : ce badge était précédemment blanc sur blanc, donc invisible. Vérifie
    systématiquement que la couleur du texte contraste avec celle du fond du badge.
  - NAVIGATIONBAR : fond blanc, bordure supérieure outline, onglet actif en KabroBlue.
    L'onglet sélectionné doit être déduit de la route courante
    (navController.currentBackStackEntryAsState()) et non d'une variable figée : actuellement
    « Accueil » reste surligné même dans un écran de chapitre.
  - PROGRESSION : fond blanc, statistiques en KabroBlack, barres en KabroBlue sur KabroTrack.

=== À NE PAS TOUCHER ===
  - ÉCRAN DE RÉVISION : conserve exactement la disposition Anki (question ancrée en haut et
    toujours visible, réponse en dessous). Adapte seulement les couleurs : fond blanc,
    question en KabroBlack, réponse dans un Surface KabroSurface. NE MODIFIE PAS la structure.
  - BARRE DES 4 BOUTONS DE NOTATION : conserve les couleurs pleines et opaques
    (Encore rouge, Difficile gris foncé, Correct vert, Facile bleu). Ce codage est
    FONCTIONNEL : il ne dépend pas de l'identité visuelle et ne doit pas être harmonisé.

=== BADGES DE TEXTE (correction d'une régression) ===
Un badge contenant du texte ne doit JAMAIS être contraint à un cercle de taille fixe : le
badge « 470 XP » s'affichait coupé en « 4 / 7 / 0 » sur trois lignes.
Utilise une pastille à largeur adaptative :
    Row(
        modifier = Modifier
            .height(32.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { /* icône + Text(maxLines = 1, softWrap = false) */ }
La contrainte de 48 dp circulaire ne s'applique qu'aux indicateurs de progression
CIRCULAIRES, jamais aux badges textuels.

=== EN-TÊTES SUPERPOSÉS (correction d'une régression) ===
Sur l'Accueil, le contenu passait sous l'en-tête. Place l'en-tête dans le topBar d'un
Scaffold et applique les paddingValues fournis au contenu ; ajoute statusBarsPadding() sur
l'en-tête.

=== VALIDATION FINALE ===
  a) Tous les écrans ont un fond blanc, aucune surface en verre sombre.
  b) Aucun Color(0xFF...) hors du fichier de thème.
  c) « 470 XP » s'affiche sur une seule ligne.
  d) Le badge de coefficient est jaune à texte brun, parfaitement lisible.
  e) Aucun texte ne passe sous un en-tête.
  f) L'onglet surligné correspond toujours à l'écran affiché.
  g) La disposition Anki de l'écran de révision est intacte.
  h) Les 4 boutons de notation restent opaques et distincts.
  i) Chaque nouveau composable possède un @Preview clair et un @Preview sombre.
```
