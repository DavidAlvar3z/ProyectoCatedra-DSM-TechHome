package com.techhome.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.techhome.models.PaymentMethod

class PaymentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val paymentsCollection = db.collection("payments")

    companion object {
        private const val TAG = "PaymentRepository"
    }

    fun savePayment(
        payment: PaymentMethod,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        paymentsCollection.document(payment.paymentId)
            .set(payment)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Método de pago guardado: ${payment.paymentId}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al guardar método de pago", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun getUserPayments(
        userId: String,
        onSuccess: (List<PaymentMethod>) -> Unit,
        onError: (String) -> Unit
    ) {
        paymentsCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val payments = documents.mapNotNull {
                    try {
                        it.toObject(PaymentMethod::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapeando método de pago", e)
                        null
                    }
                }.sortedByDescending { it.createdAt }
                Log.d(TAG, "✅ Métodos de pago cargados: ${payments.size}")
                onSuccess(payments)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al cargar métodos de pago", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun getPaymentById(
        paymentId: String,
        onSuccess: (PaymentMethod?) -> Unit,
        onError: (String) -> Unit
    ) {
        paymentsCollection.document(paymentId)
            .get()
            .addOnSuccessListener { document ->
                val payment = document.toObject(PaymentMethod::class.java)
                Log.d(TAG, "✅ Método de pago obtenido: $paymentId")
                onSuccess(payment)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al obtener método de pago", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun deletePayment(
        paymentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        paymentsCollection.document(paymentId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Método de pago eliminado: $paymentId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al eliminar método de pago", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun setDefaultPayment(
        userId: String,
        paymentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        paymentsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()

                documents.forEach { doc ->
                    batch.update(doc.reference, "isDefault", false)
                }

                val newDefaultRef = paymentsCollection.document(paymentId)
                batch.update(newDefaultRef, "isDefault", true)

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Método de pago default actualizado")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error al actualizar default", e)
                        onError(e.message ?: "Error desconocido")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al buscar métodos de pago", e)
                onError(e.message ?: "Error desconocido")
            }
    }
}