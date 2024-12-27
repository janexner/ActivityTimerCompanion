package com.exner.tools.activitytimercompanion.data.persistence

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TimerDataRepository @Inject constructor(private val meditationTimerProcessDAO: TimerDataDAO) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val observeProcesses: Flow<List<TimerProcess>> =
        meditationTimerProcessDAO.observeProcessesAlphabeticallyOrdered()

    val observeCategories: Flow<List<TimerProcessCategory>> =
        meditationTimerProcessDAO.observeCategoriesAlphabeticallyOrdered()

    val observeCategoryUsageCount: Flow<List<TimerCategoryIdNameCount>> =
        meditationTimerProcessDAO.observeCategoryUsageCount()

    @WorkerThread
    suspend fun loadProcessByUuid(uuid: String): TimerProcess? {
        return meditationTimerProcessDAO.getTimerProcessByUuid(uuid)
    }

    @WorkerThread
    suspend fun getUuidsOfDependentProcesses(fotoTimerProcess: TimerProcess): List<String> {
        return meditationTimerProcessDAO.getUuidsOfDependantProcesses(fotoTimerProcess.uuid)
    }

    @WorkerThread
    suspend fun doesProcessWithUuidExist(uuid: String): Boolean {
        return (meditationTimerProcessDAO.getTimerProcessByUuid(uuid) !== null)
    }

    @WorkerThread
    suspend fun getCategoryById(id: Long): TimerProcessCategory? {
        return meditationTimerProcessDAO.getCategoryById(id)
    }

    @WorkerThread
    suspend fun insert(fotoTimerProcess: TimerProcess) {
        meditationTimerProcessDAO.insert(fotoTimerProcess)
    }

    @WorkerThread
    suspend fun update(fotoTimerProcess: TimerProcess) {
        meditationTimerProcessDAO.updateProcess(fotoTimerProcess)
    }

    @WorkerThread
    suspend fun delete(fotoTimerProcess: TimerProcess) {
        meditationTimerProcessDAO.delete(fotoTimerProcess)
    }

    @WorkerThread
    suspend fun insertCategory(category: TimerProcessCategory) {
        meditationTimerProcessDAO.insertCategory(category)
    }

    @WorkerThread
    suspend fun updateCategory(category: TimerProcessCategory) {
        meditationTimerProcessDAO.updateCategory(category)
    }

    @WorkerThread
    suspend fun deleteCategoriesByIdsFromList(listOfIds: List<Long>) {
        if (listOfIds.isNotEmpty()) {
            meditationTimerProcessDAO.deleteCategoriesByIdsFromList(listOfIds)
        }
    }
}