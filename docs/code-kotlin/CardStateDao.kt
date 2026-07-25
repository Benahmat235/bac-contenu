package com.kabroedu.app.data.local.spaced

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO (Data Access Object) pour les états de cartes — Room.
 * Fournit les requêtes nécessaires au système de répétition espacée.
 */
@Dao
interface CardStateDao {

    /** Récupère l'état d'une carte par son ID. */
    @Query("SELECT * FROM card_states WHERE cardId = :cardId")
    suspend fun getCardState(cardId: String): CardState?

    /** Récupère toutes les cartes dues pour aujourd'hui (nextReview <= aujourd'hui). */
    @Query("SELECT * FROM card_states WHERE nextReview <= :today ORDER BY nextReview ASC")
    fun getCardsDueToday(today: LocalDate = LocalDate.now()): Flow<List<CardState>>

    /** Nombre de cartes dues aujourd'hui (pour l'Accueil : "12 cartes à revoir"). */
    @Query("SELECT COUNT(*) FROM card_states WHERE nextReview <= :today")
    fun countDueToday(today: LocalDate = LocalDate.now()): Flow<Int>

    /** Récupère les cartes dues pour une matière spécifique. */
    @Query("SELECT * FROM card_states WHERE cardId LIKE :matierePrefix || '%' AND nextReview <= :today")
    fun getCardsDueForMatiere(matierePrefix: String, today: LocalDate = LocalDate.now()): Flow<List<CardState>>

    /** Nombre total de cartes maîtrisées (interval >= 21 jours = "connue"). */
    @Query("SELECT COUNT(*) FROM card_states WHERE interval >= 21")
    fun countMastered(): Flow<Int>

    /** Nombre total de cartes vues au moins une fois. */
    @Query("SELECT COUNT(*) FROM card_states WHERE repetitions > 0")
    fun countSeen(): Flow<Int>

    /** Insère ou met à jour l'état d'une carte. */
    @Upsert
    suspend fun upsertCardState(state: CardState)

    /** Insère ou met à jour plusieurs cartes. */
    @Upsert
    suspend fun upsertAll(states: List<CardState>)

    /** Supprime toutes les données (reset). */
    @Query("DELETE FROM card_states")
    suspend fun clearAll()
}
