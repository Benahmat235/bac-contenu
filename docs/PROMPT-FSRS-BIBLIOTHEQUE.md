# Prompt — Remplacer le SM-2 maison par une bibliothèque éprouvée

## Option A (recommandée) — FSRS via la bibliothèque Java MIT

À coller dans Google AI Studio :

```
Remplace l'implémentation maison de SM-2 de Kabro Edu par la bibliothèque officielle
java-fsrs (algorithme FSRS, celui utilisé par Anki aujourd'hui). N'ÉCRIS PAS l'algorithme
toi-même : utilise la bibliothèque.

1. DÉPENDANCE (build.gradle.kts du module app)
   implementation("io.github.open-spaced-repetition:fsrs:<dernière version>")
   Cette bibliothèque est sous licence MIT et requiert Java 17.
   Vérifie que compileOptions/kotlinOptions ciblent bien Java 17 :
     compileOptions { sourceCompatibility = JavaVersion.VERSION_17
                      targetCompatibility = JavaVersion.VERSION_17 }
     kotlinOptions { jvmTarget = "17" }

2. API À UTILISER (ne pas réinventer)
   import io.github.openspacedrepetition.Scheduler;
   import io.github.openspacedrepetition.Card;
   import io.github.openspacedrepetition.Rating;
   import io.github.openspacedrepetition.CardAndReviewLog;

   val scheduler = Scheduler.builder()
       .desiredRetention(0.9)                     // 90 % de rétention visée
       .maximumInterval(365)                      // pas au-delà d'un an (préparation au bac)
       .enableFuzzing(true)
       .build()

   // Toute carte neuve est due immédiatement
   var card = Card.builder().build()

   // Notation : Rating.AGAIN(1) / HARD(2) / GOOD(3) / EASY(4)
   val result = scheduler.reviewCard(card, Rating.GOOD)
   card = result.card()
   val due: Instant = card.getDue()

3. CORRESPONDANCE DES 4 BOUTONS DE L'INTERFACE
   « À revoir »  -> Rating.AGAIN
   « Difficile » -> Rating.HARD
   « Correct »   -> Rating.GOOD
   « Facile »    -> Rating.EASY

4. PERSISTANCE AVEC ROOM
   Card et ReviewLog sont sérialisables en JSON via toJson() / fromJson().
   Crée une entité Room :
     @Entity(tableName = "card_states")
     data class CardStateEntity(
         @PrimaryKey val cardId: String,   // ex. "svt-d-u003-2"
         val cardJson: String,             // Card sérialisée par la bibliothèque
         val dueEpochMillis: Long          // copie de card.getDue() pour pouvoir trier/filtrer en SQL
     )
   La colonne dueEpochMillis est indispensable : elle permet la requête
   « cartes dues aujourd'hui » directement en SQL, sans désérialiser chaque carte.

     @Query("SELECT * FROM card_states WHERE dueEpochMillis <= :now ORDER BY dueEpochMillis ASC")
     fun getDueCards(now: Long): Flow<List<CardStateEntity>>

     @Query("SELECT COUNT(*) FROM card_states WHERE dueEpochMillis <= :now")
     fun countDue(now: Long): Flow<Int>

5. IMPORTANT — FUSEAU HORAIRE
   java-fsrs travaille EXCLUSIVEMENT en UTC. Stocke les échéances en epoch millis
   (UTC) et ne convertis en heure locale que pour l'affichage.

6. SUPPRESSION DE L'ANCIEN CODE
   Supprime SpacedRepetitionEngine.kt (implémentation maison) et fais passer
   ReviewViewModel par le Scheduler de la bibliothèque. Conserve l'interface publique du
   ViewModel (dueCount, dueCards, rateCard, markFirstSeen) pour ne pas casser l'UI.

7. NOTION DE « CARTE MAÎTRISÉE »
   Remplace le critère « interval >= 21 jours » par la récupérabilité fournie par la
   bibliothèque : scheduler.getCardRetrievability(card). Considère une carte maîtrisée
   lorsque son échéance est à plus de 21 jours dans le futur.

8. ATTRIBUTION
   Ajoute la mention de la bibliothèque et de sa licence MIT dans l'écran « À propos » :
   « Répétition espacée propulsée par FSRS (Open Spaced Repetition, licence MIT). »
```

## Option B — Corriger le SM-2 maison (sans dépendance)

Si vous préférez éviter toute dépendance externe, ce prompt corrige
l'implémentation actuelle pour qu'elle respecte la vraie spécification SM-2 :

```
Corrige SpacedRepetitionEngine pour respecter FIDÈLEMENT la spécification originale
SM-2 de SuperMemo. L'implémentation actuelle s'en écarte sur quatre points.

ÉCHELLE DE NOTATION : q de 0 à 5 (et non 0 à 3).
  « À revoir » -> q = 0
  « Difficile » -> q = 3
  « Correct »   -> q = 4
  « Facile »    -> q = 5

ALGORITHME EXACT À IMPLÉMENTER :
  1. Nouvelle carte : EF = 2.5, repetitions = 0
  2. Intervalles :
       I(1) = 1 jour
       I(2) = 6 jours          <-- ACTUELLEMENT 3 JOURS, C'EST FAUX
       I(n) = arrondi( I(n-1) * EF )   pour n > 2
  3. Après chaque révision, mise à jour du facteur de facilité avec la formule exacte :
       EF' = EF + ( 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02) )
       puis EF' = max(1.3, EF')
     (remplace les ajustements arbitraires -0.3 / -0.15 / +0.1 actuels)
  4. Si q < 3 (échec) : repetitions = 0 et l'intervalle repart à I(1) = 1 jour,
     MAIS on conserve l'EF calculé à l'étape 3.
     (actuellement l'intervalle est mis à 0, ce n'est pas conforme)

Écris des tests unitaires vérifiant :
  - une carte notée « Correct » quatre fois de suite donne les intervalles 1, 6, 15, 37 jours
    environ (avec EF ≈ 2.5)
  - une carte notée « À revoir » revient à 1 jour tout en gardant un EF réduit
  - EF ne descend jamais sous 1.3

Référence : formulation originale de SuperMemo (Algorithme SM-2, 1987).
```

## Comparatif

| Critère | Option A — java-fsrs | Option B — SM-2 corrigé |
|---|---|---|
| Précision de la planification | Élevée (algorithme actuel d'Anki) | Correcte (algorithme de 1987) |
| Code à maintenir | Aucun (bibliothèque) | L'algorithme complet |
| Dépendance externe | Oui (MIT, ~quelques centaines de Ko) | Aucune |
| Java minimum | 17 | Indifférent |
| Risque d'erreur d'implémentation | Nul | Réel |

**Recommandation** : Option A. La planification est meilleure, il n'y a rien à
maintenir, et la licence MIT n'impose aucune contrainte sur le reste de l'app.

Détails et sources : voir `ALGORITHME-REPETITION-ESPACEE.md`.
