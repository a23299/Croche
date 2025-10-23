package com.croche.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

data class CounterObject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val counters: MutableList<Counter> = mutableListOf(),
    val color: Long = 0xFF2A2A2A // Default to LightGray
)

data class Counter(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val initialValue: Int = 0,
    val color: Long = 0xFF2A2A2A // Default to LightGray to match surface
) {
    var value by mutableStateOf(initialValue)
}