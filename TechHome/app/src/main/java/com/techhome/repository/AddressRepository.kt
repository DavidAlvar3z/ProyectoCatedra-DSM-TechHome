package com.techhome.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.techhome.models.ShippingAddress

class AddressRepository {

    private val db = FirebaseFirestore.getInstance()
    private val addressesCollection = db.collection("addresses")

    companion object {
        private const val TAG = "AddressRepository"
    }

    fun saveAddress(
        address: ShippingAddress,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        addressesCollection.document(address.addressId)
            .set(address)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Dirección guardada: ${address.addressId}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al guardar dirección", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun getUserAddresses(
        userId: String,
        onSuccess: (List<ShippingAddress>) -> Unit,
        onError: (String) -> Unit
    ) {
        addressesCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val addresses = documents.mapNotNull {
                    try {
                        it.toObject(ShippingAddress::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapeando dirección", e)
                        null
                    }
                }.sortedByDescending { it.createdAt }
                Log.d(TAG, "✅ Direcciones cargadas: ${addresses.size}")
                onSuccess(addresses)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al cargar direcciones", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun getAddressById(
        addressId: String,
        onSuccess: (ShippingAddress?) -> Unit,
        onError: (String) -> Unit
    ) {
        addressesCollection.document(addressId)
            .get()
            .addOnSuccessListener { document ->
                val address = document.toObject(ShippingAddress::class.java)
                Log.d(TAG, "✅ Dirección obtenida: $addressId")
                onSuccess(address)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al obtener dirección", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun deleteAddress(
        addressId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        addressesCollection.document(addressId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Dirección eliminada: $addressId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al eliminar dirección", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun setDefaultAddress(
        userId: String,
        addressId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        addressesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()

                documents.forEach { doc ->
                    batch.update(doc.reference, "isDefault", false)
                }

                val newDefaultRef = addressesCollection.document(addressId)
                batch.update(newDefaultRef, "isDefault", true)

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Dirección default actualizada")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error al actualizar default", e)
                        onError(e.message ?: "Error desconocido")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al buscar direcciones", e)
                onError(e.message ?: "Error desconocido")
            }
    }
}