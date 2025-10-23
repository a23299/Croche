package com.croche

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

fun Color.isDark() = (red * 0.299 + green * 0.587 + blue * 0.114) < 0.5

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
            objectId?.let {
                ObjectScreen(navController = navController, viewModel = viewModel, objectId = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: CounterViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showActionsFor by remember { mutableStateOf<CounterObject?>(null) }
    var showRenameDialogFor by remember { mutableStateOf<CounterObject?>(null) }
    var showColorPickerFor by remember { mutableStateOf<CounterObject?>(null) }
    var showDeleteDialogFor by remember { mutableStateOf<CounterObject?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meus Contadores") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Objeto")
            }
        }
    ) { padding ->
        if (viewModel.counterObjects.isEmpty()) {
            EmptyState("Nenhum objeto ainda", "Toque em '+' para criar o seu primeiro objeto.")
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(viewModel.counterObjects, key = { it.id }) { obj ->
                    ObjectCard(
                        obj = obj,
                        onClick = { navController.navigate("object/${obj.id}") },
                        onLongClick = { showActionsFor = obj }
                    )
                }
            }
        }

        if (showCreateDialog) {
            ObjectNameDialog(onDismiss = { showCreateDialog = false }) { name ->
                viewModel.addCounterObject(name)
                showCreateDialog = false
            }
        }

        showActionsFor?.let { obj ->
            ObjectActionsDialog(
                onDismiss = { showActionsFor = null },
                onRename = { showRenameDialogFor = obj },
                onColor = { showColorPickerFor = obj },
                onDelete = { showDeleteDialogFor = obj }
            )
        }

        showRenameDialogFor?.let { obj ->
            RenameObjectDialog(obj, onDismiss = { showRenameDialogFor = null }) { newName ->
                viewModel.renameObject(obj.id, newName)
                showRenameDialogFor = null
            }
        }

        showColorPickerFor?.let { obj ->
            ColorPickerDialog(onDismiss = { showColorPickerFor = null }) { newColor ->
                viewModel.changeObjectColor(obj.id, newColor)
                showColorPickerFor = null
            }
        }

        showDeleteDialogFor?.let { obj ->
            DeleteConfirmationDialog(itemName = obj.name, onDismiss = { showDeleteDialogFor = null }) {
                viewModel.deleteObject(obj.id)
                showDeleteDialogFor = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectScreen(navController: NavController, viewModel: CounterViewModel, objectId: String) {
    val counterObject by remember(objectId) { derivedStateOf { viewModel.counterObjects.find { it.id == objectId } } }

    LaunchedEffect(counterObject) {
        if (counterObject == null) {
            navController.popBackStack()
        }
    }

    val currentObject = counterObject
    if (currentObject != null) {
        var showCreateCounterDialog by remember { mutableStateOf(false) }
        var showObjectActions by remember { mutableStateOf(false) }
        var showRenameObjectDialog by remember { mutableStateOf(false) }
        var showObjectColorPicker by remember { mutableStateOf(false) }
        var showDeleteObjectDialog by remember { mutableStateOf(false) }

        var showCounterActionsFor by remember { mutableStateOf<Counter?>(null) }
        var showRenameCounterDialog by remember { mutableStateOf<Counter?>(null) }
        var showCounterColorPicker by remember { mutableStateOf<Counter?>(null) }
        var showDeleteCounterDialog by remember { mutableStateOf<Counter?>(null) }
        var showEditValueDialog by remember { mutableStateOf<Counter?>(null) }

        val topBarColor = Color(currentObject.color)
        val onTopBarColor = if (topBarColor.isDark()) Color.White else Color.Black

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentObject.name, color = onTopBarColor) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = onTopBarColor) } },
                    actions = { IconButton(onClick = { showObjectActions = true }) { Icon(Icons.Default.MoreVert, "Mais opções", tint = onTopBarColor) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
                )
            },
            floatingActionButton = { FloatingActionButton(onClick = { showCreateCounterDialog = true }) { Icon(Icons.Default.Add, "Adicionar Contador") } }
        ) { padding ->
            if (currentObject.counters.isEmpty()) {
                EmptyState("Nenhum contador ainda", "Toque em '+' para adicionar o seu primeiro contador.")
            } else {
                LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(currentObject.counters, key = { it.id }) { counter ->
                        CounterRow(
                            counter = counter,
                            onIncrement = { viewModel.incrementCounter(objectId, counter.id) },
                            onDecrement = { viewModel.decrementCounter(objectId, counter.id) },
                            onLongClick = { showCounterActionsFor = counter }
                        )
                    }
                }
            }

            if (showCreateCounterDialog) {
                CounterLabelDialog(onDismiss = { showCreateCounterDialog = false }) { label -> viewModel.addCounter(objectId, label); showCreateCounterDialog = false }
            }
            if (showObjectActions) {
                ObjectActionsDialog(onDismiss = { showObjectActions = false }, onRename = { showRenameObjectDialog = true }, onColor = { showObjectColorPicker = true }, onDelete = { showDeleteObjectDialog = true })
            }
            if (showRenameObjectDialog) {
                RenameObjectDialog(currentObject, onDismiss = { showRenameObjectDialog = false }) { newName -> viewModel.renameObject(currentObject.id, newName); showRenameObjectDialog = false }
            }
            if (showObjectColorPicker) {
                ColorPickerDialog(onDismiss = { showObjectColorPicker = false }) { newColor -> viewModel.changeObjectColor(currentObject.id, newColor); showObjectColorPicker = false }
            }
            if (showDeleteObjectDialog) {
                DeleteConfirmationDialog(currentObject.name, onDismiss = { showDeleteObjectDialog = false }) { viewModel.deleteObject(currentObject.id) }
            }

            showCounterActionsFor?.let { counter ->
                CounterActionsDialog(
                    onDismiss = { showCounterActionsFor = null },
                    onRename = { showRenameCounterDialog = counter },
                    onColor = { showCounterColorPicker = counter },
                    onDelete = { showDeleteCounterDialog = counter },
                    onEditValue = { showEditValueDialog = counter }
                )
            }

            showRenameCounterDialog?.let { counter ->
                RenameCounterDialog(counter, onDismiss = { showRenameCounterDialog = null }) { newName ->
                    viewModel.renameCounter(objectId, counter.id, newName)
                    showRenameCounterDialog = null
                }
            }

            showCounterColorPicker?.let { counter ->
                ColorPickerDialog(onDismiss = { showCounterColorPicker = null }) { newColor ->
                    viewModel.changeCounterColor(objectId, counter.id, newColor)
                    showCounterColorPicker = null
                }
            }

            showDeleteCounterDialog?.let { counter ->
                DeleteConfirmationDialog(itemName = counter.label, onDismiss = { showDeleteCounterDialog = null }) {
                    viewModel.deleteCounter(objectId, counter.id)
                    showDeleteCounterDialog = null
                }
            }

            showEditValueDialog?.let { counter ->
                CounterValueDialog(counter, onDismiss = { showEditValueDialog = null }) { newValue ->
                    viewModel.setCounterValue(objectId, counter.id, newValue)
                    showEditValueDialog = null
                }
            }
        }
    } else {
        EmptyState("A carregar...", "")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObjectCard(obj: CounterObject, onClick: () -> Unit, onLongClick: () -> Unit) {
    val cardColor = Color(obj.color)
    val textColor = if (cardColor.isDark()) Color.White else Color.Black
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(obj.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${obj.counters.size} contadores", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ObjectActionsDialog(onDismiss: () -> Unit, onRename: () -> Unit, onColor: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ações do Objeto") },
        text = {
            Column {
                Text("Mudar o nome", modifier = Modifier.fillMaxWidth().clickable { onRename(); onDismiss() }.padding(vertical = 12.dp))
                Text("Mudar a cor", modifier = Modifier.fillMaxWidth().clickable { onColor(); onDismiss() }.padding(vertical = 12.dp))
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Eliminar", modifier = Modifier.fillMaxWidth().clickable { onDelete(); onDismiss() }.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CounterRow(counter: Counter, onIncrement: () -> Unit, onDecrement: () -> Unit, onLongClick: () -> Unit) {
    val backgroundColor = Color(counter.color)
    val onBackgroundColor = if (backgroundColor.isDark()) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(counter.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = onBackgroundColor)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) { Icon(Icons.Default.KeyboardArrowDown, "Diminuir", tint = onBackgroundColor) }
            Text(text = counter.value.toString(), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp), color = onBackgroundColor)
            IconButton(onClick = onIncrement) { Icon(Icons.Default.KeyboardArrowUp, "Aumentar", tint = onBackgroundColor) }
        }
    }
}


@Composable
fun CounterActionsDialog(onDismiss: () -> Unit, onRename: () -> Unit, onColor: () -> Unit, onDelete: () -> Unit, onEditValue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ações do Contador") },
        text = {
            Column {
                Text("Mudar o nome", modifier = Modifier.fillMaxWidth().clickable { onRename(); onDismiss() }.padding(vertical = 12.dp))
                Text("Mudar a cor", modifier = Modifier.fillMaxWidth().clickable { onColor(); onDismiss() }.padding(vertical = 12.dp))
                Text("Editar o valor", modifier = Modifier.fillMaxWidth().clickable { onEditValue(); onDismiss() }.padding(vertical = 12.dp))
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Eliminar", modifier = Modifier.fillMaxWidth().clickable { onDelete(); onDismiss() }.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
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
fun ObjectNameDialog(initialName: String = "", onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "Novo Objeto" else "Mudar o nome do Objeto") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Nome do Objeto") }) },
        confirmButton = { Button(onClick = { onConfirm(text) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun RenameObjectDialog(obj: CounterObject, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    ObjectNameDialog(initialName = obj.name, onDismiss = onDismiss, onConfirm = onConfirm)
}

@Composable
fun RenameCounterDialog(counter: Counter, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(counter.label) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mudar o nome do Contador") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Nome do Contador") }) },
        confirmButton = { Button(onClick = { onConfirm(text) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterValueDialog(counter: Counter, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf(counter.value.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar valor do Contador") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '-' } },
                label = { Text("Novo valor") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = { Button(onClick = { onConfirm(text.toIntOrNull() ?: 0); onDismiss() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


@Composable
fun ColorPickerDialog(onDismiss: () -> Unit, onColorSelected: (Long) -> Unit) {
    val colors = listOf(
        0xFFFFFFFF, 0xFF000000, 0xFFF44336, 0xFFFF9800,
        0xFFFFEB3B, 0xFF4CAF50, 0xFF9C27B0, 0xFF795548, 0xFF2196F3
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha uma cor") },
        text = {
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.padding(top = 16.dp)) {
                items(colors) { colorValue ->
                    val color = Color(colorValue)
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(6.dp)
                            .background(color, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            .clickable { onColorSelected(colorValue); onDismiss() }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun DeleteConfirmationDialog(itemName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Item") },
        text = { Text("Tem a certeza de que pretende eliminar \"$itemName\"? Esta ação não pode ser desfeita.") },
        confirmButton = { Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterLabelDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Contador") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Etiqueta do Contador") }) },
        confirmButton = { Button(onClick = { onConfirm(text) }) { Text("Criar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}