package com.gcaguilar.biciradar.appfunctions.results

import android.os.Parcel
import android.os.Parcelable

/**
 * Result data for favorites list query.
 */
data class FavoritesListResult(
    val favorites: List<StationResult>,
    val homeStation: StationResult?,
    val workStation: StationResult?,
    val totalCount: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(StationResult.CREATOR) ?: emptyList(),
        parcel.readParcelable(StationResult::class.java.classLoader),
        parcel.readParcelable(StationResult::class.java.classLoader),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(favorites)
        parcel.writeParcelable(homeStation, flags)
        parcel.writeParcelable(workStation, flags)
        parcel.writeInt(totalCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FavoritesListResult> {
        override fun createFromParcel(parcel: Parcel): FavoritesListResult {
            return FavoritesListResult(parcel)
        }

        override fun newArray(size: Int): Array<FavoritesListResult?> {
            return arrayOfNulls(size)
        }
    }
}