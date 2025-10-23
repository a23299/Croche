package com.croche.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.croche.data.Counter
import com.croche.data.CounterObject

class CounterViewModel : ViewModel() {
    val counterObjects = mutableStateListOf<CounterObject>()

    fun addCounterObject(name: String) {
        counterObjects.add(CounterObject(name = name))
    }

    fun addCounter(counterObjectId: String, counterLabel: String) {
        val objectIndex = counterObjects.indexOfFirst { it.id == counterObjectId }
        if (objectIndex != -1) {
            val oldObject = counterObjects[objectIndex]
            val newCounters = oldObject.counters.toMutableList().apply {
                add(Counter(label = counterLabel))
            }
            val newObject = oldObject.copy(counters = newCounters)
            counterObjects[objectIndex] = newObject
        }
    }

    fun incrementCounter(counterObjectId: String, counterId: String) {
        counterObjects.find { it.id == counterObjectId }?.counters?.find { it.id == counterId }?.let { it.value++ }
    }

    fun decrementCounter(counterObjectId: String, counterId: String) {
        counterObjects.find { it.id == counterObjectId }?.counters?.find { it.id == counterId }?.let { it.value-- }
    }
}