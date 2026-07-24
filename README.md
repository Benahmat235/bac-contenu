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
c/                       Matières propres à la série C (maths, physique, chimie).
d/                       Matières propres à la série D (maths, physique, chimie).
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
