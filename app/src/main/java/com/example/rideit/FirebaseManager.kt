package com.example.rideit

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    const val ROLE_RIDER = "rider"
    const val ROLE_DRIVER = "driver"

    const val PAYMENT_CASH = "cash"
    const val PAYMENT_CARD = "card"
    const val PAYMENT_WALLET = "wallet"

    data class RiderPaymentProfile(
        val selectedPaymentMethod: String = PAYMENT_CASH,
        val cardLastFour: String = "",
        val cardHolderName: String = "",
        val walletBalance: Long = 1250L
    )

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
                    userData["riderSelectedPaymentMethod"] = PAYMENT_CASH
                    userData["riderWalletBalance"] = 1250L
                    userData["riderPaymentMode"] = "safe_demo"
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

    fun loadRiderPaymentProfile(
        onSuccess: (RiderPaymentProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to load payment methods.")
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(paymentProfileFromSnapshot(snapshot))
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load payment method")
            }
    }

    fun saveRiderPaymentProfile(
        selectedPaymentMethod: String,
        cardLastFour: String,
        cardHolderName: String,
        walletBalance: Long = 1250L,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to save payment method.")
            return
        }

        val safeMethod = when (selectedPaymentMethod.lowercase()) {
            PAYMENT_CARD -> PAYMENT_CARD
            PAYMENT_WALLET -> PAYMENT_WALLET
            else -> PAYMENT_CASH
        }

        val cleanLastFour = cardLastFour
            .filter { it.isDigit() }
            .takeLast(4)

        val cleanCardHolderName = cardHolderName
            .trim()
            .take(40)

        val paymentData = hashMapOf<String, Any>(
            "riderSelectedPaymentMethod" to safeMethod,
            "riderCardLastFour" to cleanLastFour,
            "riderCardHolderName" to cleanCardHolderName,
            "riderWalletBalance" to walletBalance.coerceAtLeast(0L),
            "riderPaymentMode" to "safe_demo",
            "riderPaymentUpdatedAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        firestore.collection("users")
            .document(currentUser.uid)
            .set(paymentData, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save payment method")
            }
    }

    fun removeRiderSavedCard(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("You must be logged in to remove card.")
            return
        }

        val paymentData = hashMapOf<String, Any>(
            "riderSelectedPaymentMethod" to PAYMENT_CASH,
            "riderCardLastFour" to "",
            "riderCardHolderName" to "",
            "riderPaymentMode" to "safe_demo",
            "riderPaymentUpdatedAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        firestore.collection("users")
            .document(currentUser.uid)
            .set(paymentData, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to remove saved card")
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

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                saveRideRequestWithPaymentProfile(
                    currentUserUid = currentUser.uid,
                    currentUserEmail = currentUser.email ?: "",
                    pickupAddress = pickupAddress,
                    dropoffAddress = dropoffAddress,
                    rideType = rideType,
                    fareEstimate = fareEstimate,
                    paymentProfile = paymentProfileFromSnapshot(userSnapshot),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
            .addOnFailureListener {
                saveRideRequestWithPaymentProfile(
                    currentUserUid = currentUser.uid,
                    currentUserEmail = currentUser.email ?: "",
                    pickupAddress = pickupAddress,
                    dropoffAddress = dropoffAddress,
                    rideType = rideType,
                    fareEstimate = fareEstimate,
                    paymentProfile = RiderPaymentProfile(),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
    }

    private fun saveRideRequestWithPaymentProfile(
        currentUserUid: String,
        currentUserEmail: String,
        pickupAddress: String,
        dropoffAddress: String,
        rideType: String,
        fareEstimate: String,
        paymentProfile: RiderPaymentProfile,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestRef = firestore.collection("ride_requests").document()

        val cleanPickup = pickupAddress.trim().ifBlank { "Selected pickup location" }
        val cleanDropoff = dropoffAddress.trim().ifBlank { "Selected dropoff location" }
        val cleanRideType = rideType.trim().ifBlank { "Rideit" }
        val cleanFareEstimate = fareEstimate.trim().ifBlank { "Fare pending" }

        val safePaymentMethod = when (paymentProfile.selectedPaymentMethod.lowercase()) {
            PAYMENT_CARD -> PAYMENT_CARD
            PAYMENT_WALLET -> PAYMENT_WALLET
            else -> PAYMENT_CASH
        }

        val paymentTitle = when (safePaymentMethod) {
            PAYMENT_CARD -> "Debit / Credit Card"
            PAYMENT_WALLET -> "Rideit Wallet"
            else -> "Cash"
        }

        val paymentStatus = when (safePaymentMethod) {
            PAYMENT_CARD -> "demo_card_selected"
            PAYMENT_WALLET -> "demo_wallet_selected"
            else -> "cash_pending"
        }

        val requestData = hashMapOf<String, Any>(
            "requestId" to requestRef.id,

            "riderId" to currentUserUid,
            "riderEmail" to currentUserEmail,

            "userId" to currentUserUid,
            "userEmail" to currentUserEmail,

            "pickupAddress" to cleanPickup,
            "dropoffAddress" to cleanDropoff,
            "pickupText" to cleanPickup,
            "dropText" to cleanDropoff,

            "rideType" to cleanRideType,
            "fareEstimate" to cleanFareEstimate,
            "fare" to cleanFareEstimate,

            "paymentMethodId" to safePaymentMethod,
            "paymentMethodTitle" to paymentTitle,
            "paymentStatus" to paymentStatus,
            "paymentMode" to "safe_demo",
            "paymentGateway" to "none",
            "paymentCaptured" to false,
            "paymentAddedAtBooking" to true,

            "status" to "pending",
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        if (safePaymentMethod == PAYMENT_CARD && paymentProfile.cardLastFour.isNotBlank()) {
            requestData["cardLastFour"] = paymentProfile.cardLastFour
        }

        if (safePaymentMethod == PAYMENT_WALLET) {
            requestData["walletBalanceAtBooking"] = paymentProfile.walletBalance
        }

        requestRef
            .set(requestData)
            .addOnSuccessListener {
                onSuccess(requestRef.id)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save ride request")
            }
    }

    private fun paymentProfileFromSnapshot(
        snapshot: DocumentSnapshot?
    ): RiderPaymentProfile {
        if (snapshot == null || !snapshot.exists()) {
            return RiderPaymentProfile()
        }

        val method = when (
            snapshot.getString("riderSelectedPaymentMethod")
                ?.trim()
                ?.lowercase()
        ) {
            PAYMENT_CARD -> PAYMENT_CARD
            PAYMENT_WALLET -> PAYMENT_WALLET
            else -> PAYMENT_CASH
        }

        val lastFour = snapshot.getString("riderCardLastFour")
            ?.filter { it.isDigit() }
            ?.takeLast(4)
            .orEmpty()

        val cardHolderName = snapshot.getString("riderCardHolderName")
            ?.trim()
            .orEmpty()

        val walletBalance = snapshot.getLong("riderWalletBalance") ?: 1250L

        return RiderPaymentProfile(
            selectedPaymentMethod = method,
            cardLastFour = lastFour,
            cardHolderName = cardHolderName,
            walletBalance = walletBalance.coerceAtLeast(0L)
        )
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

    fun currentUserDisplayName(
        fallback: String = "Rideit User"
    ): String {
        val user = auth.currentUser ?: return fallback

        val firebaseName = user.displayName
            ?.trim()
            .orEmpty()

        if (firebaseName.isNotBlank()) {
            return firebaseName
        }

        val emailPrefix = user.email
            ?.substringBefore("@")
            ?.replace(".", " ")
            ?.replace("_", " ")
            ?.replace("-", " ")
            ?.trim()
            .orEmpty()

        if (emailPrefix.isBlank()) {
            return fallback
        }

        return emailPrefix
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) {
                        char.titlecase()
                    } else {
                        char.toString()
                    }
                }
            }
            .ifBlank { fallback }
    }

    fun currentDriverDisplayName(): String {
        return currentUserDisplayName(fallback = "Rideit Driver")
    }

    fun currentRiderDisplayName(): String {
        return currentUserDisplayName(fallback = "Rideit Rider")
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