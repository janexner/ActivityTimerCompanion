package com.exner.tools.activitytimercompanion.data.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDataDAO {

    @Query("SELECT * FROM timerprocess ORDER BY name ASC")
    fun observeProcessesAlphabeticallyOrdered(): Flow<List<TimerProcess>>

    @Query("SELECT * FROM timerprocess ORDER BY name ASC")
    suspend fun getAllProcesses(): List<TimerProcess>

    @Query("SELECT * FROM timerprocess WHERE uuid=:uuid")
    suspend fun getTimerProcessByUuid(uuid: String): TimerProcess?

    @Query("SELECT * FROM timerprocesscategory ORDER BY name ASC")
    fun observeCategoriesAlphabeticallyOrdered(): Flow<List<TimerProcessCategory>>

    @Query("SELECT * FROM timercategoryidnamecount")
    fun observeCategoryUsageCount(): Flow<List<TimerCategoryIdNameCount>>

    @Query("SELECT * FROM timercategoryidnamecount WHERE uid=:id")
    suspend fun getCategoryUsageCountForId(id: Long): TimerCategoryIdNameCount?

    @Query("SELECT uuid FROM timerprocess WHERE goto_uuid=:uuid ORDER BY name ASC")
    suspend fun getUuidsOfDependantProcesses(uuid: String): List<String>

    @Query("SELECT * FROM timerprocesscategory WHERE uid=:id")
    suspend fun getCategoryById(id: Long): TimerProcessCategory?

    @Query("SELECT tp.* FROM timerprocess tp, timerprocesscategory tc " +
            "WHERE tc.name = :categoryName " +
            "AND tp.category_id = tc.uid")
    suspend fun getProcessesByCategory(categoryName: String): List<TimerProcess>

    @Query("DELETE FROM timerprocesscategory WHERE uid IN (:listOfIds)")
    suspend fun deleteCategoriesByIdsFromList(listOfIds: List<Long>)

    @Insert
    suspend fun insert(process: TimerProcess)

    @Update
    suspend fun updateProcess(process: TimerProcess)

    @Delete
    suspend fun delete(process: TimerProcess)

    @Insert
    suspend fun insertCategory(category: TimerProcessCategory)

    @Update
    suspend fun updateCategory(category: TimerProcessCategory)

    @Delete
    suspend fun deleteCategory(category: TimerProcessCategory)
}