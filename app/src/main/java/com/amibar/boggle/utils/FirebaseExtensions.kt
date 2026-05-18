package com.amibar.boggle.utils

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Returns a [Flow] that emits [DataSnapshot]s whenever the value at this [Query] changes.
 */
fun Query.valueFlow(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            trySend(snapshot)
        }
        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

/**
 * Suspends until a single value is retrieved from this [Query].
 */
suspend fun Query.awaitValue(): DataSnapshot = suspendCancellableCoroutine { continuation ->
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            continuation.resume(snapshot)
        }
        override fun onCancelled(error: DatabaseError) {
            continuation.resumeWithException(error.toException())
        }
    }
    addListenerForSingleValueEvent(listener)
    continuation.invokeOnCancellation { removeEventListener(listener) }
}

/**
 * Returns a [Flow] that emits [DataSnapshot]s whenever a child is removed from this [Query].
 */
fun Query.childRemovedFlow(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {
            trySend(snapshot)
        }
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    addChildEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

/**
 * Helper to add a [ChildEventListener] with optional lambdas for each event.
 */
@Suppress("unused")
inline fun DatabaseReference.addChildEventsListener(
    crossinline onChildAdded: (snapshot: DataSnapshot, previousChildName: String?) -> Unit = { _, _ -> },
    crossinline onChildChanged: (snapshot: DataSnapshot, previousChildName: String?) -> Unit = { _, _ -> },
    crossinline onChildRemoved: (snapshot: DataSnapshot) -> Unit = { _ -> },
    crossinline onChildMoved: (snapshot: DataSnapshot, previousChildName: String?) -> Unit = { _, _ -> },
    crossinline onCancelled: (error: DatabaseError) -> Unit = { _ -> }
): ChildEventListener {
    val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) = onChildAdded(snapshot, previousChildName)
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = onChildChanged(snapshot, previousChildName)
        override fun onChildRemoved(snapshot: DataSnapshot) = onChildRemoved(snapshot)
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = onChildMoved(snapshot, previousChildName)
        override fun onCancelled(error: DatabaseError) = onCancelled(error)
    }
    addChildEventListener(listener)
    return listener
}
