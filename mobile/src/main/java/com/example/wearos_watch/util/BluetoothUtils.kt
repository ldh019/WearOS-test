package com.example.wearos_watch.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.example.wearos_watch.Constants.Companion.CHARACTERISTIC_INFO_STRING
import com.example.wearos_watch.Constants.Companion.CHARACTERISTIC_STATUS_STRING
import com.example.wearos_watch.Constants.Companion.CHARACTERISTIC_X_STRING
import com.example.wearos_watch.Constants.Companion.CHARACTERISTIC_Y_STRING
import com.example.wearos_watch.Constants.Companion.CHARACTERISTIC_Z_STRING
import com.example.wearos_watch.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.example.wearos_watch.Constants.Companion.SERVICE_STRING
import java.util.*


class BluetoothUtils {
    companion object {
        fun findStatusCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, CHARACTERISTIC_STATUS_STRING)?.apply {
                getDescriptor()
            }
        }

        fun findInfoCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, CHARACTERISTIC_INFO_STRING)?.apply {
                addDescriptor(
                    getDescriptor()
                )
            }
        }

        fun findXValueCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, CHARACTERISTIC_X_STRING)?.apply {
                addDescriptor(
                    getDescriptor()
                )
            }
        }

        fun findYValueCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, CHARACTERISTIC_Y_STRING)?.apply {
                addDescriptor(
                    getDescriptor()
                )
            }
        }

        fun findZValueCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, CHARACTERISTIC_Z_STRING)?.apply {
                addDescriptor(
                    getDescriptor()
                )
            }
        }

        fun getDescriptor(): BluetoothGattDescriptor {
            return BluetoothGattDescriptor(
                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG),
                BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ
            ).apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
        }

        /**
         * Find the given uuid characteristic
         * @param gatt gatt instance
         * @param uuidString uuid to query as string
         */
        private fun findCharacteristic(
            gatt: BluetoothGatt,
            uuidString: String
        ): BluetoothGattCharacteristic? {
            val serviceList = gatt.services
            val service = findGattService(serviceList) ?: return null
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                if (matchCharacteristic(characteristic, uuidString)) {
                    return characteristic
                }
            }
            return null
        }

        /**
         * Match the given characteristic and a uuid string
         * @param characteristic one of found characteristic provided by the server
         * @param uuidString uuid as string to match
         * @return true if matched
         */
        private fun matchCharacteristic(
            characteristic: BluetoothGattCharacteristic?,
            uuidString: String
        ): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return matchUUIDs(uuid.toString(), uuidString)
        }

        /**
         * Find Gatt service that matches with the server's service
         * @param serviceList list of services
         * @return matched service if found
         */
        private fun findGattService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
            for (service in serviceList) {
                val serviceUuidString = service.uuid.toString()
                if (matchServiceUUIDString(serviceUuidString)) {
                    return service
                }
            }
            return null
        }

        fun findDescriptor(characteristic: BluetoothGattCharacteristic?): BluetoothGattDescriptor? {
            return characteristic?.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
        }

        /**
         * Try to match the given uuid with the service uuid
         * @param serviceUuidString service UUID as string
         * @return true if service uuid is matched
         */
        private fun matchServiceUUIDString(serviceUuidString: String): Boolean {
            return matchUUIDs(serviceUuidString, SERVICE_STRING)
        }

        /**
         * Try to match a uuid with the given set of uuid
         * @param uuidString uuid to query
         * @param matches a set of uuid
         * @return true if matched
         */
        private fun matchUUIDs(uuidString: String, vararg matches: String): Boolean {
            for (match in matches) {
                if (uuidString.equals(match, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}