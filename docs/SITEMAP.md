# Sitemap & navigation — Kabro Edu

Application mobile (navigation par onglets), offline-first, contenu chargé depuis GitHub.

## 1. Arborescence des écrans

```text
Lancement
└─ Onboarding (1er lancement uniquement)
   ├─ Écran de bienvenue (vision, gratuit / sans pub / sans compte)
   ├─ Choix de la série  ──►  A4 · C · D
   └─ Premier téléchargement du contenu (avec barre de progression)

Application (barre d'onglets)
├─ 🏠 Accueil (tableau de bord)
│   ├─ Objectif du jour + streak (jours consécutifs)
│   ├─ « Continuer » (reprise du dernier chapitre)
│   ├─ « À réviser aujourd'hui » (file de répétition espacée)  ──►  Session de révision
│   └─ Accès rapide aux matières
│
├─ 📚 Matières
│   └─ Liste des matières de la série (progression par matière)
│       └─ Parcours de la matière (unités ordonnées, style « chemin »)
│           ├─ Unité verrouillée (prérequis non atteints)
│           └─ Unité ouverte  ──►  Détail de l'unité
│               ├─ Liste des étapes (formulaire · quiz · méthode · exercices · défi)
│               ├─ 🎥 Vidéos d'explication du chapitre (ouvre YouTube)
│               ├─ 📄 Formulaire (deck de formules — matières scientifiques)
│               └─ Bouton « Démarrer / Continuer »  ──►  Lecteur d'étape
│                   └─ Lecteur d'activité (une activité à la fois)
│                       ├─ Rendu selon le type (QCM, flashcard, association, exercice…)
│                       ├─ Validation → feedback immédiat
│                       ├─ En cas d'échec : réponse révélée + correction
│                       └─ Fin d'étape  ──►  Écran de résultats (score, XP, réussite)
│
├─ 🔁 Réviser
│   ├─ « À réviser aujourd'hui » (toutes matières)
│   ├─ Révision par matière
│   └─ (v2) Révision par catégorie : définitions · citations · thèse/antithèse · formules · méthode
│
├─ 📈 Progression
│   ├─ Streak, XP total, cartes maîtrisées
│   ├─ Progression par matière (barres)
│   └─ (v2) Chapitres à renforcer
│
└─ ⚙️ Paramètres
    ├─ Changer de série
    ├─ Gérer le contenu (vérifier les mises à jour, télécharger, vider le cache)
    ├─ Objectif quotidien & rappels (notifications locales)
    ├─ Apparence (thème clair/sombre, taille de police)
    └─ À propos (gratuit, sans pub, sans collecte de données ; crédits vidéos)
```

## 2. Parcours utilisateur clés

### 2.1 Première utilisation
Bienvenue → choix de la série → téléchargement du contenu → Accueil.

### 2.2 Apprendre un chapitre
Accueil/Matières → matière → unité ouverte → étape → activités successives →
résultats → l'étape suivante se débloque ; les cartes vues entrent dans la file de révision.

### 2.3 Réviser (répétition espacée)
Accueil « À réviser aujourd'hui » (ou onglet Réviser) → session de cartes dues →
auto-évaluation → mise à jour des intervalles.

### 2.4 Regarder une vidéo
Détail d'unité → section Vidéos → ouverture du lien YouTube (playlist ou vidéo).

### 2.5 Mettre à jour le contenu
Paramètres → « Vérifier les mises à jour » → comparaison `contentVersion`/`version` →
téléchargement des matières modifiées uniquement.

## 3. États & composants transverses

- **États vides** : « pas de révision aujourd'hui », « chapitre non commencé ».
- **Hors-ligne** : bannière discrète ; tout le contenu déjà téléchargé reste accessible.
- **Erreurs réseau** : messages clairs, bouton « réessayer », dégradation gracieuse.
- **Verrouillage** : unités bloquées tant que les `prerequis` ne sont pas validés.
- **Feedback** : correct/incorrect, animations légères, gains d'XP, entretien du streak.

## 4. Modèle de navigation

- **Barre d'onglets** persistante : Accueil · Matières · Réviser · Progression · Paramètres.
- Navigation en **pile** à l'intérieur de chaque onglet (matière → unité → étape → activité).
- Le **lecteur d'activité** est plein écran et focalisé (une activité à la fois, façon Duolingo).
