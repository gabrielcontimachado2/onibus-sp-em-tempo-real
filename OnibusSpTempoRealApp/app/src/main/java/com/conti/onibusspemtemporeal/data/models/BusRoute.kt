package com.conti.onibusspemtemporeal.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity(
    tableName = "bus_route_favorite",
    indices = [Index(
        value = ["lineCod"],
        unique = true
    )]
)
data class BusRoute(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("cl")
    val lineCod: Int,
    @SerializedName("lc")
    val circularMode: Boolean,
    @SerializedName("lt")
    val firstNumbersPlacard: String,
    @SerializedName("tl")
    val secondPartPlacard: Int,
    @SerializedName("sl")
    val lineWay: Int,
    @SerializedName("tp")
    val mainTerminal: String,
    @SerializedName("ts")
    val secondTerminal: String
) : Serializable