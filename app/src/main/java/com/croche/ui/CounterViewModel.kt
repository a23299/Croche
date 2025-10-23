package com.croche.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.croche.data.Counter
import com.croche.data.CounterObject

class CounterViewModel : ViewModel() {
    val counterObjects = mutableStateListOf<CounterObject>()

    // Object-level actions
    fun addCounterObject(name: String) {
        counterObjects.add(CounterObject(name = name))
    }

    fun renameObject(objectId: String, newName: String) {
        val index = counterObjects.indexOfFirst { it.id == objectId }
        if (index != -1) {
            counterObjects[index] = counterObjects[index].copy(name = newName)
        }
    }

    fun changeObjectColor(objectId: String, newColor: Long) {
        val index = counterObjects.indexOfFirst { it.id == objectId }
        if (index != -1) {
            counterObjects[index] = counterObjects[index].copy(color = newColor)
        }
    }

    fun deleteObject(objectId: String) {
        counterObjects.removeIf { it.id == objectId }
    }

    // Counter-level actions
    fun addCounter(objectId: String, counterLabel: String) {
        val objectIndex = counterObjects.indexOfFirst { it.id == objectId }
        if (objectIndex != -1) {
            val oldObject = counterObjects[objectIndex]
            val newCounters = oldObject.counters.toMutableList().apply {
                add(Counter(label = counterLabel))
            }
            counterObjects[objectIndex] = oldObject.copy(counters = newCounters)
        }
    }

    fun renameCounter(objectId: String, counterId: String, newName: String) {
        val objectIndex = counterObjects.indexOfFirst { it.id == objectId }
        if (objectIndex != -1) {
            val oldObject = counterObjects[objectIndex]
            val counterIndex = oldObject.counters.indexOfFirst { it.id == counterId }
            if (counterIndex != -1) {
                val newCounters = oldObject.counters.toMutableList()
                newCounters[counterIndex] = newCounters[counterIndex].copy(label = newName)
                counterObjects[objectIndex] = oldObject.copy(counters = newCounters)
            }
        }
    }

    fun changeCounterColor(objectId: String, counterId: String, newColor: Long) {
        val objectIndex = counterObjects.indexOfFirst { it.id == objectId }
        if (objectIndex != -1) {
            val oldObject = counterObjects[objectIndex]
            val counterIndex = oldObject.counters.indexOfFirst { it.id == counterId }
            if (counterIndex != -1) {
                val newCounters = oldObject.counters.toMutableList()
                newCounters[counterIndex] = newCounters[counterIndex].copy(color = newColor)
                counterObjects[objectIndex] = oldObject.copy(counters = newCounters)
            }
        }
    }

    fun deleteCounter(objectId: String, counterId: String) {
        val objectIndex = counterObjects.indexOfFirst { it.id == objectId }
        if (objectIndex != -1) {
            val oldObject = counterObjects[objectIndex]
            val newCounters = oldObject.counters.toMutableList()
            newCounters.removeIf { it.id == counterId }
            counterObjects[objectIndex] = oldObject.copy(counters = newCounters)
        }
    }

    fun setCounterValue(objectId: String, counterId: String, newValue: Int) {
        val obj = counterObjects.find { it.id == objectId }
        obj?.counters?.find { it.id == counterId }?.let { it.value = newValue }
    }

    fun incrementCounter(objectId: String, counterId: String) {
        val obj = counterObjects.find { it.id == objectId }
        obj?.counters?.find { it.id == counterId }?.let { it.value++ }
    }

    fun decrementCounter(objectId: String, counterId: String) {
        val obj = counterObjects.find { it.id == objectId }
        obj?.counters?.find { it.id == counterId }?.let { it.value-- }
    }
}