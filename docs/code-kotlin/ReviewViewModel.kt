package com.kabroedu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabroedu.app.data.local.spaced.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de révision.
 * Gère la file de cartes dues et la logique de notation.
 *
 * Usage dans le Composable :
 *   val dueCards by viewModel.dueCards.collectAsState(emptyList())
 *   val dueCount by viewModel.dueCount.collectAsState(0)
 */
class ReviewViewModel(
    private val cardStateDao: CardStateDao
) : ViewModel() {

    /** Nombre de cartes dues aujourd'hui (affiché sur l'Accueil). */
    val dueCount: Flow<Int> = cardStateDao.countDueToday()

    /** Liste des cartes dues aujourd'hui. */
    val dueCards: Flow<List<CardState>> = cardStateDao.getCardsDueToday()

    /** Nombre de cartes maîtrisées (interval >= 21j). */
    val masteredCount: Flow<Int> = cardStateDao.countMastered()

    /**
     * L'élève a évalué une carte. Met à jour son état via SM-2.
     *
     * @param cardId L'identifiant de la carte (ex: "svt-d-cell-memo-03").
     * @param rating La note donnée par l'élève.
     */
    fun rateCard(cardId: String, rating: SpacedRepetitionEngine.Rating) {
        viewModelScope.launch {
            val current = cardStateDao.getCardState(cardId)
            val newState = SpacedRepetitionEngine.review(current, rating)
            cardStateDao.upsertCardState(newState.copy(cardId = cardId))
        }
    }

    /**
     * Enregistre une carte comme « vue pour la première fois » (après mémorisation).
     * Appelé quand l'élève voit une flashcard pour la première fois dans une leçon.
     */
    fun markFirstSeen(cardId: String) {
        viewModelScope.launch {
            val existing = cardStateDao.getCardState(cardId)
            if (existing == null) {
                // Nouvelle carte → prochaine révision demain
                val state = SpacedRepetitionEngine.review(null, SpacedRepetitionEngine.Rating.CORRECT)
                cardStateDao.upsertCardState(state.copy(cardId = cardId))
            }
        }
    }

    /** Réinitialise toute la progression (paramètres → reset). */
    fun resetAll() {
        viewModelScope.launch { cardStateDao.clearAll() }
    }
}
