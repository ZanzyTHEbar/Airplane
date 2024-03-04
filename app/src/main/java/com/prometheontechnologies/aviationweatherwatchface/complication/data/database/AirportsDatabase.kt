package com.prometheontechnologies.aviationweatherwatchface.complication.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

const val AIRPORTS_DB_NAME = "airports"


@Entity(tableName = AIRPORTS_DB_NAME)
data class Airport(
    @PrimaryKey
    val id: Int,
    val ident: String,
    val type: String,
    val name: String,
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val elevationFt: Double?,
    val continent: String,
    val isoCountry: String,
    val isoRegion: String,
    val municipality: String?,
    val scheduledService: String,
    val gpsCode: String?,
    val iataCode: String?,
    val localCode: String?,
    val homeLink: String?,
    val wikipediaLink: String?,
    val keywords: String?
)


@Dao
abstract class AirportDAO {

    @Transaction
    @Query(
        """
    SELECT * FROM airports 
    WHERE (UPPER(ident) LIKE 'K%') 
    AND latitudeDeg BETWEEN :minLat AND :maxLat 
    AND longitudeDeg BETWEEN :minLon AND :maxLon
"""
    )
    abstract fun getAirportsByLocationsInRangeStartingWithK(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<Airport>>
}

@Database(entities = [Airport::class], version = 1)
abstract class AirportsDatabase : RoomDatabase() {

    abstract fun airportDAO(): AirportDAO

    companion object {
        private var mINSTANCE: AirportsDatabase? = null

        fun getDatabase(context: Context): AirportsDatabase {
            if (mINSTANCE == null) {
                mINSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AirportsDatabase::class.java,
                    "$AIRPORTS_DB_NAME.db"
                )
                    .createFromAsset("database/airports.sqlite")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return mINSTANCE as AirportsDatabase
        }

        @JvmStatic
        fun destroyInstance() {
            mINSTANCE = null
        }
    }
}
