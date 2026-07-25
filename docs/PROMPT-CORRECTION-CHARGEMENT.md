# Prompt de correction — Chargement du contenu GitHub

Collez ce prompt dans Google AI Studio si l'app affiche « 0/0 Unités » ou ne charge
pas les chapitres correctement.

---

```
Il y a un bug critique dans Kabro Edu : certaines matières affichent « 0 / 0 Unités »
et les chapitres n'apparaissent pas. Le problème vient du chargement des données depuis
GitHub. Corrige le ContentRepository en suivant EXACTEMENT cet algorithme :

BASE_URL = "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/"

ÉTAPE 1 — Charger le manifest :
  GET BASE_URL + "manifest.json"
  Résultat : un objet avec { contentVersion, matieresPartagees, series[] }

ÉTAPE 2 — Résoudre les matières de la série choisie :
  Pour chaque entrée dans series[serieChoisie].matieres[] :
    - Si l'entrée a un champ "ref" (ex: { "ref": "histoire" }) :
        → c'est une RÉFÉRENCE. Va chercher l'objet complet dans
          manifest.matieresPartagees["histoire"]. Cet objet contient { id, titre,
          version, chemin, fichiers[] }.
    - Si l'entrée a un champ "id" (ex: { "id": "svt-d", "chemin": "d/svt/", ... }) :
        → c'est une matière PROPRE. Utilise-la directement.
    IMPORTANT : "histoire" et "geographie" sont des RÉFÉRENCES DISTINCTES. Ne les
    fusionne JAMAIS. Chacune doit être une tuile séparée dans la liste des matières.

ÉTAPE 3 — Charger le matiere.json de chaque matière :
  Pour chaque matière résolue, construis l'URL :
    url = BASE_URL + matiere.chemin + "matiere.json"
    Exemple : BASE_URL + "partage/histoire/" + "matiere.json"
             = "https://raw.githubusercontent.com/Benahmat235/bac-contenu/main/partage/histoire/matiere.json"
  Télécharge-le. Il contient : { id, titre, description, unites[] }
  Le tableau "unites" donne la LISTE ORDONNÉE des chapitres avec { id, titre, ordre, fichier }.
  C'est CE tableau qui détermine le nombre d'unités à afficher (PAS le manifest).

ÉTAPE 4 — Afficher les unités :
  Pour chaque unité dans matiere.unites[] :
    - Affiche son titre dans l'ordre du champ "ordre" (croissant).
    - Si l'unité a "estRevision": true, affiche-la avec une icône spéciale (🔁).
    - Le nombre total d'unités = matiere.unites.size
      → Affiche "x / N Unités" où N = ce nombre.

ÉTAPE 5 — Charger le contenu d'une unité (au clic) :
  url = BASE_URL + unite.fichier
  Exemple : BASE_URL + "partage/histoire/unite-01.json"
  L'objet contient : { id, titre, etapes[], flashcards?, formulaire?, banqueExercices?, videos? }

RÈGLES CRITIQUES :
- Préfixe TOUJOURS les chemins relatifs par BASE_URL avant tout appel réseau.
- Ne charge PAS toutes les unités d'un coup — charge matiere.json pour la liste,
  et le contenu complet d'une unité uniquement quand l'utilisateur l'ouvre.
- Gère l'erreur réseau : si un appel échoue, affiche un message « Erreur de chargement,
  réessayer » au lieu de laisser « 0/0 ».
- Ajoute des logs : Log.d("ContentRepo", "Loading: $url → ${response.code}")

TYPES DANS LE CODE (data classes Kotlin) :

data class ManifestDto(
    val contentVersion: Int,
    val matieresPartagees: Map<String, MatiereRefDto>,
    val series: List<SerieDto>
)
data class SerieDto(val id: String, val titre: String, val matieres: List<MatiereEntryDto>)
data class MatiereEntryDto(
    val ref: String? = null,   // Si c'est une référence
    val id: String? = null,    // Si c'est une matière propre
    val titre: String? = null,
    val type: String? = null,
    val version: Int? = null,
    val chemin: String? = null,
    val fichiers: List<String>? = null
)
data class MatiereRefDto(
    val id: String, val titre: String, val version: Int,
    val chemin: String, val fichiers: List<String>
)
data class MatiereDetailDto(
    val id: String, val titre: String, val description: String?,
    val unites: List<UniteRefDto>
)
data class UniteRefDto(
    val id: String, val titre: String, val ordre: Int,
    val fichier: String, val prerequis: List<String> = emptyList(),
    val estRevision: Boolean = false
)

VÉRIFIE que après correction :
- L'écran "Histoire" affiche "0 / 8 Unités" (8 unités)
- L'écran "Géographie" affiche "0 / 8 Unités" (8 unités)
- L'écran "SVT" (série D) affiche "0 / 16 Unités"
- Les unités sont dans l'ordre croissant (1, 2, 3...)
- Les unités de révision (estRevision=true) ont une icône distincte
```

---

## Prompt complémentaire — Intégrer le moteur SM-2

Après avoir corrigé le chargement, collez ce prompt pour brancher le vrai moteur
de répétition espacée (les fichiers Kotlin sont dans `docs/code-kotlin/`) :

```
Intègre le moteur de répétition espacée SM-2 dans Kabro Edu. Voici les fichiers à ajouter :

1. Crée le package com.kabroedu.app.data.local.spaced avec :
   - SpacedRepetitionEngine.kt (objet singleton avec la méthode review())
   - CardState.kt (entité Room @Entity "card_states")
   - CardStateDao.kt (DAO avec getCardsDueToday, countDueToday, upsertCardState...)

2. Crée com.kabroedu.app.data.local.LocalDateConverter.kt (TypeConverter pour Room).

3. Ajoute CardState aux @Entity de ta base Room et LocalDateConverter aux @TypeConverters.

4. Crée/modifie ReviewViewModel pour utiliser CardStateDao et SpacedRepetitionEngine.

5. Dans le composable FlashCard (après révélation de la réponse) :
   - Affiche 4 boutons en bas : "À revoir" / "Difficile" / "Correct" / "Facile"
   - Au clic sur un bouton : appelle reviewViewModel.rateCard(cardId, rating)
   - Le cardId = unite.id + "-" + index de la carte dans le formulaire/flashcards

6. Dans l'étape "Mémoriser le cours" : quand l'élève voit une carte pour la 1ère fois,
   appelle reviewViewModel.markFirstSeen(cardId) → elle entre dans la file de révision.

7. Sur l'Accueil : affiche le compteur de cartes dues (reviewViewModel.dueCount).

8. Dans l'onglet "Réviser" : charge les cartes dues (reviewViewModel.dueCards) et
   présente-les en mode flashcard avec les 4 boutons de notation.

L'algorithme :
- À revoir → interval = 0, revoir tout de suite
- Difficile → interval augmente peu
- Correct → interval × ease (×2.5 environ)
- Facile → interval × ease × 1.3
- Carte "maîtrisée" quand interval >= 21 jours
```
