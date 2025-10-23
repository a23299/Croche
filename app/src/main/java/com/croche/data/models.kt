package com.croche.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

data class CounterObject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val counters: MutableList<Counter> = mutableListOf()
)

data class Counter(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val initialValue: Int = 0
) {
    var value by mutableStateOf(initialValue)
}
