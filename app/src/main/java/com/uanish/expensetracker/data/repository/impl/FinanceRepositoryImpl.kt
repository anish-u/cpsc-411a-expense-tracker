package com.uanish.expensetracker.data.repository.impl

import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.data.model.Transaction
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.data.remote.FirestorePaths
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.data.repository.TxFilter
import com.uanish.expensetracker.util.AppResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FinanceRepositoryImpl(
    private val db: FirebaseFirestore
) : FinanceRepository {

    override fun observeCategories(uid: String): Flow<List<Category>> = callbackFlow {
        val ref = db.collection(FirestorePaths.categories(uid))
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val reg = ref.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err) // <-- surface the error to the Flow
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(Category::class.java)?.copy(id = d.id)
            }.orEmpty()
            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    override fun observeTransactions(uid: String, filter: TxFilter): Flow<List<Transaction>> = callbackFlow {
        var q: Query = db.collection(FirestorePaths.transactions(uid))

        // Sorting
        q = q.orderBy("dateEpochMillis", if (filter.newestFirst) Query.Direction.DESCENDING else Query.Direction.ASCENDING)

        // Filtering (Firestore supports equality filters; compound needs index sometimes)
        filter.type?.let { q = q.whereEqualTo("type", it.name) }
        filter.categoryId?.let { q = q.whereEqualTo("categoryId", it) }
        filter.limit?.let { q = q.limit(it) }
        filter.startEpochMillis?.let { start ->
            q = q.whereGreaterThanOrEqualTo("dateEpochMillis", start)
        }
        filter.endEpochMillis?.let { end ->
            q = q.whereLessThanOrEqualTo("dateEpochMillis", end)
        }

        val reg = q.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err) // <-- surface the error to the Flow
                return@addSnapshotListener
            }
            var list = snap?.documents?.mapNotNull { d ->
                val raw = d.data ?: return@mapNotNull null
                val typeStr = raw["type"] as? String ?: TxType.EXPENSE.name
                Transaction(
                    id = d.id,
                    amount = (raw["amount"] as? Number)?.toDouble() ?: 0.0,
                    type = runCatching { TxType.valueOf(typeStr) }.getOrDefault(TxType.EXPENSE),
                    categoryId = raw["categoryId"] as? String ?: "",
                    note = raw["note"] as? String ?: "",
                    dateEpochMillis = (raw["dateEpochMillis"] as? Number)?.toLong() ?: 0L,
                    createdAt = (raw["createdAt"] as? Number)?.toLong() ?: 0L,
                )
            }.orEmpty()

            // Client-side search (note contains). Firestore "contains" needs different modeling.
            filter.searchNote?.takeIf { it.isNotBlank() }?.let { term ->
                val t = term.trim().lowercase()
                list = list.filter { it.note.lowercase().contains(t) }
            }

            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    override suspend fun upsertCategory(uid: String, category: Category): AppResult<Unit> = try {
        val col = db.collection(FirestorePaths.categories(uid))
        val doc = if (category.id.isBlank()) col.document() else col.document(category.id)
        doc.set(category.copy(id = "")).await() // don't store id field; doc id is id
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Failed to save category.")
    }

    override suspend fun deleteCategory(uid: String, categoryId: String): AppResult<Unit> = try {
        db.collection(FirestorePaths.categories(uid)).document(categoryId).delete().await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Failed to delete category.")
    }

    override suspend fun upsertTransaction(uid: String, tx: Transaction): AppResult<Unit> = try {
        val col = db.collection(FirestorePaths.transactions(uid))
        val doc = if (tx.id.isBlank()) col.document() else col.document(tx.id)

        val payload = mapOf(
            "amount" to tx.amount,
            "type" to tx.type.name,
            "categoryId" to tx.categoryId,
            "note" to tx.note,
            "dateEpochMillis" to tx.dateEpochMillis,
            "createdAt" to tx.createdAt
        )

        doc.set(payload).await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Failed to save transaction.")
    }

    override suspend fun deleteTransaction(uid: String, txId: String): AppResult<Unit> = try {
        db.collection(FirestorePaths.transactions(uid)).document(txId).delete().await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Failed to delete transaction.")
    }

    override suspend fun getTransaction(uid: String, txId: String): AppResult<Transaction> = try {
        val doc = db.collection(com.uanish.expensetracker.data.remote.FirestorePaths.transactions(uid))
            .document(txId).get().await()

        val raw = doc.data ?: return AppResult.Error("Transaction not found.")
        val typeStr = raw["type"] as? String ?: com.uanish.expensetracker.data.model.TxType.EXPENSE.name
        val tx = com.uanish.expensetracker.data.model.Transaction(
            id = doc.id,
            amount = (raw["amount"] as? Number)?.toDouble() ?: 0.0,
            type = runCatching { com.uanish.expensetracker.data.model.TxType.valueOf(typeStr) }
                .getOrDefault(com.uanish.expensetracker.data.model.TxType.EXPENSE),
            categoryId = raw["categoryId"] as? String ?: "",
            note = raw["note"] as? String ?: "",
            dateEpochMillis = (raw["dateEpochMillis"] as? Number)?.toLong() ?: 0L,
            createdAt = (raw["createdAt"] as? Number)?.toLong() ?: 0L,
        )
        AppResult.Success(tx)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Failed to load transaction.")
    }

    override suspend fun getCategory(uid: String, categoryId: String): AppResult<com.uanish.expensetracker.data.model.Category> = try {
        val doc = db.collection(com.uanish.expensetracker.data.remote.FirestorePaths.categories(uid))
            .document(categoryId).get().await()

        val obj = doc.toObject(com.uanish.expensetracker.data.model.Category::class.java)
            ?: return AppResult.Error("Category not found.")
        AppResult.Success(obj.copy(id = doc.id))
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Failed to load category.")
    }

}
