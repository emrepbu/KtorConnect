package com.emrepbu.ktorconnect.server.repository

import com.emrepbu.ktorconnect.server.data.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Repository class manages the data used by the server.
 * In a real application, this data might be stored in a database.
 */
class DataRepository {
    private val _dataSet = MutableStateFlow(
        listOf(
            SampleData(0, "Item 0", 0.0),
            SampleData(1, "Item 1", 1.0),
            SampleData(2, "Item 2", 2.0),
            SampleData(3, "Item 3", 3.0),
            SampleData(4, "Item 4", 4.0),
            SampleData(5, "Item 5", 5.0),
            SampleData(6, "Item 6", 6.0),
            SampleData(7, "Item 7", 7.0),
            SampleData(8, "Item 8", 8.0),
            SampleData(9, "Item 9", 9.0),
        )
    )
    val dataSet: StateFlow<List<SampleData>> = _dataSet.asStateFlow()

    /**
     * Returns the data item with the specified ID.
     */
    fun getDataById(id: Int): SampleData? {
        return _dataSet.value.find { it.id == id }
    }

    /**
     * Returns the list of all data items.
     */
    fun getAllData(): List<SampleData> {
        return _dataSet.value
    }

    /**
     * Adds the specified data item to the list of data items.
     */
    fun addData(data: SampleData): Boolean {
        if (_dataSet.value.any { it.id == data.id }) {
            return false
        }
        _dataSet.value += data
        return true
    }

    /**
     * Updates the specified data item in the list of data items.
     */
    fun updateData(data: SampleData): Boolean {
        val currentDataSet = _dataSet.value.toMutableList()
        val index = currentDataSet.indexOfFirst { it.id == data.id }

        if (index == -1) {
            return false
        }

        currentDataSet[index] = data
        _dataSet.value = currentDataSet
        return true
    }

    /**
     * Delete the specified data item from the list of data items.
     */
    fun deleteDataById(id: Int): Boolean {
        val currentDataSet = _dataSet.value
        if (currentDataSet.none { it.id == id }) {
            return false
        }
        _dataSet.value = currentDataSet.filter { it.id != id }
        return true
    }

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataRepository().also { INSTANCE = it }
            }
        }
    }
}
