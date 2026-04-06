package com.gcaguilar.biciradar.appfunctions.results

import android.os.Parcel
import android.os.Parcelable

/**
 * Result data for a station query.
 */
data class StationResult(
    val stationId: String,
    val name: String,
    val bikesAvailable: Int,
    val slotsAvailable: Int,
    val distance: Double,
    val isFavorite: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(stationId)
        parcel.writeString(name)
        parcel.writeInt(bikesAvailable)
        parcel.writeInt(slotsAvailable)
        parcel.writeDouble(distance)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<StationResult> {
        override fun createFromParcel(parcel: Parcel): StationResult {
            return StationResult(parcel)
        }

        override fun newArray(size: Int): Array<StationResult?> {
            return arrayOfNulls(size)
        }
    }
}