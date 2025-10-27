package com.techhome.models

enum class PaymentType {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    CASH_ON_DELIVERY
}

data class PaymentMethod(
    val paymentId: String = "",
    val userId: String = "",
    val type: PaymentType = PaymentType.CREDIT_CARD,
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cvv: String = "",
    val paypalEmail: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDisplayName(): String {
        return when (type) {
            PaymentType.CREDIT_CARD -> "Tarjeta de Crédito •••• ${getLastFourDigits()}"
            PaymentType.DEBIT_CARD -> "Tarjeta de Débito •••• ${getLastFourDigits()}"
            PaymentType.PAYPAL -> "PayPal - $paypalEmail"
            PaymentType.CASH_ON_DELIVERY -> "Pago contra entrega"
        }
    }

    fun getLastFourDigits(): String {
        return if (cardNumber.length >= 4) {
            cardNumber.takeLast(4)
        } else {
            cardNumber
        }
    }

    fun getMaskedCardNumber(): String {
        return if (cardNumber.length >= 4) {
            "•••• •••• •••• ${getLastFourDigits()}"
        } else {
            cardNumber
        }
    }

    fun isComplete(): Boolean {
        return when (type) {
            PaymentType.CREDIT_CARD, PaymentType.DEBIT_CARD -> {
                cardHolderName.isNotEmpty() &&
                        cardNumber.length == 16 &&
                        expiryMonth.isNotEmpty() &&
                        expiryYear.isNotEmpty() &&
                        cvv.length in 3..4
            }
            PaymentType.PAYPAL -> paypalEmail.isNotEmpty()
            PaymentType.CASH_ON_DELIVERY -> true
        }
    }
}