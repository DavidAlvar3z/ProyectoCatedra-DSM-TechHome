package com.techhome.models

data class ShippingAddress(
    val addressId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "El Salvador",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFullAddress(): String {
        val parts = mutableListOf<String>()
        if (addressLine1.isNotEmpty()) parts.add(addressLine1)
        if (addressLine2.isNotEmpty()) parts.add(addressLine2)
        if (city.isNotEmpty()) parts.add(city)
        if (state.isNotEmpty()) parts.add(state)
        if (zipCode.isNotEmpty()) parts.add(zipCode)
        if (country.isNotEmpty()) parts.add(country)
        return parts.joinToString(", ")
    }

    fun isComplete(): Boolean {
        return fullName.isNotEmpty() &&
                phoneNumber.isNotEmpty() &&
                addressLine1.isNotEmpty() &&
                city.isNotEmpty() &&
                state.isNotEmpty() &&
                zipCode.isNotEmpty()
    }
}