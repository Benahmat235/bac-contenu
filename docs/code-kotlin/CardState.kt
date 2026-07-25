package com.kabroedu.app.data.local.spaced

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * État d'une carte de mémorisation (flashcard) — stocké en Room (SQLite).
 *
 * Chaque carte (identifiée par cardId = matière + unité + index)
 * possède son propre état de répétition espacée.
 */
@Entity(tableName = "card_states")
data class CardState(
    /** Identifiant unique de la carte (ex: "svt-d-cell-memo-03"). */
    @PrimaryKey
    val cardId: String = "",

    /** Facteur de facilité (commence à 2.5, ajusté par l'algorithme SM-2). */
    val ease: Float = SpacedRepetitionEngine.DEFAULT_EASE,

    /** Intervalle actuel en jours avant la prochaine révision. */
    val interval: Int = 0,

    /** Nombre de révisions réussies consécutives. */
    val repetitions: Int = 0,

    /** Date de la prochaine révision. */
    val nextReview: LocalDate = LocalDate.now()
)
