package com.kabroedu.app.data.local.spaced

import java.time.LocalDate

/**
 * Moteur de répétition espacée — Algorithme SM-2 adapté pour Kabro Edu.
 *
 * Principe :
 * - Chaque carte (flashcard) a un état (ease, interval, nextReview).
 * - Après chaque révision, l'élève s'auto-évalue : À_REVOIR, DIFFICILE, CORRECT, FACILE.
 * - Le moteur ajuste l'intervalle et la facilité en conséquence.
 * - Les cartes « dues » (nextReview <= aujourd'hui) sont proposées à la révision.
 *
 * Ceci est une implémentation propre, sans aucune dépendance externe ni licence contraignante.
 */
object SpacedRepetitionEngine {

    /** Facilité minimale (empêche les intervalles de s'écrouler à 0). */
    private const val MIN_EASE = 1.3f

    /** Facilité initiale d'une carte nouvellement vue. */
    const val DEFAULT_EASE = 2.5f

    /** Intervalle initial (en jours) après la première bonne réponse. */
    private const val FIRST_INTERVAL = 1 // 1 jour
    private const val SECOND_INTERVAL = 3 // 3 jours (2e bonne réponse)

    /**
     * Qualité de la réponse de l'élève (boutons affichés dans l'app).
     */
    enum class Rating(val value: Int) {
        A_REVOIR(0),   // Oublié / complètement faux → revoir tout de suite
        DIFFICILE(1),  // Se souvient avec difficulté
        CORRECT(2),    // Bonne réponse, effort normal
        FACILE(3)      // Réponse immédiate, sans effort
    }

    /**
     * Calcule le nouvel état d'une carte après une révision.
     *
     * @param current L'état actuel de la carte (ou null si c'est la première fois).
     * @param rating  La qualité de la réponse de l'élève.
     * @return Le nouvel état mis à jour.
     */
    fun review(current: CardState?, rating: Rating): CardState {
        val today = LocalDate.now()

        // Carte vue pour la première fois
        if (current == null || current.repetitions == 0) {
            return when (rating) {
                Rating.A_REVOIR -> CardState(
                    ease = DEFAULT_EASE,
                    interval = 0, // revoir immédiatement (dans la même session)
                    repetitions = 0,
                    nextReview = today
                )
                Rating.DIFFICILE -> CardState(
                    ease = DEFAULT_EASE - 0.2f,
                    interval = FIRST_INTERVAL,
                    repetitions = 1,
                    nextReview = today.plusDays(FIRST_INTERVAL.toLong())
                )
                Rating.CORRECT -> CardState(
                    ease = DEFAULT_EASE,
                    interval = FIRST_INTERVAL,
                    repetitions = 1,
                    nextReview = today.plusDays(FIRST_INTERVAL.toLong())
                )
                Rating.FACILE -> CardState(
                    ease = DEFAULT_EASE + 0.15f,
                    interval = SECOND_INTERVAL,
                    repetitions = 1,
                    nextReview = today.plusDays(SECOND_INTERVAL.toLong())
                )
            }
        }

        // Carte déjà vue — calcul de la nouvelle facilité
        val newEase = when (rating) {
            Rating.A_REVOIR -> maxOf(MIN_EASE, current.ease - 0.3f)
            Rating.DIFFICILE -> maxOf(MIN_EASE, current.ease - 0.15f)
            Rating.CORRECT -> current.ease // inchangée
            Rating.FACILE -> current.ease + 0.1f
        }

        // Calcul du nouvel intervalle
        val newInterval: Int
        val newRepetitions: Int

        if (rating == Rating.A_REVOIR) {
            // Échec → remettre à 0 (revoir aujourd'hui ou demain)
            newInterval = 0
            newRepetitions = 0
        } else {
            newRepetitions = current.repetitions + 1
            newInterval = when (newRepetitions) {
                1 -> FIRST_INTERVAL
                2 -> SECOND_INTERVAL
                else -> {
                    // Intervalle = ancien intervalle × facilité (arrondi)
                    val multiplier = when (rating) {
                        Rating.DIFFICILE -> 1.0f  // pas d'augmentation
                        Rating.CORRECT -> newEase
                        Rating.FACILE -> newEase * 1.3f
                        else -> newEase
                    }
                    maxOf(current.interval + 1, (current.interval * multiplier).toInt())
                }
            }
        }

        val nextReview = if (newInterval == 0) today else today.plusDays(newInterval.toLong())

        return CardState(
            ease = newEase,
            interval = newInterval,
            repetitions = newRepetitions,
            nextReview = nextReview
        )
    }

    /**
     * Vérifie si une carte est due pour révision aujourd'hui.
     */
    fun isDueToday(state: CardState?): Boolean {
        if (state == null) return true // jamais vue → due
        return !state.nextReview.isAfter(LocalDate.now())
    }
}
