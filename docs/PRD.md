# PRD — Kabro Edu (préparation au Baccalauréat, Tchad)

> Document de exigences produit (Product Requirements Document).
> Version 1.0 — cible : MVP puis v2. Langue de l'app : français.

## 1. Vision

Aider efficacement et **gratuitement** les élèves tchadiens de Terminale à préparer
le baccalauréat, grâce à une application mobile qui combine :

- **l'apprentissage par étapes façon Duolingo** (parcours progressif, court, motivant) ;
- **la mémorisation durable façon Anki** (répétition espacée des définitions, formules et citations) ;
- des **exercices corrigés** et des **quiz** ;
- des **vidéos d'explication** gratuites pour chaque chapitre.

## 2. Public & contexte

- **Pays** : Tchad. **Langue** : français.
- **Séries** : **A4** (littéraire), **C** et **D** (scientifiques).
- **Contexte technique** : connexions internet souvent lentes/coûteuses et appareils
  modestes ⇒ l'app doit être **légère et fonctionner hors-ligne** après un premier chargement.

## 3. Principes non négociables

1. **100 % gratuite.**
2. **Aucune publicité.**
3. **Aucune collecte de données personnelles** — pas de compte obligatoire, pas de
   serveur de tracking. La progression est stockée **localement sur l'appareil**.
4. **Contenu hébergé sur GitHub** (dépôt `bac-contenu`, JSON en lecture seule). Les
   PDF sources restent privés (`bac-data`) et **ne sont jamais exposés** aux utilisateurs.

## 4. Fonctionnalités

### 4.1 MVP (version 1)

| # | Fonctionnalité | Description |
|---|---|---|
| F1 | **Choix de la série** | Au 1er lancement : A4, C ou D. Modifiable dans les paramètres. |
| F2 | **Accueil / tableau de bord** | Objectif du jour, série active, reprise du dernier chapitre, série de jours (streak). |
| F3 | **Liste des matières** | Matières de la série (partagées + propres), avec progression par matière. |
| F4 | **Parcours d'une matière** | Unités (chapitres) ordonnées, déblocage progressif (style Duolingo), prérequis. |
| F5 | **Étapes & activités** | Une unité = suite d'étapes (mémorisation, quiz, méthode, exercices, défi). |
| F6 | **Types d'activités** | Voir §5. Littéraire (mémorisation) + scientifique (formules, exercices corrigés). |
| F7 | **Répétition espacée** | File de révision (algorithme type SM-2/Leitner) alimentée par les cartes vues. |
| F8 | **Correction & réponse révélée** | En cas d'échec, l'app affiche la réponse finale puis la correction détaillée. |
| F9 | **Vidéos d'explication** | Vidéos recommandées par chapitre (champ `videos`), ouvertes dans YouTube. |
| F10 | **Progression locale** | XP, étapes validées, taux de réussite, streak — stockés en local (offline). |
| F11 | **Mode hors-ligne** | Contenu téléchargé et mis en cache ; utilisable sans connexion. |
| F12 | **Mises à jour du contenu** | Vérifie `contentVersion` / `version` du manifest ; retélécharge si nécessaire. |

### 4.2 Version 2 (améliorations)

- **Révision par catégorie** (ex. « réviser uniquement les citations », « uniquement les formules »).
- **Recherche** dans les matières / chapitres.
- **Statistiques** de progression (courbe de rétention, chapitres faibles).
- **Défi quotidien** mixte (mélange d'activités dues à la révision espacée).
- **Rappels/notifications locales** (objectif quotidien) — sans serveur.
- **Mode sombre**, réglages de taille de police (accessibilité).
- (Optionnel) **Assistant IA** « explique-moi » via l'API Gemini — désactivable, jamais requis.

## 5. Types d'activités (alignés sur le contenu JSON)

L'app doit savoir **rendre et évaluer** chaque type défini dans le contenu.

### 5.1 Matières littéraires (mémorisation)

| type | Interaction |
|---|---|
| `choix_unique` | QCM à une bonne réponse (`reponse` = index). |
| `choix_multiple` | QCM à plusieurs bonnes réponses (`reponses` = indices). |
| `association` | Relier des paires gauche/droite. |
| `classement` | Remettre des éléments dans l'ordre (`ordreCorrect`). |
| `texte_a_completer` | Compléter un blanc `___` (`toleranceMots`). |
| `rappel_actif` | Flashcard : révéler la réponse modèle puis auto-évaluation. |
| `reponse_courte` | Réponse ouverte courte (`reponsesAcceptees`). |
| `citation` | Attribuer/compléter une citation (mode `attribuer`). |

Champ `categorie` d'étape (philosophie notamment) : `definitions`, `citations`,
`these`, `antithese`, `exemples`, `methode` → permet la **révision ciblée**.

### 5.2 Matières scientifiques (`type: scientifique`)

| type | Interaction |
|---|---|
| `formule` | Flashcard `recto`/`verso` en répétition espacée (auto-évaluation). |
| `exercice_corrige` | Énoncé → l'élève cherche → révélation de `reponseFinale` puis `etapesCorrection`. |
| `resolution_ordonnee` | Remettre les étapes d'une résolution dans l'ordre. |

Unité scientifique = `formulaire` (deck de formules) + `etapes` (formulaire, quiz,
methode, exercices, defi) + `banqueExercices` (réservoir d'entraînement illimité).

## 6. Architecture des données

```text
manifest.json  ──►  matieresPartagees + series[].matieres[]
     │                     │
     │                     ├─ ref (matière partagée)  ou
     │                     └─ matière propre { chemin, fichiers[] }
     ▼
matiere.json (par matière)  ──►  unites[] { id, titre, ordre, fichier, prerequis[] }
     ▼
unite-XX.json  ──►  { formulaire?, etapes[], banqueExercices?, videos? }
```

- **Point d'entrée** : `manifest.json` (à `baseUrl` GitHub raw).
- **Cache & versions** : comparer `contentVersion` (global) et `version` (matière)
  aux valeurs en cache ; retélécharger uniquement ce qui a changé.
- **Identifiants stables** : `id` d'unité/étape et `competenceId` **ne changent jamais** ;
  la progression locale y est rattachée.

## 7. Répétition espacée (algorithme)

- Chaque carte de mémorisation (`formule`, `rappel_actif`, `citation`, définitions)
  possède un état local : facilité, intervalle, prochaine échéance.
- Algorithme **SM-2 simplifié / Leitner** ; l'auto-évaluation
  (À revoir / Difficile / Correct / Facile) met à jour l'intervalle.
- La file « À réviser aujourd'hui » agrège toutes les cartes dues, toutes matières confondues.

## 8. Exigences non fonctionnelles

- **Android natif** : app codée en Java/Kotlin avec Jetpack Compose + Material 3.
- **Offline-first** : après le 1er téléchargement, tout fonctionne sans réseau (Room cache).
- **Léger** : contenu JSON compact ; images/vidéos jamais embarquées (liens externes).
- **Performance** : démarrage < 2 s sur appareil modeste ; navigation fluide.
- **Confidentialité** : zéro donnée envoyée ; stockage strictement local (Room + DataStore).
- **Accessibilité** : bonne lisibilité, contrastes, taille de police réglable.
- **Robustesse** : si une vidéo est indisponible, le chapitre reste utilisable.

## 8bis. Stack technique

| Composant | Choix |
|---|---|
| Langages | **Kotlin** (principal) + **Java** |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Navigation Compose (BottomNavigation) |
| Stockage local | Room (progression, cache JSON, intervalles de révision) + DataStore (préférences) |
| Réseau | Retrofit + OkHttp + Gson |
| Mises à jour | WorkManager (vérification quotidienne de contentVersion) |
| DI | Hilt (ou injection manuelle) |
| Aucun SDK | Pas de Firebase, pas d'analytics, pas de pub |

## 9. Indicateurs de succès (sans tracking serveur)

Mesurés **localement** (affichés à l'élève, non transmis) : streak moyen, nombre de
cartes maîtrisées, taux de réussite par matière, chapitres terminés. La réussite
« business » se mesure par l'adoption et les retours utilisateurs, pas par la donnée.

## 10. Feuille de route

1. **Phase 1 (MVP)** : F1–F12 pour les 3 séries à partir du contenu existant.
2. **Phase 2** : révision par catégorie, recherche, statistiques, notifications locales.
3. **Phase 3** : polish UI (thème sombre, animations), assistant IA optionnel, publication.

## 11. Hors périmètre (pour l'instant)

- Comptes utilisateurs / synchronisation multi-appareils.
- Classements sociaux, messagerie.
- Distribution des PDF sources.
- Toute monétisation.
