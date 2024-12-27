package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataRepository
import com.exner.tools.activitytimercompanion.data.persistence.TimerProcessCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessListViewModel @Inject constructor(
    private val repository: TimerDataRepository
) : ViewModel() {

    // mocks
    val observeProcessesRaw = repository.observeProcesses
    val observeCategoriesRaw = repository.observeCategories
    private val _currentCategory = MutableStateFlow(
        TimerProcessCategory(
            name = "All",
            backgroundUri = null,
            uid = CategoryListDefinitions.CATEGORY_UID_ALL
        )
    )
    val currentCategory: StateFlow<TimerProcessCategory>
        get() = _currentCategory

    fun updateCategoryId(id: Long) {
        if (id == CategoryListDefinitions.CATEGORY_UID_ALL) {
            _currentCategory.value = TimerProcessCategory(
                name = "All",
                backgroundUri = null,
                uid = CategoryListDefinitions.CATEGORY_UID_ALL
            )
        } else {
            viewModelScope.launch {
                _currentCategory.value =
                    repository.getCategoryById(id) ?: TimerProcessCategory(
                        name = "None",
                        backgroundUri = null,
                        uid = CategoryListDefinitions.CATEGORY_UID_NONE
                    )
            }
        }
    }
}