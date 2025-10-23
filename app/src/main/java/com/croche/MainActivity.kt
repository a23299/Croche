package com.croche

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.croche.data.Counter
import com.croche.data.CounterObject
import com.croche.ui.CounterViewModel
import com.croche.ui.theme.CrocheTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrocheTheme {
                CounterApp(viewModel)
            }
        }
    }
}

@Composable
fun CounterApp(viewModel: CounterViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            "object/{objectId}",
            arguments = listOf(navArgument("objectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val objectId = backStackEntry.arguments?.getString("objectId")
            if (objectId != null) {
                ObjectScreen(navController = navController, viewModel = viewModel, objectId = objectId)
            } else {
                // Handle error or navigate back
                navController.popBackStack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: CounterViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Contadores") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Objeto")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(viewModel.counterObjects) { obj ->
                ListItem(
                    headlineContent = { Text(obj.name) },
                    modifier = Modifier.clickable {
                        navController.navigate("object/${obj.id}")
                    }
                )
            }
        }

        if (showDialog) {
            ObjectNameDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name ->
                    viewModel.addCounterObject(name)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectScreen(navController: NavController, viewModel: CounterViewModel, objectId: String) {
    val counterObject = viewModel.counterObjects.find { it.id == objectId }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(counterObject?.name ?: "Objeto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Contador")
            }
        }
    ) { padding ->
        if (counterObject != null) {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(counterObject.counters) { counter ->
                    CounterRow(
                        counter = counter,
                        onIncrement = { viewModel.incrementCounter(objectId, counter.id) },
                        onDecrement = { viewModel.decrementCounter(objectId, counter.id) }
                    )
                }
            }
        }

        if (showDialog) {
            CounterLabelDialog(
                onDismiss = { showDialog = false },
                onConfirm = { label ->
                    viewModel.addCounter(objectId, label)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CounterRow(counter: Counter, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(counter.label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onDecrement) { Text("-") }
            Text(
                text = counter.value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(onClick = onIncrement) { Text("+") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectNameDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Objeto") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nome do Objeto") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("Criar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterLabelDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Contador") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Etiqueta do Contador") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("Criar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
