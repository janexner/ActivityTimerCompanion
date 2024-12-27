package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.data.persistence.TimerChainingDependencies
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataIdAndName
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProcessDeleteViewModel.ProcessDeleteViewModelFactory::class)
class ProcessDeleteViewModel @AssistedInject constructor(
    @Assisted val uuid: String,
    private val repository: TimerDataRepository
) : ViewModel() {

    private val _processName: MutableLiveData<String> = MutableLiveData("")
    val processName: LiveData<String> = _processName

    private val _processIsTarget: MutableLiveData<Boolean> = MutableLiveData(false)
    val processIsTarget: LiveData<Boolean> = _processIsTarget

    private val _processChainingDependencies: MutableLiveData<TimerChainingDependencies> =
        MutableLiveData(null)
    val processChainingDependencies: LiveData<TimerChainingDependencies> =
        _processChainingDependencies

    init {
        viewModelScope.launch {
            val process = repository.loadProcessByUuid(uuid)
            if (process != null) {
                _processName.value = process.name
                val newDependentProcessUuids = repository.getUuidsOfDependentProcesses(process)
                val newDependentProcesses = mutableListOf<TimerDataIdAndName>()
                newDependentProcessUuids.forEach {
                    val tmpProcess = repository.loadProcessByUuid(it)
                    if (tmpProcess != null) {
                        newDependentProcesses.add(TimerDataIdAndName(it, tmpProcess.name))
                    }
                }
                val chainingDependencies = TimerChainingDependencies(
                    newDependentProcesses
                )
                _processChainingDependencies.value = chainingDependencies
                _processIsTarget.value = true
            }
        }
    }

    @AssistedFactory
    interface ProcessDeleteViewModelFactory {
        fun create(uuid: String): ProcessDeleteViewModel
    }

    fun deleteProcess(processUuid: String) {
        viewModelScope.launch {
            val ftp = repository.loadProcessByUuid(processUuid)
            if (ftp != null) {
                repository.delete(ftp)
            }
        }
    }
}