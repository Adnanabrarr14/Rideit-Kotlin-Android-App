package com.example.rideit

object RideitFareConstants {

    const val CURRENCY_PREFIX = "Rs."

    const val MINI_ID = "1"
    const val MINI_TITLE = "Mini"
    const val MINI_SUBTITLE = "Suzuki Alto / Mira"
    const val MINI_FARE = 180
    const val MINI_TIME = "2 min"

    const val COMFORT_ID = "2"
    const val COMFORT_TITLE = "Comfort"
    const val COMFORT_SUBTITLE = "Corolla / Civic / Elantra"
    const val COMFORT_FARE = 320
    const val COMFORT_TIME = "4 min"

    const val BUSINESS_ID = "3"
    const val BUSINESS_TITLE = "Business"
    const val BUSINESS_SUBTITLE = "Fortuner / Prado / Tucson"
    const val BUSINESS_FARE = 580
    const val BUSINESS_TIME = "6 min"

    fun formatFare(amount: Int): String {
        return "$CURRENCY_PREFIX $amount"
    }
}