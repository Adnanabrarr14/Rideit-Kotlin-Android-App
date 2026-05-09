package com.example.rideit

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    const val ROLE_RIDER = "rider"
    const val ROLE_DRIVER = "driver"

    fun login(
        email: String,
        password: String,
        expectedRole: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()

        auth.signInWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user

                if (user == null) {
                    auth.signOut()
                    onError("Login failed. User account not found.")
                    return@addOnSuccessListener
                }

                firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val savedRole = snapshot.getString("role")

                        when {
                            savedRole == null -> {
                                createOrUpdateUserRole(
                                    uid = user.uid,
                                    email = cleanEmail,
                                    role = expectedRole,
                                    onSuccess = onSuccess,
                                    onError = {
                                        auth.signOut()
                                        onError(it)
                                    }
                                )
                            }

                            savedRole == expectedRole -> {
                                onSuccess()
                            }

                            else -> {
                                auth.signOut()

                                val correctAccountText = if (savedRole == ROLE_DRIVER) {
                                    "Driver Login"
                                } else {
                                    "Rider Login"
                                }

                                onError(
                                    "This email is registered as a ${savedRole.uppercase()} account. Please use $correctAccountText."
                                )
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        auth.signOut()
                        onError(exception.message ?: "Failed to check user role")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Login failed")
            }
    }

    fun signup(
        email: String,
        password: String,
        role: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()

        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user

                if (user == null) {
                    auth.signOut()
                    onError("Signup failed. User account not created.")
                    return@addOnSuccessListener
                }

                createOrUpdateUserRole(
                    uid = user.uid,
                    email = cleanEmail,
                    role = role,
                    onSuccess = onSuccess,
                    onError = {
                        auth.signOut()
                        onError(it)
                    }
                )
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Signup failed")
            }
    }

    private fun createOrUpdateUserRole(
        uid: String,
        email: String,
        role: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userDocument = firestore.collection("users").document(uid)

        userDocument
            .get()
            .addOnSuccessListener { snapshot ->
                val userData = hashMapOf<String, Any>(
                    "uid" to uid,
                    "email" to email,
                    "role" to role,
                    "updatedAt" to Timestamp.now()
                )

                if (!snapshot.exists()) {
                    userData["createdAt"] = Timestamp.now()
                }

                userDocument
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to save user role")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to check user profile")
            }
    }

    fun saveRideRequest(
        pickupAddress: String,
        dropoffAddress: String,
        rideType: String,
        fareEstimate: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to book a ride.")
            return
        }

        val requestRef = firestore.collection("ride_requests").document()

        val cleanPickup = pickupAddress.trim().ifBlank { "Selected pickup location" }
        val cleanDropoff = dropoffAddress.trim().ifBlank { "Selected dropoff location" }
        val cleanRideType = rideType.trim().ifBlank { "Rideit" }
        val cleanFareEstimate = fareEstimate.trim().ifBlank { "Fare pending" }

        val requestData = hashMapOf(
            "requestId" to requestRef.id,

            "riderId" to currentUser.uid,
            "riderEmail" to (currentUser.email ?: ""),

            "userId" to currentUser.uid,
            "userEmail" to (currentUser.email ?: ""),

            "pickupAddress" to cleanPickup,
            "dropoffAddress" to cleanDropoff,
            "pickupText" to cleanPickup,
            "dropText" to cleanDropoff,

            "rideType" to cleanRideType,
            "fareEstimate" to cleanFareEstimate,
            "fare" to cleanFareEstimate,

            "status" to "pending",
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        requestRef
            .set(requestData)
            .addOnSuccessListener {
                onSuccess(requestRef.id)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save ride request")
            }
    }

    fun findLatestRestorableRiderRide(
        onSuccess: (
            requestId: String?,
            status: String?,
            driverName: String?,
            driverEmail: String?,
            feedbackSubmitted: Boolean
        ) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onSuccess(null, null, null, null, false)
            return
        }

        firestore.collection("ride_requests")
            .whereEqualTo("riderId", currentUser.uid)
            .limit(25)
            .get()
            .addOnSuccessListener { snapshots ->
                val restorableStatuses = setOf(
                    "pending",
                    "requested",
                    "accepted",
                    "completed"
                )

                val document = snapshots.documents
                    .filter { doc ->
                        val status = doc.getString("status").orEmpty().lowercase()
                        val feedbackSubmitted = doc.getBoolean("feedbackSubmitted") ?: false

                        status in restorableStatuses &&
                                !(status == "completed" && feedbackSubmitted)
                    }
                    .maxByOrNull { doc ->
                        doc.getTimestamp("createdAt")?.seconds ?: 0L
                    }

                if (document == null) {
                    onSuccess(null, null, null, null, false)
                    return@addOnSuccessListener
                }

                onSuccess(
                    document.id,
                    document.getString("status"),
                    document.getString("driverName"),
                    document.getString("driverEmail"),
                    document.getBoolean("feedbackSubmitted") ?: false
                )
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to restore active ride")
            }
    }

    fun cancelRideRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to cancel a ride.")
            return
        }

        if (requestId.isBlank()) {
            onError("Ride request not found.")
            return
        }

        firestore.collection("ride_requests")
            .document(requestId)
            .update(
                mapOf(
                    "status" to "cancelled_by_rider",
                    "cancelledBy" to "rider",
                    "cancelledByUserId" to currentUser.uid,
                    "cancelledByUserEmail" to (currentUser.email ?: ""),
                    "cancelledAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to cancel ride request")
            }
    }

    fun completeDriverTrip(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in as driver to complete the trip.")
            return
        }

        if (requestId.isBlank()) {
            onError("Ride request not found.")
            return
        }

        firestore.collection("ride_requests")
            .document(requestId)
            .update(
                mapOf(
                    "status" to "completed",
                    "completedBy" to "driver",
                    "completedByDriverId" to currentUser.uid,
                    "completedByDriverEmail" to (currentUser.email ?: ""),
                    "completedAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to complete trip")
            }
    }

    fun cancelDriverTrip(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in as driver to cancel the trip.")
            return
        }

        if (requestId.isBlank()) {
            onError("Ride request not found.")
            return
        }

        firestore.collection("ride_requests")
            .document(requestId)
            .update(
                mapOf(
                    "status" to "cancelled_by_driver",
                    "cancelledBy" to "driver",
                    "cancelledByDriverId" to currentUser.uid,
                    "cancelledByDriverEmail" to (currentUser.email ?: ""),
                    "cancelledAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to cancel trip")
            }
    }

    fun saveRiderTripFeedback(
        requestId: String,
        rating: Int,
        tags: List<String>,
        feedback: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to submit feedback.")
            return
        }

        if (requestId.isBlank()) {
            onError("Completed ride request not found.")
            return
        }

        val safeRating = rating.coerceIn(1, 5)

        val cleanTags = tags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val cleanFeedback = feedback.trim()

        firestore.collection("ride_requests")
            .document(requestId)
            .set(
                mapOf(
                    "riderRating" to safeRating,
                    "riderFeedbackTags" to cleanTags,
                    "riderFeedback" to cleanFeedback,
                    "feedbackSubmitted" to true,
                    "feedbackSubmittedBy" to "rider",
                    "feedbackSubmittedByUserId" to currentUser.uid,
                    "feedbackSubmittedByUserEmail" to (currentUser.email ?: ""),
                    "feedbackSubmittedAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save feedback")
            }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Password reset failed")
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun currentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        login(
            email = email,
            password = password,
            expectedRole = ROLE_RIDER,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun signup(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        signup(
            email = email,
            password = password,
            role = ROLE_RIDER,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}