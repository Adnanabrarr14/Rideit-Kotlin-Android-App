package com.example.rideit

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale
import java.util.concurrent.TimeUnit

object FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    const val ROLE_RIDER = "rider"
    const val ROLE_DRIVER = "driver"

    const val PAYMENT_CASH = "cash"
    const val PAYMENT_CARD = "card"
    const val PAYMENT_WALLET = "wallet"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_ROSE = "rose"

    const val GENDER_WOMAN = "woman"
    const val GENDER_MAN = "man"
    const val GENDER_PREFER_NOT_TO_SAY = "prefer_not_to_say"
    const val GENDER_OTHER = "other"

    data class RiderPaymentProfile(
        val selectedPaymentMethod: String = PAYMENT_CASH,
        val cardLastFour: String = "",
        val cardHolderName: String = "",
        val walletBalance: Long = 1250L
    )

    data class RideitUserProfile(
        val fullName: String = "",
        val email: String = "",
        val phoneNumber: String = "",
        val role: String = ROLE_RIDER,
        val gender: String = GENDER_PREFER_NOT_TO_SAY,
        val preferredThemeMode: String = THEME_SYSTEM
    )

    fun login(
        email: String,
        password: String,
        expectedRole: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            onError("Please enter email and password.")
            return
        }

        auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
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
                                    fullName = user.displayName.orEmpty(),
                                    role = expectedRole,
                                    gender = null,
                                    preferredThemeMode = null,
                                    phoneNumber = user.phoneNumber.orEmpty(),
                                    onSuccess = onSuccess,
                                    onError = {
                                        auth.signOut()
                                        onError(it)
                                    }
                                )
                            }

                            savedRole == expectedRole -> {
                                val savedName = snapshot.getString("fullName")
                                    ?: snapshot.getString("displayName")
                                    ?: snapshot.getString("name")
                                    ?: ""

                                if (savedName.isNotBlank() && user.displayName != savedName) {
                                    updateAuthDisplayNameOnly(
                                        fullName = savedName,
                                        onComplete = onSuccess
                                    )
                                } else {
                                    onSuccess()
                                }
                            }

                            else -> {
                                auth.signOut()

                                val correctAccountText = if (savedRole == ROLE_DRIVER) {
                                    "Driver Login"
                                } else {
                                    "Rider Login"
                                }

                                onError(
                                    "This account is registered as ${savedRole.uppercase()}. Please use $correctAccountText."
                                )
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        auth.signOut()
                        onError(exception.message ?: "Failed to check user role.")
                    }
            }
            .addOnFailureListener {
                onError("Wrong email or password. Please check your account details and try again.")
            }
    }

    fun signup(
        fullName: String,
        email: String,
        password: String,
        role: String,
        gender: String = GENDER_PREFER_NOT_TO_SAY,
        preferredThemeMode: String = THEME_SYSTEM,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()
        val cleanName = sanitizeFullName(fullName)

        if (cleanName.length < 2) {
            onError("Please enter your real full name.")
            return
        }

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            onError("Please enter email and password.")
            return
        }

        if (cleanPassword.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
            .addOnSuccessListener { authResult ->
                val user = authResult.user

                if (user == null) {
                    auth.signOut()
                    onError("Signup failed. User account not created.")
                    return@addOnSuccessListener
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(cleanName)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener {
                        createOrUpdateUserRole(
                            uid = user.uid,
                            email = cleanEmail,
                            fullName = cleanName,
                            role = role,
                            gender = gender,
                            preferredThemeMode = preferredThemeMode,
                            phoneNumber = user.phoneNumber.orEmpty(),
                            onSuccess = onSuccess,
                            onError = {
                                auth.signOut()
                                onError(it)
                            }
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Signup failed.")
            }
    }

    fun sendPhoneLoginOtp(
        activity: Activity,
        phoneNumber: String,
        expectedRole: String,
        onCodeSent: (String) -> Unit,
        onAutoVerified: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanPhone = sanitizePhoneNumber(phoneNumber)

        if (!isValidInternationalPhone(cleanPhone)) {
            onError("Enter phone number with country code, example +923001234567.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(
                    credential = credential,
                    expectedRole = expectedRole,
                    phoneNumber = cleanPhone,
                    onSuccess = onAutoVerified,
                    onError = onError
                )
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                onError(exception.message ?: "Phone verification failed.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(cleanPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneLoginOtp(
        verificationId: String,
        otpCode: String,
        expectedRole: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanOtp = otpCode.trim()
        val cleanPhone = sanitizePhoneNumber(phoneNumber)

        if (verificationId.isBlank()) {
            onError("Please request OTP first.")
            return
        }

        if (cleanOtp.length < 6) {
            onError("Enter the 6-digit OTP code.")
            return
        }

        val credential = PhoneAuthProvider.getCredential(
            verificationId,
            cleanOtp
        )

        signInWithPhoneCredential(
            credential = credential,
            expectedRole = expectedRole,
            phoneNumber = cleanPhone,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        expectedRole: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user

                if (user == null) {
                    auth.signOut()
                    onError("Phone login failed. User account not found.")
                    return@addOnSuccessListener
                }

                val cleanPhone = sanitizePhoneNumber(
                    user.phoneNumber ?: phoneNumber
                )

                firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val savedRole = snapshot.getString("role")

                        when {
                            savedRole == null -> {
                                createOrUpdateUserRole(
                                    uid = user.uid,
                                    email = user.email.orEmpty(),
                                    fullName = user.displayName.orEmpty().ifBlank {
                                        if (expectedRole == ROLE_DRIVER) "Rideit Driver" else "Rideit Rider"
                                    },
                                    role = expectedRole,
                                    gender = null,
                                    preferredThemeMode = null,
                                    phoneNumber = cleanPhone,
                                    onSuccess = onSuccess,
                                    onError = {
                                        auth.signOut()
                                        onError(it)
                                    }
                                )
                            }

                            savedRole == expectedRole -> {
                                val phoneData = hashMapOf<String, Any>(
                                    "phoneNumber" to cleanPhone,
                                    "phoneVerified" to true,
                                    "updatedAt" to Timestamp.now()
                                )

                                firestore.collection("users")
                                    .document(user.uid)
                                    .set(phoneData, SetOptions.merge())
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { exception ->
                                        onError(exception.message ?: "Failed to update phone profile.")
                                    }
                            }

                            else -> {
                                auth.signOut()

                                val correctAccountText = if (savedRole == ROLE_DRIVER) {
                                    "Driver Login"
                                } else {
                                    "Rider Login"
                                }

                                onError(
                                    "This phone number is registered as ${savedRole.uppercase()}. Please use $correctAccountText."
                                )
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        auth.signOut()
                        onError(exception.message ?: "Failed to check phone account role.")
                    }
            }
            .addOnFailureListener {
                onError("Invalid OTP or phone login failed.")
            }
    }

    private fun createOrUpdateUserRole(
        uid: String,
        email: String,
        fullName: String,
        role: String,
        gender: String?,
        preferredThemeMode: String?,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userDocument = firestore.collection("users").document(uid)

        userDocument
            .get()
            .addOnSuccessListener { snapshot ->
                val safeGender = sanitizeGender(gender)
                val safeTheme = sanitizeThemeMode(preferredThemeMode)
                val safeName = sanitizeFullName(fullName)
                val safePhone = sanitizePhoneNumber(phoneNumber)

                val userData = hashMapOf<String, Any>(
                    "uid" to uid,
                    "email" to email.trim().lowercase(),
                    "role" to role,
                    "updatedAt" to Timestamp.now()
                )

                if (safeName.isNotBlank()) {
                    userData["fullName"] = safeName
                    userData["displayName"] = safeName
                    userData["name"] = safeName
                }

                if (safePhone.isNotBlank()) {
                    userData["phoneNumber"] = safePhone
                    userData["phoneVerified"] = true
                }

                if (gender != null) {
                    userData["gender"] = safeGender
                }

                if (preferredThemeMode != null) {
                    userData["preferredThemeMode"] = safeTheme
                }

                if (!snapshot.exists()) {
                    userData["createdAt"] = Timestamp.now()
                    userData["riderSelectedPaymentMethod"] = PAYMENT_CASH
                    userData["riderWalletBalance"] = 1250L
                    userData["riderPaymentMode"] = "safe_demo"
                    userData["preferredLanguageCode"] = "en"
                    userData["preferredCurrencyCode"] = "PKR"
                    userData["preferredThemeMode"] = safeTheme
                    userData["gender"] = safeGender
                    userData["phoneNumber"] = safePhone
                }

                userDocument
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to save user profile.")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to check user profile.")
            }
    }

    fun loadCurrentUserProfile(
        onSuccess: (RideitUserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("Please login again to load profile.")
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val fullName = snapshot.getString("fullName")
                    ?: snapshot.getString("displayName")
                    ?: snapshot.getString("name")
                    ?: currentUser.displayName
                    ?: currentUserDisplayName("Rideit User")

                onSuccess(
                    RideitUserProfile(
                        fullName = sanitizeFullName(fullName),
                        email = snapshot.getString("email") ?: currentUser.email.orEmpty(),
                        phoneNumber = snapshot.getString("phoneNumber")
                            ?: currentUser.phoneNumber.orEmpty(),
                        role = snapshot.getString("role") ?: ROLE_RIDER,
                        gender = snapshot.getString("gender") ?: GENDER_PREFER_NOT_TO_SAY,
                        preferredThemeMode = snapshot.getString("preferredThemeMode") ?: THEME_SYSTEM
                    )
                )
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load profile.")
            }
    }

    fun updateCurrentUserProfile(
        fullName: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("Please login again to update profile.")
            return
        }

        val safeName = sanitizeFullName(fullName)
        val safePhone = phoneNumber.trim().take(24)

        if (safeName.length < 2) {
            onError("Please enter a valid full name.")
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(safeName)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnSuccessListener {
                val profileData = hashMapOf<String, Any>(
                    "fullName" to safeName,
                    "displayName" to safeName,
                    "name" to safeName,
                    "phoneNumber" to safePhone,
                    "updatedAt" to Timestamp.now()
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(profileData, SetOptions.merge())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to save profile.")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to update account name.")
            }
    }

    private fun updateAuthDisplayNameOnly(
        fullName: String,
        onComplete: () -> Unit
    ) {
        val currentUser = auth.currentUser ?: run {
            onComplete()
            return
        }

        val safeName = sanitizeFullName(fullName)

        if (safeName.isBlank()) {
            onComplete()
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(safeName)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { onComplete() }
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
                onError(exception.message ?: "Failed to load payment method.")
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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save payment method.")
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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to remove saved card.")
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
        val safeFareAmount = extractRideitFareAmount(cleanFareEstimate)

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
            "fareAmount" to safeFareAmount,
            "currencyCode" to "PKR",
            "currencySymbol" to "Rs",
            "paymentMethodId" to safePaymentMethod,
            "paymentMethodTitle" to paymentTitle,
            "paymentStatus" to paymentStatus,
            "paymentMode" to "safe_demo",
            "paymentGateway" to "none",
            "paymentCaptured" to false,
            "paymentAddedAtBooking" to true,
            "driverEarningAmount" to 0,
            "driverEarningText" to "Rs 0",
            "driverWalletStatus" to "not_completed",
            "feedbackSubmitted" to false,
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
            .addOnSuccessListener { onSuccess(requestRef.id) }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save ride request.")
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
                val activeOnlyStatuses = setOf(
                    "accepted",
                    "driver_arriving",
                    "ride_started"
                )

                val document = snapshots.documents
                    .filter { doc ->
                        val status = doc.getString("status").orEmpty().trim().lowercase()
                        status in activeOnlyStatuses
                    }
                    .maxByOrNull { doc ->
                        doc.getTimestamp("updatedAt")?.seconds
                            ?: doc.getTimestamp("acceptedAt")?.seconds
                            ?: doc.getTimestamp("createdAt")?.seconds
                            ?: 0L
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
                onError(exception.message ?: "Failed to restore active ride.")
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
                    "driverWalletStatus" to "cancelled",
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to cancel ride request.")
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

        val rideDocumentRef = firestore.collection("ride_requests")
            .document(requestId)

        rideDocumentRef
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onError("Ride request not found.")
                    return@addOnSuccessListener
                }

                val fareSource = snapshot.getString("fareEstimate")
                    ?: snapshot.getString("fare")
                    ?: snapshot.getString("estimatedFare")
                    ?: snapshot.getString("driverEarningText")
                    ?: ""

                val savedFareAmount = snapshot.getLong("fareAmount")?.toInt()
                    ?: snapshot.getDouble("fareAmount")?.toInt()
                    ?: 0

                val safeEarningAmount = when {
                    snapshot.getLong("driverEarningAmount") != null &&
                            (snapshot.getLong("driverEarningAmount") ?: 0L) > 0L ->
                        snapshot.getLong("driverEarningAmount")!!.toInt()

                    snapshot.getDouble("driverEarningAmount") != null &&
                            (snapshot.getDouble("driverEarningAmount") ?: 0.0) > 0.0 ->
                        snapshot.getDouble("driverEarningAmount")!!.toInt()

                    savedFareAmount > 0 ->
                        savedFareAmount

                    else ->
                        extractRideitFareAmount(fareSource)
                }.coerceAtLeast(0)

                val safeEarningText = formatRideitRupees(safeEarningAmount)
                val completedNow = Timestamp.now()

                rideDocumentRef
                    .set(
                        mapOf(
                            "status" to "completed",
                            "completedBy" to "driver",
                            "completedByDriverId" to currentUser.uid,
                            "completedByDriverEmail" to (currentUser.email ?: ""),
                            "driverId" to currentUser.uid,
                            "driverEmail" to (currentUser.email ?: ""),
                            "driverName" to currentUserDisplayName("Rideit Driver"),
                            "fareAmount" to safeEarningAmount,
                            "fare" to safeEarningText,
                            "fareEstimate" to safeEarningText,
                            "driverEarningAmount" to safeEarningAmount,
                            "driverEarningText" to safeEarningText,
                            "driverWalletStatus" to "earned",
                            "driverPayoutStatus" to "pending_demo_payout",
                            "driverEarningCurrencyCode" to "PKR",
                            "driverEarningCurrencySymbol" to "Rs",
                            "driverEarningRecordedAt" to completedNow,
                            "feedbackSubmitted" to false,
                            "completedAt" to completedNow,
                            "updatedAt" to completedNow
                        ),
                        SetOptions.merge()
                    )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Failed to complete trip.")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load trip fare before completion.")
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
                    "driverWalletStatus" to "cancelled",
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to cancel trip.")
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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save feedback.")
            }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()

        if (cleanEmail.isBlank()) {
            onError("Enter your email first.")
            return
        }

        auth.sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError("Could not send reset email. Make sure this account exists.")
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

        if (emailPrefix.isNotBlank()) {
            return emailPrefix
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    word.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }
                }
                .ifBlank { fallback }
        }

        val phone = user.phoneNumber.orEmpty()

        if (phone.isNotBlank()) {
            return phone
        }

        return fallback
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
            fullName = email.substringBefore("@"),
            email = email,
            password = password,
            role = ROLE_RIDER,
            gender = GENDER_PREFER_NOT_TO_SAY,
            preferredThemeMode = THEME_SYSTEM,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun sanitizeFullName(
        fullName: String?
    ): String {
        return fullName
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.take(40)
            .orEmpty()
    }

    private fun sanitizePhoneNumber(
        phoneNumber: String?
    ): String {
        return phoneNumber
            ?.trim()
            ?.replace(" ", "")
            ?.replace("-", "")
            ?.replace("(", "")
            ?.replace(")", "")
            .orEmpty()
    }

    private fun isValidInternationalPhone(
        phoneNumber: String
    ): Boolean {
        return phoneNumber.startsWith("+") &&
                phoneNumber.length in 10..16 &&
                phoneNumber.drop(1).all { it.isDigit() }
    }

    private fun sanitizeGender(
        gender: String?
    ): String {
        return when (gender?.trim()?.lowercase()) {
            GENDER_WOMAN -> GENDER_WOMAN
            GENDER_MAN -> GENDER_MAN
            GENDER_OTHER -> GENDER_OTHER
            else -> GENDER_PREFER_NOT_TO_SAY
        }
    }

    private fun sanitizeThemeMode(
        themeMode: String?
    ): String {
        return when (themeMode?.trim()?.lowercase()) {
            THEME_LIGHT -> THEME_LIGHT
            THEME_DARK -> THEME_DARK
            THEME_ROSE -> THEME_ROSE
            else -> THEME_SYSTEM
        }
    }

    private fun extractRideitFareAmount(
        fareText: String?
    ): Int {
        val cleanText = fareText
            ?.trim()
            .orEmpty()

        if (cleanText.isBlank()) {
            return 0
        }

        val digitsOnly = cleanText.filter { it.isDigit() }

        if (digitsOnly.isBlank()) {
            return 0
        }

        return digitsOnly.toIntOrNull() ?: 0
    }

    private fun formatRideitRupees(
        amount: Int
    ): String {
        if (amount <= 0) {
            return "Rs 0"
        }

        return "Rs ${String.format(Locale.US, "%,d", amount)}"
    }
}