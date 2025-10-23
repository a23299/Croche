package com.croche

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.croche.ui.theme.LightBlue

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
        topBar = { TopAppBar(title = { Text("Meus Contadores") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Objeto")
            }
        }
    ) { padding ->
        if (viewModel.counterObjects.isEmpty()) {
            EmptyState("Nenhum objeto ainda", "Toque em '+' para criar o seu primeiro objeto.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(viewModel.counterObjects) { obj ->
                    ObjectCard(obj) {
                        navController.navigate("object/${obj.id}")
                    }
                }
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
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
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
        if (counterObject == null || counterObject.counters.isEmpty()) {
            EmptyState("Nenhum contador ainda", "Toque em '+' para adicionar o seu primeiro contador.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectCard(obj: CounterObject, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(obj.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${obj.counters.size} contadores", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CounterRow(counter: Counter, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(counter.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuir") }
            Text(
                text = counter.value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = onIncrement) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aumentar") }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
            TextButton(onClick = onDismiss) {
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
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
