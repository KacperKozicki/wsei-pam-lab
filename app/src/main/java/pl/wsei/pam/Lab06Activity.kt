package pl.wsei.pam

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.wsei.pam.lab06.ui.theme.Lab06Theme as MyAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import pl.wsei.pam.data.TodoRepository
import java.time.Instant
import java.time.ZoneId
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat


import androidx.compose.material.icons.filled.Delete
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavType
import androidx.navigation.navArgument
import pl.wsei.pam.data.AppContainer
import pl.wsei.pam.lab01.R

const val notificationID = 121
const val channelID = "Lab06 channel"
const val titleExtra = "title"
const val messageExtra = "message"
class Lab06Activity : ComponentActivity() {
    companion object {
        lateinit var container: AppContainer
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        container = (this.application as TodoApplication).container
        scheduleAlarm(System.currentTimeMillis() + 2000)



        setContent {
            MyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Lab06 channel"
        val descriptionText = "Lab06 is channel for notifications for approaching tasks."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleAlarm(time: Long) {
        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
        intent.putExtra(titleExtra, "Deadline")
        intent.putExtra(messageExtra, "Zbliża się termin zakończenia zadania")

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }
}




enum class Priority {
    High, Medium, Low
}

data class TodoTask(
    val id: Int = 0,
    val title: String,
    val deadline: LocalDate,
    val isDone: Boolean,
    val priority: Priority
)


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") { ListScreen(navController = navController) }
        composable("form") { FormScreen(navController = navController) }
        composable(
            "taskDetail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            TaskDetailScreen(navController = navController, taskId = taskId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.navigate(route) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (route != "form") {
                OutlinedButton(
                    onClick = { navController.navigate("list") }
                ) {
                    Text(
                        text = "Zapisz",
                        fontSize = 18.sp
                    )
                }
            } else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                }
            }
        }
    )
}

@Composable
fun ListItem(
    item: TodoTask,
    onCheckChanged: (TodoTask) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when(item.priority) {
        Priority.High -> Color.Red
        Priority.Medium -> Color.Blue
        Priority.Low -> Color.Green
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                // Title with checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { isChecked ->
                            // Call the callback with the updated task
                            onCheckChanged(item.copy(isDone = isChecked))
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
                ) {
                    Text(
                        text = "Deadline: ${item.deadline.format(dateFormatter)}",
                        fontSize = 14.sp
                    )
                }
            }

            // Priority badge
            Box(
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(4.dp)
            ) {
                Surface(
                    color = priorityColor,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = item.priority.name,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavController) {
    val context = LocalContext.current
    val todoRepository = (context.applicationContext as TodoApplication).container.todoTaskRepository
    val tasks by todoRepository.getAllAsStream().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Lista zadań",
                showBackIcon = false,
                route = "form"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = {
                    navController.navigate("form")
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Dodawanie zadania",
                    modifier = Modifier.scale(1.5f)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(items = tasks) { task ->
                ListItem(
                    item = task,
                    onCheckChanged = { updatedTask ->
                        coroutineScope.launch {
                            todoRepository.updateItem(updatedTask)
                        }
                    },
                    onClick = {
                        navController.navigate("taskDetail/${task.id}")
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController) {
    val context = LocalContext.current
    val todoRepository = (context.applicationContext as TodoApplication).container.todoTaskRepository
    val dateProvider = (context.applicationContext as TodoApplication).container.currentDateProvider
    val coroutineScope = rememberCoroutineScope()

    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.Medium) }
    var isTaskDone by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    fun validate(): Boolean {
        if (selectedDate.isBefore(dateProvider.currentDate) || selectedDate.isEqual(dateProvider.currentDate)) {
            validationError = "Deadline musi być późniejszy niż dzisiejsza data"
            return false
        }
        validationError = null
        return true
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Add Task",
                showBackIcon = true,
                route = "list"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Tytuł zadania") },
                modifier = Modifier.fillMaxWidth()
            )

            // Priority selection using RadioButtons
            Text(
                text = "Priorytet:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Priority.values().forEach { priority ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority }
                        )
                        Text(
                            text = priority.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = when (priority) {
                                        Priority.High -> Color.Red
                                        Priority.Medium -> Color.Blue
                                        Priority.Low -> Color.Green
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Date picker field
            OutlinedTextField(
                value = selectedDate.format(dateFormatter),
                onValueChange = { },
                label = { Text("Należy wykonać do") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Wybierz datę"
                        )
                    }
                }
            )

            // Validation error display
            validationError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Task status with improved Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { isTaskDone = !isTaskDone }
            ) {
                Checkbox(
                    checked = isTaskDone,
                    onCheckedChange = { isTaskDone = it }
                )
                Text(
                    text = "Oznacz jako ukończone",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Submit button
            Button(
                onClick = {
                    if (validate()) {
                        // Create and save the new task
                        val newTask = TodoTask(
                            title = taskTitle,
                            deadline = selectedDate,
                            isDone = isTaskDone,
                            priority = selectedPriority
                        )
                        coroutineScope.launch {
                            todoRepository.insertItem(newTask)
                            navController.navigate("list")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Zapisz zadanie")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyAppTheme {
        MainScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navController: NavController, taskId: Int) {
    val context = LocalContext.current
    val todoRepository = (context.applicationContext as TodoApplication).container.todoTaskRepository
    val dateProvider = (context.applicationContext as TodoApplication).container.currentDateProvider
    val coroutineScope = rememberCoroutineScope()

    val taskStream = todoRepository.getItemAsStream(taskId)
    val task by taskStream.collectAsState(initial = null)

    // Create state variables for the form fields
    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.Medium) }
    var isTaskDone by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Load task data when it becomes available
    LaunchedEffect(task) {
        task?.let {
            taskTitle = it.title
            selectedPriority = it.priority
            isTaskDone = it.isDone
            selectedDate = it.deadline
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    fun validate(): Boolean {
        if (selectedDate.isBefore(dateProvider.currentDate) || selectedDate.isEqual(dateProvider.currentDate)) {
            validationError = "Deadline musi być późniejszy niż dzisiejsza data"
            return false
        }
        validationError = null
        return true
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Potwierdzenie usunięcia") },
            text = { Text("Czy na pewno chcesz usunąć to zadanie?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        task?.let {
                            coroutineScope.launch {
                                todoRepository.deleteItem(it)
                                showDeleteConfirmation = false
                                navController.popBackStack()
                            }
                        }
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(text = "Edytuj zadanie") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete task"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Tytuł zadania") },
                modifier = Modifier.fillMaxWidth()
            )

            // Priority selection using RadioButtons
            Text(
                text = "Priorytet:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Priority.values().forEach { priority ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority }
                        )
                        Text(
                            text = priority.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = when (priority) {
                                        Priority.High -> Color.Red
                                        Priority.Medium -> Color.Blue
                                        Priority.Low -> Color.Green
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Date picker field
            OutlinedTextField(
                value = selectedDate.format(dateFormatter),
                onValueChange = { },
                label = { Text("Należy wykonać do") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Wybierz datę"
                        )
                    }
                }
            )

            // Validation error display
            validationError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Task status with improved Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { isTaskDone = !isTaskDone }
            ) {
                Checkbox(
                    checked = isTaskDone,
                    onCheckedChange = { isTaskDone = it }
                )
                Text(
                    text = "Oznacz jako ukończone",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Update button
            Button(
                onClick = {
                    if (validate()) {
                        task?.let {
                            // Create updated task
                            val updatedTask = it.copy(
                                title = taskTitle,
                                deadline = selectedDate,
                                isDone = isTaskDone,
                                priority = selectedPriority
                            )
                            coroutineScope.launch {
                                todoRepository.updateItem(updatedTask)
                                navController.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Zapisz zmiany")
            }
        }
    }
}