# Rideit — Premium Ride-Hailing Android App

Rideit is a modern ride-hailing Android application built with **Kotlin**, **Jetpack Compose**, **Firebase**, and **Google Maps**.

The app includes separate rider and driver flows, Firebase Authentication, Firestore ride booking, driver request handling, trip tracking, trip history, driver wallet, driver documents, driver support screens, and a polished portfolio-level UI.

This project is designed as a professional portfolio app to demonstrate real-world Android development, Firebase integration, map-based ride booking, rider/driver role separation, and clean modern UI design.

---

## ✨ Features

### Rider Features

- Rider login and signup
- Role-safe Firebase authentication
- Premium rider map screen
- Pickup and dropoff search
- Ride option selection
- Ride booking with Firebase Firestore
- Active ride restore after logout/login
- Driver accepted state visibility
- Cancel ride flow
- Rating and feedback flow
- Trip receipt and completion UI
- Rider trip history
- Payment methods screen
- Notifications and promotions screen
- Settings screen
- Rider drawer navigation

### Driver Features

- Driver login and signup
- Role-safe Firebase authentication
- Driver dashboard
- Online/offline Live toggle
- Check new ride requests
- Accept or decline rider requests
- Active trip restore
- Active trip screen
- Complete/cancel trip flow
- Driver wallet and earnings summary
- Driver trip history
- Driver documents screen
- Driver support screen
- Driver drawer navigation
- Real Firebase driver name/email fallback

### Security and Professional Fixes

- Google Maps API key moved to `local.properties`
- Firestore Security Rules added and tested
- Duplicate/conflicting files removed
- Legacy routes cleaned
- Ride prices moved into a constants file
- App icon connected
- Role-safe rider/driver navigation
- Main app files audited for duplicate cleanup

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture Direction:** Clean Architecture / MVVM-inspired structure
- **Authentication:** Firebase Auth
- **Database:** Firebase Firestore
- **Maps:** Google Maps SDK + Maps Compose
- **Location:** Google Play Services Location
- **Networking:** OkHttp
- **Build System:** Gradle Kotlin DSL
- **Minimum SDK:** 24
- **Target SDK:** 35
- **Java Version:** 17

---

## 📱 Main App Screens

- Account Type Selection
- Rider Login
- Driver Login
- Rider Signup
- Driver Signup
- Rider Map Screen
- Rider Profile
- Rider Trip History
- Rider Payment
- Rider Notifications
- Rider Settings
- Driver Dashboard
- Driver Active Trip
- Driver Wallet
- Driver Trip History
- Driver Documents
- Driver Support

---

## 🧭 App Flow

### Rider Flow

```text
Account Type
→ Rider Login / Signup
→ Rider Map
→ Select pickup/dropoff
→ Choose ride option
→ Book ride
→ Driver accepts
→ Rider sees driver
→ Complete/cancel
→ Rating / receipt
→ Trip history
```

### Driver Flow

```text
Account Type
→ Driver Login / Signup
→ Driver Dashboard
→ Go Live
→ Check ride request
→ Accept / decline ride
→ Active trip
→ Complete / cancel trip
→ Wallet / history update
```
