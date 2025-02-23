package com.exner.tools.activitytimercompanion.ui.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exner.tools.activitytimercompanion.data.persistence.TimerCategoryIdNameCount
import com.exner.tools.activitytimercompanion.data.persistence.TimerProcessCategory
import com.exner.tools.activitytimercompanion.ui.BodyText
import com.exner.tools.activitytimercompanion.ui.CategoryListViewModel
import com.exner.tools.activitytimercompanion.ui.HeaderText
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun CategoryBulkDelete(
    categoryListViewModel: CategoryListViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val categories: List<TimerProcessCategory> by categoryListViewModel.observeCategoriesRaw.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val categoryUsage: List<TimerCategoryIdNameCount> by categoryListViewModel.observeCategoryUsage.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val listOfCategoryIdsToDelete = remember {
        mutableStateListOf<Long>()
    }

    val openAlertDialog = remember { mutableStateOf(false) }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(innerPadding)
                        .padding(innerPadding)
                        .imePadding()

                ) {
                    items(items = categories, key = {it.uid}) { category ->
                        var supText = "Unused"
                        val usage = categoryUsage.firstOrNull {
                            it.uid == category.uid
                        }
                        if (usage != null) {
                            if (usage.usageCount > 0) {
                                supText = "Used in ${usage.usageCount} process(es)"
                            }
                        }
                        ListItem(
                            leadingContent = {
                                Checkbox(
                                    checked = listOfCategoryIdsToDelete.contains(category.uid),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            listOfCategoryIdsToDelete.add(category.uid)
                                        } else {
                                            listOfCategoryIdsToDelete.remove(category.uid)
                                        }
                                    })
                            },
                            headlineContent = { HeaderText(text = category.name) },
                            supportingContent = { BodyText(text = supText) }
                        )
                    }
                }
                // Alert Dialog
                if (openAlertDialog.value) {
                    AlertDialog(
                        icon = {},
                        title = { Text(text = "Delete?") },
                        text = { Text(text = "Delete ${listOfCategoryIdsToDelete.size} categories?") },
                        onDismissRequest = { openAlertDialog.value = false },
                        dismissButton = {
                            TextButton(onClick = {
                                openAlertDialog.value = false
                            }) {
                                Text(text = "No")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                categoryListViewModel.deleteAllCategoriesFromListOfIds(
                                    listOfCategoryIdsToDelete
                                )
                                openAlertDialog.value = false
                                navigator.navigateUp()
                            }) {
                                Text(text = "Yes, delete")
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Delete") },
                        icon = {
                            Icon(Icons.Default.Delete, "Delete selected categories")
                        },
                        onClick = {
                            openAlertDialog.value = true
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    )
                }
            )
        }
    )
}