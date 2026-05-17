package com.example.rideit

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class RideitUserNotification(
    val id: String = "",
    val icon: String = "!",
    val title: String = "",
    val message: String = "",
    val type: String = "Ride",
    val rideRequestId: String = "",
    val eventKey: String = "",
    val createdAt: Timestamp? = null,
    val isRead: Boolean = false
) {
    val unread: Boolean
        get() = !isRead

    val timeText: String
        get() = formatNotificationTime(createdAt)

    companion object {
        const val TYPE_RIDE = "Ride"
    }
}

object RideitNotificationCenter {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    const val TYPE_RIDE = "Ride"
    const val TYPE_DRIVER = "Driver"
    const val TYPE_TRIP = "Trip"
    const val TYPE_CANCELLATION = "Cancellation"

    fun listenToCurrentUserNotifications(
        limit: Long = 50,
        onChange: (List<RideitUserNotification>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val currentUser = auth.currentUser ?: return null

        return firestore.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError("Unable to load notifications. Please try again.")
                    return@addSnapshotListener
                }

                val notifications = snapshots
                    ?.documents
                    .orEmpty()
                    .map { document -> document.toRideitNotification() }

                onChange(notifications)
            }
    }

    fun listenToCurrentUserUnreadCount(
        onChange: (Int) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val currentUser = auth.currentUser ?: return null

        return firestore.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .limit(100)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError("Unable to load notifications. Please try again.")
                    return@addSnapshotListener
                }

                onChange(snapshots?.size() ?: 0)
            }
    }

    fun listenToCurrentUserRideAlertsEnabled(
        onChange: (Boolean) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration? {
        val currentUser = auth.currentUser ?: return null

        return firestore.collection("users")
            .document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError("Unable to load notification settings. Please try again.")
                    return@addSnapshotListener
                }

                onChange(snapshot?.getBoolean("rideAlertsEnabled") ?: true)
            }
    }

    fun markCurrentUserNotificationsRead(
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("Unable to update notifications. Please try again.")
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .limit(50)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.documents.isEmpty()) {
                    onComplete()
                    return@addOnSuccessListener
                }

                val readAt = Timestamp.now()
                val batch = firestore.batch()

                snapshots.documents.forEach { document ->
                    batch.set(
                        document.reference,
                        mapOf(
                            "isRead" to true,
                            "readAt" to readAt,
                            "updatedAt" to readAt
                        ),
                        SetOptions.merge()
                    )
                }

                batch.commit()
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener {
                        onError("Unable to update notifications. Please try again.")
                    }
            }
            .addOnFailureListener {
                onError("Unable to update notifications. Please try again.")
            }
    }

    fun notifyRideRequestCreated(
        rideRequestId: String,
        riderId: String,
        pickupAddress: String,
        dropoffAddress: String,
        rideType: String
    ) {
        writeNotification(
            NotificationPayload(
                recipientId = riderId,
                recipientRole = FirebaseManager.ROLE_RIDER,
                type = TYPE_RIDE,
                icon = "R",
                title = "Finding your driver",
                message = "Your ${rideType.cleanRideLabel()} request from ${pickupAddress.cleanPlace()} to ${dropoffAddress.cleanPlace()} is waiting for a driver.",
                rideRequestId = rideRequestId,
                eventKey = "rider_request_created"
            )
        )
    }

    fun notifyNewDriverRequest(
        driverId: String,
        rideRequestId: String
    ) {
        withRideRequest(rideRequestId) { document ->
            writeNotification(
                NotificationPayload(
                    recipientId = driverId,
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_DRIVER,
                    icon = "N",
                    title = "New ride request",
                    message = "Pickup: ${document.pickupAddress()}. Dropoff: ${document.dropoffAddress()}. Fare: ${document.fareText()}.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_new_request"
                )
            )
        }
    }

    fun notifyDriverAccepted(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            val riderId = document.riderId()
            val driverId = document.driverId()
            val driverName = document.driverName()
            val riderLabel = document.riderLabel()

            writeNotification(
                NotificationPayload(
                    recipientId = riderId,
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_RIDE,
                    icon = "D",
                    title = "Driver accepted your ride",
                    message = "$driverName accepted your ${document.rideType()} ride.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_driver_accepted"
                )
            )

            writeNotification(
                NotificationPayload(
                    recipientId = driverId,
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_DRIVER,
                    icon = "A",
                    title = "Ride accepted",
                    message = "You accepted $riderLabel's ride from ${document.pickupAddress()}.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_accepted_confirmation"
                )
            )
        }
    }

    fun notifyRideDeclined(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            writeNotification(
                NotificationPayload(
                    recipientId = document.riderId(),
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_CANCELLATION,
                    icon = "!",
                    title = "Ride request declined",
                    message = "This driver declined the request. Please book another ride.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_request_declined"
                )
            )
        }
    }

    fun notifyDriverArrived(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            val driverName = document.driverName()

            writeNotification(
                NotificationPayload(
                    recipientId = document.riderId(),
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_RIDE,
                    icon = "P",
                    title = "Driver arrived at pickup",
                    message = "$driverName is at ${document.pickupAddress()}.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_driver_arrived"
                )
            )

            writeNotification(
                NotificationPayload(
                    recipientId = document.driverId(),
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_DRIVER,
                    icon = "P",
                    title = "Pickup arrival updated",
                    message = "The rider was notified that you arrived at pickup.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_arrived_confirmation"
                )
            )
        }
    }

    fun notifyRideStarted(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            writeNotification(
                NotificationPayload(
                    recipientId = document.riderId(),
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_TRIP,
                    icon = "S",
                    title = "Trip started",
                    message = "Your ride to ${document.dropoffAddress()} is now in progress.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_trip_started"
                )
            )

            writeNotification(
                NotificationPayload(
                    recipientId = document.driverId(),
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_TRIP,
                    icon = "S",
                    title = "Trip started",
                    message = "Trip to ${document.dropoffAddress()} is now in progress.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_trip_started"
                )
            )
        }
    }

    fun notifyRideCompleted(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            writeNotification(
                NotificationPayload(
                    recipientId = document.riderId(),
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_TRIP,
                    icon = "C",
                    title = "Trip completed",
                    message = "Your trip to ${document.dropoffAddress()} is complete. Please rate your driver and view receipt.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_trip_completed"
                )
            )

            writeNotification(
                NotificationPayload(
                    recipientId = document.driverId(),
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_TRIP,
                    icon = "C",
                    title = "Trip completed",
                    message = "Earning ${document.fareText()} has been recorded for this trip.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_trip_completed"
                )
            )
        }
    }

    fun notifyRideCancelledByRider(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            writeNotification(
                NotificationPayload(
                    recipientId = document.riderId(),
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_CANCELLATION,
                    icon = "X",
                    title = "Ride cancelled",
                    message = "Your ride request was cancelled.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_cancelled_confirmation"
                )
            )

            writeNotification(
                NotificationPayload(
                    recipientId = document.driverId(),
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_CANCELLATION,
                    icon = "X",
                    title = "Rider cancelled trip",
                    message = "The rider cancelled the trip from ${document.pickupAddress()}.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_rider_cancelled"
                )
            )
        }
    }

    fun notifyRideCancelledByDriver(rideRequestId: String) {
        withRideRequest(rideRequestId) { document ->
            val driverName = document.driverName()

            writeNotification(
                NotificationPayload(
                    recipientId = document.riderId(),
                    recipientRole = FirebaseManager.ROLE_RIDER,
                    type = TYPE_CANCELLATION,
                    icon = "X",
                    title = "Driver cancelled the trip",
                    message = "$driverName cancelled this trip. You can book another ride.",
                    rideRequestId = rideRequestId,
                    eventKey = "rider_driver_cancelled"
                )
            )

            writeNotification(
                NotificationPayload(
                    recipientId = document.driverId(),
                    recipientRole = FirebaseManager.ROLE_DRIVER,
                    type = TYPE_CANCELLATION,
                    icon = "X",
                    title = "Trip cancelled",
                    message = "You cancelled this trip.",
                    rideRequestId = rideRequestId,
                    eventKey = "driver_cancelled_confirmation"
                )
            )
        }
    }

    private fun withRideRequest(
        rideRequestId: String,
        onLoaded: (DocumentSnapshot) -> Unit
    ) {
        if (rideRequestId.isBlank()) return

        firestore.collection("ride_requests")
            .document(rideRequestId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onLoaded(document)
                }
            }
    }

    private fun writeNotification(
        payload: NotificationPayload
    ) {
        if (payload.recipientId.isBlank() || payload.rideRequestId.isBlank()) return

        val notificationId = notificationDocumentId(
            rideRequestId = payload.rideRequestId,
            eventKey = payload.eventKey
        )

        val notificationRef = firestore.collection("users")
            .document(payload.recipientId)
            .collection("notifications")
            .document(notificationId)

        notificationRef.get()
            .addOnSuccessListener { existing ->
                if (existing.exists()) return@addOnSuccessListener

                val now = Timestamp.now()
                val notificationData = mapOf(
                    "id" to notificationId,
                    "recipientId" to payload.recipientId,
                    "recipientRole" to payload.recipientRole,
                    "type" to payload.type,
                    "icon" to payload.icon,
                    "title" to payload.title,
                    "message" to payload.message,
                    "rideRequestId" to payload.rideRequestId,
                    "eventKey" to payload.eventKey,
                    "createdAt" to now,
                    "updatedAt" to now,
                    "isRead" to false
                )

                notificationRef.set(notificationData, SetOptions.merge())
            }
    }

    private fun notificationDocumentId(
        rideRequestId: String,
        eventKey: String
    ): String {
        return "${rideRequestId}_${eventKey}"
            .replace("/", "_")
            .replace("\\", "_")
            .take(140)
    }

    private data class NotificationPayload(
        val recipientId: String,
        val recipientRole: String,
        val type: String,
        val icon: String,
        val title: String,
        val message: String,
        val rideRequestId: String,
        val eventKey: String
    )
}

private fun DocumentSnapshot.toRideitNotification(): RideitUserNotification {
    return RideitUserNotification(
        id = id,
        icon = getString("icon").orEmpty().ifBlank { "!" },
        title = getString("title").orEmpty(),
        message = getString("message").orEmpty(),
        type = getString("type").orEmpty().ifBlank { RideitNotificationCenter.TYPE_RIDE },
        rideRequestId = getString("rideRequestId").orEmpty(),
        eventKey = getString("eventKey").orEmpty(),
        createdAt = getTimestamp("createdAt"),
        isRead = getBoolean("isRead") ?: false
    )
}

private fun DocumentSnapshot.riderId(): String {
    return text("riderId")
        .ifBlank { text("userId") }
}

private fun DocumentSnapshot.driverId(): String {
    return text("driverId")
        .ifBlank { text("acceptedDriverId") }
        .ifBlank { text("completedByDriverId") }
        .ifBlank { text("cancelledByDriverId") }
}

private fun DocumentSnapshot.driverName(): String {
    return text("driverName")
        .ifBlank { text("acceptedDriverName") }
        .ifBlank { "Your driver" }
}

private fun DocumentSnapshot.riderLabel(): String {
    return text("riderName")
        .ifBlank { text("riderEmail") }
        .ifBlank { text("userEmail") }
        .ifBlank { "the rider" }
}

private fun DocumentSnapshot.pickupAddress(): String {
    return text("pickupAddress")
        .ifBlank { text("pickupText") }
        .ifBlank { text("pickup") }
        .ifBlank { text("from") }
        .cleanPlace()
}

private fun DocumentSnapshot.dropoffAddress(): String {
    return text("dropoffAddress")
        .ifBlank { text("dropText") }
        .ifBlank { text("dropoffText") }
        .ifBlank { text("dropoff") }
        .ifBlank { text("destination") }
        .ifBlank { text("to") }
        .cleanPlace()
}

private fun DocumentSnapshot.rideType(): String {
    return text("rideType")
        .ifBlank { text("selectedRideType") }
        .ifBlank { "Rideit" }
        .cleanRideLabel()
}

private fun DocumentSnapshot.fareText(): String {
    return text("driverEarningText")
        .ifBlank { text("fareEstimate") }
        .ifBlank { text("fare") }
        .ifBlank { text("estimatedFare") }
        .ifBlank { "fare pending" }
}

private fun DocumentSnapshot.text(field: String): String {
    return try {
        val value = get(field)
        when (value) {
            null -> ""
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> value.toString()
        }
    } catch (_: Exception) {
        ""
    }
}

private fun String.cleanPlace(): String {
    return trim()
        .replace(Regex("\\s+"), " ")
        .ifBlank { "selected location" }
}

private fun String.cleanRideLabel(): String {
    return trim()
        .replace(Regex("\\s+"), " ")
        .ifBlank { "Rideit" }
}

private fun formatNotificationTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"

    val createdAtMillis = timestamp.toDate().time
    val diffMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        minutes < 1L -> "Just now"
        minutes == 1L -> "1 min ago"
        minutes < 60L -> "$minutes min ago"
        hours == 1L -> "1 hour ago"
        hours < 24L -> "$hours hours ago"
        days == 1L -> "Yesterday"
        days < 7L -> "$days days ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(timestamp.toDate())
    }
}
