# Algorithme de répétition espacée — sources officielles

Objectif : **ne pas réécrire l'algorithme de zéro**. On s'appuie sur des
bibliothèques et des spécifications publiques, sous licence compatible avec
Kabro Edu (gratuit, publiable sur le Play Store).

## Recommandation : FSRS via `java-fsrs` (licence MIT)

FSRS (*Free Spaced Repetition Scheduler*) est l'algorithme moderne utilisé
aujourd'hui par Anki. Il est plus précis que SM-2 et il existe une
**bibliothèque Java prête à l'emploi**, directement utilisable dans un projet
Android.

| Élément | Détail |
|---|---|
| Bibliothèque | [`java-fsrs`](https://github.com/open-spaced-repetition/java-fsrs) |
| Artefact Maven | `io.github.open-spaced-repetition:fsrs` ([Maven Central](https://central.sonatype.com/artifact/io.github.open-spaced-repetition/fsrs)) |
| Licence | **MIT** — aucune contamination, code de l'app libre de rester privé |
| Java requis | 17+ |
| Documentation | [javadoc.io](https://javadoc.io/doc/io.github.open-spaced-repetition/fsrs) |
| Variante Kotlin | [`FSRS-Kotlin`](https://github.com/open-spaced-repetition/FSRS-Kotlin) (MIT également, FSRS v6) |

### Pourquoi pas le dépôt d'Anki lui-même ?

Le code source d'Anki ([`ankitects/anki`](https://github.com/ankitects/anki))
est sous **AGPL-3.0**. L'intégrer obligerait à publier l'intégralité du code de
Kabro Edu sous AGPL. `java-fsrs` implémente le même algorithme moderne sous
licence MIT : c'est la bonne porte d'entrée.

### API de `java-fsrs` (l'essentiel)

```java
Scheduler scheduler = Scheduler.builder().build();
Card card = Card.builder().build();          // toute carte neuve est due immédiatement

// Rating.AGAIN (1) · HARD (2) · GOOD (3) · EASY (4)
CardAndReviewLog result = scheduler.reviewCard(card, Rating.GOOD);
card = result.card();
Instant due = card.getDue();                  // prochaine échéance
```

Paramètres configurables : `parameters` (21 poids du modèle),
`desiredRetention` (0.9 par défaut), `learningSteps` (1 min puis 10 min),
`relearningSteps`, `maximumInterval`, `enableFuzzing`.

`Card`, `Scheduler` et `ReviewLog` sont sérialisables en JSON
(`toJson()` / `fromJson()`) — pratique pour le stockage en base.

> Attention : `java-fsrs` travaille **en UTC uniquement**.
> L'optimisation des paramètres (apprentissage machine sur l'historique) n'est
> pas incluse ; ce n'est pas nécessaire pour Kabro Edu.

## Repli : SM-2, spécification canonique

Si l'on préfère rester sur SM-2 (plus simple, sans dépendance externe), voici
la formulation **originale de SuperMemo**, à respecter fidèlement :

```
Qualité de la réponse q : 0 à 5 (5 = parfait, 3 = correct avec effort,
                                 < 3 = échec)

1. Toute nouvelle carte : EF = 2.5
2. Intervalles :
     I(1) = 1 jour
     I(2) = 6 jours
     I(n) = arrondi( I(n-1) × EF )      pour n > 2
3. Mise à jour du facteur de facilité après chaque révision :
     EF' = EF + ( 0.1 − (5 − q) × (0.08 + (5 − q) × 0.02) )
     EF est borné : jamais inférieur à 1.3
4. Si q < 3 : on recommence les répétitions à I(1),
   MAIS on conserve l'EF calculé.
```

Points où mon implémentation initiale s'écartait de la vraie SM-2, et qu'il
faut corriger :

| Point | Implémentation initiale (à corriger) | SM-2 officielle |
|---|---|---|
| 2ᵉ intervalle | 3 jours | **6 jours** |
| Mise à jour de EF | ajustements arbitraires (−0.3 / −0.15 / +0.1) | **formule exacte** ci-dessus |
| Échelle de notation | 4 boutons mappés à 0-3 | q de **0 à 5** (à mapper explicitement) |
| Après un échec | intervalle remis à 0 | **retour à I(1) = 1 jour** |

### Correspondance des 4 boutons de l'interface

L'interface de Kabro Edu propose 4 boutons ; il faut les mapper vers l'échelle
attendue :

| Bouton | SM-2 (q) | FSRS (`Rating`) |
|---|---|---|
| À revoir | 0 | `AGAIN` |
| Difficile | 3 | `HARD` |
| Correct | 4 | `GOOD` |
| Facile | 5 | `EASY` |

## Sources

- [SuperMemo — Application of a computer to improve the results obtained in working with the SuperMemo method](https://www.supermemo.com/en/blog/application-of-a-computer-to-improve-the-results-obtained-in-working-with-the-supermemo-method) — article d'origine décrivant SM-2 et l'E-Factor (variation de 1.1 à 2.5, valeur initiale 2.5).
- [SuperMemo Guru — E-Factor](https://supermemo.guru/wiki/E-Factor) — définition de l'E-Factor dans SM-2.
- [Wikipédia — SuperMemo](https://en.wikipedia.org/wiki/SuperMemo) — historique des versions (SM-0 à SM-2, 1987).
- [`cnnrhill/sm-2`](https://github.com/cnnrhill/sm-2) — explication pédagogique de SM-2 avec implémentation de référence.
- [FSRS — The Algorithm (wiki officiel)](https://github.com/open-spaced-repetition/fsrs4anki/wiki/The-Algorithm) — modèle DSR (Difficulté, Stabilité, Récupérabilité).
- [`open-spaced-repetition/java-fsrs`](https://github.com/open-spaced-repetition/java-fsrs) — bibliothèque Java, MIT.
- [`open-spaced-repetition/FSRS-Kotlin`](https://github.com/open-spaced-repetition/FSRS-Kotlin) — implémentation Kotlin de FSRS v6, MIT.
- [Explication technique de FSRS](https://expertium.github.io/Algorithm.html) — détail des équations.

*Contenu reformulé pour respecter les licences des sources.*
