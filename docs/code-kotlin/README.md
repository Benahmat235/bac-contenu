# Code Kotlin — Moteur de répétition espacée SM-2

Fichiers prêts à intégrer dans le projet Android Studio de Kabro Edu.

## Fichiers

| Fichier | Rôle | Package cible |
|---|---|---|
| `SpacedRepetitionEngine.kt` | Algorithme SM-2 (calcul des intervalles) | `com.kabroedu.app.data.local.spaced` |
| `CardState.kt` | Entité Room (état d'une carte) | `com.kabroedu.app.data.local.spaced` |
| `CardStateDao.kt` | DAO Room (requêtes : cartes dues, stats) | `com.kabroedu.app.data.local.spaced` |
| `LocalDateConverter.kt` | Convertisseur LocalDate ↔ String pour Room | `com.kabroedu.app.data.local` |
| `ReviewViewModel.kt` | ViewModel pour l'écran de révision | `com.kabroedu.app.viewmodel` |

## Intégration

1. Copiez les fichiers dans les packages indiqués.
2. Dans votre `@Database`, ajoutez `CardState` aux entités et le convertisseur :
   ```kotlin
   @Database(entities = [..., CardState::class], version = X)
   @TypeConverters(LocalDateConverter::class)
   abstract class AppDatabase : RoomDatabase() {
       abstract fun cardStateDao(): CardStateDao
       // ...
   }
   ```
3. Dans le composable de flashcard, après la révélation de la réponse :
   ```kotlin
   // Afficher 4 boutons
   Row {
       Button(onClick = { viewModel.rateCard(cardId, Rating.A_REVOIR) }) { Text("À revoir") }
       Button(onClick = { viewModel.rateCard(cardId, Rating.DIFFICILE) }) { Text("Difficile") }
       Button(onClick = { viewModel.rateCard(cardId, Rating.CORRECT) }) { Text("Correct") }
       Button(onClick = { viewModel.rateCard(cardId, Rating.FACILE) }) { Text("Facile") }
   }
   ```
4. Sur l'Accueil, affichez le compteur de cartes dues :
   ```kotlin
   val dueCount by reviewViewModel.dueCount.collectAsState(initial = 0)
   Text("$dueCount cartes à réviser aujourd'hui")
   ```

## Comment ça marche

- L'élève voit une flashcard (recto) → tente de se souvenir → révèle la réponse (verso).
- Il s'auto-évalue : **À revoir** / **Difficile** / **Correct** / **Facile**.
- L'algorithme ajuste :
  - **À revoir** → on reverra la carte tout de suite (interval = 0).
  - **Difficile** → l'intervalle n'augmente pas (ou peu).
  - **Correct** → l'intervalle est multiplié par la facilité (×2.5 environ).
  - **Facile** → l'intervalle augmente plus vite (×3.25).
- Une carte est « maîtrisée » quand son intervalle dépasse 21 jours.

## Algorithme SM-2 simplifié

```
Si échec (À revoir) :
    interval = 0, repetitions = 0, ease -= 0.3
Si réussi :
    repetitions += 1
    Si 1ère réussite : interval = 1 jour
    Si 2ème réussite : interval = 3 jours
    Sinon : interval = ancien_interval × ease
    Ajuster ease selon la difficulté
```
