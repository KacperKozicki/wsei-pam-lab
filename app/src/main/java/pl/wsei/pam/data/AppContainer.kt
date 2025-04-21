package pl.wsei.pam.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pl.wsei.pam.NotificationHandler
import pl.wsei.pam.TodoTask


interface AppContainer {
    val todoTaskRepository: TodoTaskRepository
    val currentDateProvider: CurrentDateProvider
    val notificationHandler: NotificationHandler

}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val todoTaskRepository: TodoTaskRepository by lazy {
        // In-memory repository implementation
        object : TodoTaskRepository {
            private val tasks = mutableListOf<TodoTask>()
            private val tasksFlow = MutableStateFlow<List<TodoTask>>(emptyList())

            override fun getAllAsStream(): Flow<List<TodoTask>> = tasksFlow.asStateFlow()

            override fun getItemAsStream(id: Int): Flow<TodoTask?> =
                tasksFlow.map { tasks -> tasks.find { it.id == id } }

            override suspend fun insertItem(item: TodoTask) {
                val newItem = item.copy(id = tasks.size + 1)
                tasks.add(newItem)
                tasksFlow.update { tasks.toList() }
            }

            override suspend fun updateItem(item: TodoTask) {
                val index = tasks.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    tasks[index] = item
                    tasksFlow.update { tasks.toList() }
                }
            }

            override suspend fun deleteItem(item: TodoTask) {
                if (tasks.removeIf { it.id == item.id }) {
                    tasksFlow.update { tasks.toList() }
                }
            }
        }
    }

    override val currentDateProvider: CurrentDateProvider by lazy {
        DefaultCurrentDateProvider()
    }

    override val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(context)
    }
}