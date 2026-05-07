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

                val uid = user.uid

                firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val savedRole = snapshot.getString("role")

                        when {
                            savedRole == null -> {
                                createOrUpdateUserRole(
                                    uid = uid,
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

        val cleanPickup = pickupAddress.trim()
        val cleanDropoff = dropoffAddress.trim()
        val cleanRideType = rideType.trim()
        val cleanFareEstimate = fareEstimate.trim()

        if (cleanPickup.isBlank() || cleanDropoff.isBlank()) {
            onError("Pickup and dropoff are required.")
            return
        }

        if (cleanRideType.isBlank()) {
            onError("Please select a ride type.")
            return
        }

        val requestRef = firestore.collection("ride_requests").document()

        val requestData = hashMapOf(
            "requestId" to requestRef.id,
            "riderId" to currentUser.uid,
            "riderEmail" to (currentUser.email ?: ""),
            "pickupAddress" to cleanPickup,
            "dropoffAddress" to cleanDropoff,
            "rideType" to cleanRideType,
            "fareEstimate" to cleanFareEstimate,
            "status" to "requested",
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

    /*
     * Backward-compatible old functions.
     * These keep old app code safe if any older screen still calls FirebaseManager.login/signup.
     */
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