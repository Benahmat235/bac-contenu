# Prompt — Design « verre » iOS pour Kabro Edu

Ce prompt s'appuie sur le skill **`mobile-android-design`** de
[wshobson/agents](https://github.com/wshobson/agents) (Material Design 3 + Jetpack Compose),
copié pour référence dans `docs/skills/mobile-android-design/`.

Installation du skill dans un agent compatible :

```bash
npx skills add https://github.com/wshobson/agents --skill mobile-android-design
```

Le prompt complet est reproduit ci-dessous. Il combine les bonnes pratiques du skill
(thème Material 3 centralisé, `colorScheme`, cibles tactiles de 48 dp, `@Preview`,
`LazyColumn`, hissage d'état) avec l'esthétique « liquid glass » d'iOS.

Point de vigilance retenu du skill : **`dynamicColor` doit être désactivé**. La
personnalisation Material You remplacerait la palette du drapeau tchadien par les couleurs
du fond d'écran de l'élève, ce qui détruirait l'identité de Kabro Edu.


---

## Prompt à coller dans l'agent (installation du skill + refonte)

```
=========================================================
ÉTAPE 0 — INSTALLER ET CHARGER LE SKILL
=========================================================

Installe le skill de design Android puis charge-le avant toute modification :

    npx skills add https://github.com/wshobson/agents --skill mobile-android-design

Si la commande est interactive, sélectionne l'agent courant et confirme. En cas d'échec,
récupère le skill directement :

    git clone --depth 1 https://github.com/wshobson/agents.git /tmp/agents
    # puis lis :
    #   /tmp/agents/plugins/ui-design/skills/mobile-android-design/SKILL.md
    #   /tmp/agents/plugins/ui-design/skills/mobile-android-design/references/material3-theming.md
    #   /tmp/agents/plugins/ui-design/skills/mobile-android-design/references/compose-components.md
    #   /tmp/agents/plugins/ui-design/skills/mobile-android-design/references/android-navigation.md
    #   /tmp/agents/plugins/ui-design/skills/mobile-android-design/references/details.md

Une copie de référence est également versionnée dans ce dépôt sous
docs/skills/mobile-android-design/.

Confirme avoir lu le skill en résumant en trois lignes ses règles sur : le thème Material 3
centralisé, les cibles tactiles, et les pièges de recomposition. Applique ensuite l'étape 1.
```

Puis enchaîner avec le prompt de refonte (sections A à G) documenté ci-dessus.
