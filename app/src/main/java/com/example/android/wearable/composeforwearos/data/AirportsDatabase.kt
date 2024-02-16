package com.example.android.wearable.composeforwearos.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

const val DB_NAME = "airports"

@Entity(tableName = DB_NAME)
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(vararg airports: Airport)

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    abstract suspend fun insert(airport: Airport)

    @Transaction
    open suspend fun insertData(airports: List<Airport>) {
        airports.forEach {
            insert(it)
        }
    }

    @Upsert
    abstract suspend fun updateAll(vararg airports: Airport)

    @Upsert
    abstract suspend fun update(airport: Airport)

    //@Delete
    //abstract suspend fun deleteAll(vararg airports: Airport)

    @Delete
    abstract suspend fun delete(airport: Airport)

    @Query("DELETE FROM airports")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM airports WHERE latitudeDeg = :latitude AND longitudeDeg = :longitude")
    abstract suspend fun deleteByLocation(
        latitude: Double,
        longitude: Double
    )

    @Query("DELETE FROM airports WHERE name = :name")
    abstract suspend fun deleteByName(name: String)

    @Query("DELETE FROM airports WHERE id = :uid")
    abstract suspend fun deleteByUID(uid: Int)

    @Query("UPDATE airports SET name = :name WHERE iataCode = :icao")
    abstract suspend fun updateNameByICAO(icao: String, name: String)

    @Query(
        "UPDATE airports SET latitudeDeg = :latitude, longitudeDeg = :longitude WHERE iataCode = :icao"
    )
    abstract suspend fun updateLocationByICAO(icao: String, latitude: Double, longitude: Double)

    @RawQuery
    abstract suspend fun insertDataRawFormat(query: SupportSQLiteQuery): Boolean

    @Transaction
    @Query("SELECT * FROM airports")
    abstract fun getAllAirports(): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports ORDER BY name ASC")
    abstract fun getAirportOrderedByABC(): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports ORDER BY iataCode ASC")
    abstract fun getAirportsOrderedByABCICAO(): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports ORDER BY latitudeDeg ASC, longitudeDeg ASC")
    abstract fun getAirportsOrderedByLocation(): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports WHERE UPPER(ident) LIKE 'K%' ORDER BY latitudeDeg ASC, longitudeDeg ASC")
    abstract fun getAirportsStartingWithKOrderedByLocation(): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports WHERE ident = :icao")
    abstract fun getAirportByICAO(icao: String): Flow<Airport>

    @Transaction
    @Query("SELECT * FROM airports WHERE isoCountry = :countryCode")
    abstract fun getAirportsByCountryCode(countryCode: String): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports WHERE latitudeDeg BETWEEN :minLat AND :maxLat AND longitudeDeg BETWEEN :minLon AND :maxLon")
    abstract fun getAirportsByLocationsInRange(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<Airport>>

    @Transaction
    @Query("""
    SELECT * FROM airports 
    WHERE (UPPER(ident) LIKE 'K%') 
    AND latitudeDeg BETWEEN :minLat AND :maxLat 
    AND longitudeDeg BETWEEN :minLon AND :maxLon
""")
    abstract fun getAirportsByLocationsInRangeStartingWithK(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<Airport>>

    @Transaction
    @Query("SELECT * FROM airports WHERE id IN (:airportsIds)")
    abstract fun getAirportsByIds(airportsIds: IntArray): Flow<List<Airport>>

    @Query("SELECT * FROM airports WHERE name LIKE :name LIMIT 1")
    abstract fun getAirportsByName(name: String): Flow<Airport>
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
                    "$DB_NAME.db"
                )
                    .createFromAsset("database/airports.sqlite")
                    //.allowMainThreadQueries()
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

/*
*   User user = new User();
    user.setFirstName("Ajay");
    user.setLastName("Saini");
    user.setAge(25);
    // Get instance of AppDatabase
    AppDatabase db=AppDatabase.getAppDatabase()
    // Insert data into Database
    db.userDao().insertAll(user);
    // Get the list of User from the Database
    List<User> user=db.userDao().getAll();
* */
