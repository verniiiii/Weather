package com.example.myweather.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "temperatures",
    foreignKeys = [ForeignKey(
        entity = City::class,
        parentColumns = ["id"],
        childColumns = ["cityId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Temperature(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityId: Int,
    val month: String,
    val temperature: Double,
    val season: String
)
