package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataRepository
import com.exner.tools.activitytimercompanion.data.persistence.TimerProcess
import com.exner.tools.activitytimercompanion.data.persistence.TimerProcessCategory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel(assistedFactory = ProcessEditViewModel.ProcessEditViewModelFactory::class)
class ProcessEditViewModel @AssistedInject constructor(
    @Assisted val uuid: String?,
    @Assisted val filterProcessesForCurrentCategory: Boolean,
    private val repository: TimerDataRepository,
) : ViewModel() {

    private val _uid: MutableLiveData<Long> = MutableLiveData(-1L)
    val uid: LiveData<Long> = _uid

    private val _name: MutableLiveData<String> = MutableLiveData("Name")
    val name: LiveData<String> = _name

    private val _info: MutableLiveData<String> = MutableLiveData("Details")
    val info: LiveData<String> = _info

    private val _processTime: MutableLiveData<Int> = MutableLiveData(30)
    val processTime: LiveData<Int> = _processTime

    private val _intervalTime: MutableLiveData<Int> = MutableLiveData(5)
    val intervalTime: LiveData<Int> = _intervalTime

    private val _hasAutoChain: MutableLiveData<Boolean> = MutableLiveData(false)
    val hasAutoChain: LiveData<Boolean> = _hasAutoChain

    private val _gotoUuid: MutableLiveData<String?> = MutableLiveData(null)
    private val _gotoName: MutableLiveData<String?> = MutableLiveData(null)
    val gotoName: LiveData<String?> = _gotoName

    private val _backgroundUri: MutableLiveData<String> = MutableLiveData("https://fototimer.net/assets/activitytimer/bg-default.png")
    val backgroundUri: LiveData<String> = _backgroundUri

    private val observeProcessesRaw = repository.observeProcesses

    private val _observeProcessesForCurrentCategory =
        MutableStateFlow(emptyList<TimerProcess>())
    val observeProcessesForCurrentCategory: StateFlow<List<TimerProcess>>
        get() = _observeProcessesForCurrentCategory

    val observeCategoriesRaw = repository.observeCategories

    private val _currentCategory = MutableStateFlow(TimerProcessCategory(name = "All", backgroundUri = null, uid = -1L))
    val currentCategory: StateFlow<TimerProcessCategory>
        get() = _currentCategory

    init {
        viewModelScope.launch {
            if (uuid != null) {
                val process = repository.loadProcessByUuid(uuid)
                if (process != null) {
                    _uid.value = process.uid
                    _name.value = process.name
                    _info.value = process.info
                    _processTime.value = process.processTime
                    _intervalTime.value = process.intervalTime
                    _hasAutoChain.value = process.hasAutoChain
                    _gotoUuid.value = process.gotoUuid
                    _gotoName.value = process.gotoName
                    updateCategoryId(process.categoryId ?: -1L, filterProcessesForCurrentCategory)
                    _backgroundUri.value = process.backgroundUri ?: "https://fototimer.net/assets/activitytimer/bg-default.png"
                }
            }
            observeProcessesRaw.collect { itemsList ->
                val filteredItemsList: List<TimerProcess> = itemsList.filter { item ->
                    item.categoryId == currentCategory.value.uid || currentCategory.value.uid == -1L
                }
                _observeProcessesForCurrentCategory.value = filteredItemsList
            }
        }
    }

    fun updateCategoryId(id: Long, filterProcessesForCurrentCategory: Boolean) {
        if (id == -1L) {
            _currentCategory.value = TimerProcessCategory(name = "All", backgroundUri = null, uid = -1L)
        } else {
            viewModelScope.launch {
                _currentCategory.value =
                    repository.getCategoryById(id) ?: TimerProcessCategory(name = "All", backgroundUri = null, uid = -1L)
            }
        }
        viewModelScope.launch {
            observeProcessesRaw.collect { itemsList ->
                val filteredItemsList: List<TimerProcess> = itemsList.filter { item ->
                    if (currentCategory.value.uid == -1L || !filterProcessesForCurrentCategory) {
                        true
                    } else {
                        item.categoryId == currentCategory.value.uid
                    }
                }
                _observeProcessesForCurrentCategory.value = filteredItemsList
            }
        }
    }

    fun commitProcess() {
        viewModelScope.launch {
            val process = TimerProcess(
                uid = uid.value!!.toLong(),
                name = name.value.toString(),
                info = info.value.toString(),
                processTime = if (processTime.value != null) processTime.value!!.toInt() else 30,
                intervalTime = if (intervalTime.value != null) intervalTime.value!!.toInt() else 10,
                hasAutoChain = hasAutoChain.value == true,
                gotoUuid = _gotoUuid.value,
                gotoName = _gotoName.value,
                categoryId = currentCategory.value.uid,
                backgroundUri = backgroundUri.value,
                uuid = uuid ?: UUID.randomUUID().toString()
            )
            if (!repository.doesProcessWithUuidExist(uuid = process.uuid)) {
                repository.insert(
                    process.copy(
                        uid = 0
                    )
                )
            } else {
                repository.update(process)
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateInfo(info: String) {
        _info.value = info
    }

    fun updateProcessTime(processTime: Int) {
        _processTime.value = processTime
    }

    fun updateIntervalTime(intervalTime: Int) {
        _intervalTime.value = intervalTime
    }

    fun updateHasAutoChain(hasAutoChain: Boolean) {
        _hasAutoChain.value = hasAutoChain
    }

    fun updateGotoUuidAndName(gotoUuid: String?, name: String?) {
        _gotoUuid.value = gotoUuid
        _gotoName.value = name
    }

    fun updateBackgroundUri(backgroundUri: String) {
        _backgroundUri.value = backgroundUri
    }

    @AssistedFactory
    interface ProcessEditViewModelFactory {
        fun create(uuid: String?, filterProcessesForCurrentCategory: Boolean): ProcessEditViewModel
    }
}