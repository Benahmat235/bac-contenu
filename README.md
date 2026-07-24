# bac-contenu

Contenu pédagogique de l'application de préparation au baccalauréat tchadien.

- **Public** : Terminale, Tchad — séries **A4**, **C** et **D**.
- **Rôle** : ce dépôt contient uniquement le contenu au format **JSON**, en **lecture seule**, lu par l'application.
- **Confidentialité** : aucune donnée personnelle. Aucun PDF ici. Les PDF sources restent dans un dépôt privé séparé (`bac-data`).

## Structure

```text
manifest.json            Point d'entrée. Versions et liste des matières par série.
partage/                 Matières communes à toutes les séries.
  anglais/  francais/  histoire/  geographie/
a4/                      Matières propres à la série A4 (philosophie, mathématiques).
c/                       Matières propres à la série C (maths, physique, chimie, SVT).
d/                       Matières propres à la série D (maths, physique, chimie, SVT).
```

Chaque matière contient :
- `matiere.json` : liste ordonnée de ses unités.
- `unite-XX.json` : les étapes et activités d'une unité.

## Mise à jour du contenu

1. L'application lit d'abord `manifest.json`.
2. `contentVersion` (global) et `version` (par matière) indiquent s'il faut retélécharger.
3. Après modification d'une matière, **incrémenter sa `version`** et **incrémenter `contentVersion`**.

## Règle des identifiants

Les identifiants (`id` d'unité, d'étape, `competenceId`) sont **stables à vie**.
On peut modifier librement les textes, mais **jamais** un identifiant existant :
la progression des élèves est reliée à ces identifiants.

## Types d'activités disponibles

| type | description | champs principaux |
|---|---|---|
| `choix_unique` | une seule bonne réponse | `question`, `options`, `reponse` (index), `explication` |
| `choix_multiple` | plusieurs bonnes réponses | `question`, `options`, `reponses` (indices), `explication` |
| `association` | relier des paires | `consigne`, `paires` (`gauche`/`droite`), `explication` |
| `classement` | remettre dans l'ordre | `consigne`, `elements`, `ordreCorrect`, `explication` |
| `texte_a_completer` | compléter un blanc | `phrase` (avec `___`), `reponse`, `toleranceMots`, `explication` |
| `rappel_actif` | retrouver de mémoire (mode flashcard) | `question`, `reponseAttendue`, `toleranceMots`, `modeReponse`, `explication` |
| `reponse_courte` | réponse ouverte courte | `question`, `reponsesAcceptees`, `explication` |
| `citation` | attribuer/compléter une citation | `mode` (`attribuer`), `citation`, `options`, `reponse` (index), `oeuvre`, `explication` |

## Champ optionnel `videos` (explications vidéo)

Une unité peut porter un tableau `videos` : des vidéos d'explication (gratuites)
recommandées pour le chapitre. L'application peut les proposer en complément.

```json
"videos": [
  {
    "titre": "1) Les types de référentiels",
    "url": "https://youtu.be/bFS1x4T2els",
    "type": "video",
    "chaine": "Education Plus",
    "auteur": "Al Habib Idriss"
  }
]
```

- `type` : `"video"` (une vidéo) ou `"playlist"` (un cours complet).
- Chaque vidéo indique sa `chaine` / `auteur` (attribution).

### Couverture et sources vidéo

Presque toutes les unités (≈ 106/108) proposent des vidéos, toutes vérifiées
(liens actifs via l'API oEmbed). Chaînes privilégiées :

- **Séries C et D — chaîne de référence tchadienne** : **Education Plus**
  (Al Habib Idriss) — https://youtube.com/@alhabibidriss39 (cinématique, suites,
  nombres complexes, barycentre, dérivées, exponentielle, projectile...).
- **Programme francophone africain** : Ecoles Au Senegal, Elite Science,
  Le savant ACHI, Ecole Virtuelle Africaine, club cedeao, M. Kanté.
- **Compléments** : Yvan Monka (maths), Paul Olivier / e-profs (physique-chimie),
  Les Bons Profs, Coursitout (philosophie), English with Lucy (anglais)...
- **Non couvert** : `geo-u3` et `geo-u5` (géographie du Tchad) — seules des
  vidéos d'actualité existent, aucun cours pédagogique adapté.
- Scripts d'injection réutilisables (idempotents) : `add_videos.py`,
  `add_videos_sciences.py`, `add_videos_matphys.py`, `add_videos_lettres.py`.

## Champ optionnel `categorie` (utile en philosophie)

Une étape peut porter une `categorie` pour organiser la mémorisation :
`definitions`, `citations`, `these`, `antithese`, `exemples`, `methode`.
Cela permet de réviser par type (ex. « réviser uniquement les citations »).

## Champs d'une étape

- `id`, `titre`, `objectif`
- `seuilReussite` : pourcentage requis pour valider l'étape (ex. 80).
- `competenceId` : compétence évaluée (utilisée par les révisions espacées).
- `estDefi` : `true` pour un défi de maîtrise en fin d'unité.
- `activites` : liste ordonnée des activités.


## Mode réponse des cartes de rappel

Quand une carte `rappel_actif` porte `"modeReponse": "auto_evaluation"`, la réponse
attendue est une réponse modèle (souvent longue). L'application ne corrige pas
automatiquement : elle révèle la réponse modèle, puis l'élève s'auto-évalue
(À revoir / Difficile / Correct / Facile), comme sur une carte Anki. Cette note
alimente la répétition espacée.


## Matières scientifiques (modèle distinct)

Les matières `"type": "scientifique"` (mathématiques, physique, chimie, SVT) ne suivent
PAS la logique de mémorisation littéraire.

> **SVT (séries C et D)** : matière scientifique adaptée. Le `formulaire` mémorise
> surtout des **définitions et lois** (cellule, ADN, lois de Mendel, hormones…), et
> la `banqueExercices` propose des exercices corrigés issus du manuel officiel
> (questions-réponses, croisements génétiques notés `A//a`, régulation de la glycémie…). Elles reposent sur trois piliers :

1. **Formulaire** : un tableau `formulaire` au niveau de l'unité liste les formules
   à mémoriser en répétition espacée (cartes `formule`, `recto`/`verso`).
2. **Exercices corrigés multiples** : activités `exercice_corrige` et un tableau
   `banqueExercices` fournissant de nombreux exercices avec correction étape par étape.
3. **Quiz** : QCM (`choix_unique`, `choix_multiple`) de compréhension et de méthode.

### Champs spécifiques aux sciences

- Unité : `"type": "scientifique"`, `formulaire` (deck de formules), `banqueExercices`.

### Types d'activités scientifiques

| type | description | champs principaux |
|---|---|---|
| `formule` | mémoriser une formule (flashcard, répétition espacée) | `recto`, `verso`, `modeReponse`, `explication` |
| `exercice_corrige` | exercice avec correction progressive | `enonce`, `difficulte`, `etapesCorrection` (liste), `reponseFinale` |
| `resolution_ordonnee` | remettre les étapes d'une résolution dans l'ordre | `enonce`, `etapes`, `ordreCorrect`, `explication` |

Un objet de `banqueExercices` contient : `id`, `enonce`, `difficulte`,
`etapesCorrection`, `reponseFinale`, `competenceId`. L'application peut y piocher
pour proposer un entraînement illimité, corrigé et espacé dans le temps.
