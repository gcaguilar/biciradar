package com.gcaguilar.biciradar.appfunctions.results

import android.os.Parcel
import android.os.Parcelable

/**
 * Result data for a detailed station status query.
 */
data class StationStatusResult(
    val stationId: String,
    val name: String,
    val address: String,
    val bikesAvailable: Int,
    val slotsAvailable: Int,
    val isOpen: Boolean,
    val lastUpdated: Long,
    val isFavorite: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(stationId)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeInt(bikesAvailable)
        parcel.writeInt(slotsAvailable)
        parcel.writeByte(if (isOpen) 1 else 0)
        parcel.writeLong(lastUpdated)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<StationStatusResult> {
        override fun createFromParcel(parcel: Parcel): StationStatusResult {
            return StationStatusResult(parcel)
        }

        override fun newArray(size: Int): Array<StationStatusResult?> {
            return arrayOfNulls(size)
        }
    }
}