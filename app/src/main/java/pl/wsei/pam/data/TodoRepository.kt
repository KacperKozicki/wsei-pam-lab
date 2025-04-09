// TodoRepository.kt
package pl.wsei.pam.data

import pl.wsei.pam.Priority
import pl.wsei.pam.TodoTask
import java.time.LocalDate

interface TodoRepository {
    fun getTasks(): List<TodoTask>
    fun addTask(task: TodoTask)
}

class TodoRepositoryImpl : TodoRepository {
    private val tasks = mutableListOf(
        TodoTask("Programowanie2", LocalDate.of(2024, 4, 18), false, Priority.Low),
        TodoTask("Uczenie2", LocalDate.of(2024, 5, 12), false, Priority.High),
        TodoTask("Szkolenie2", LocalDate.of(2024, 6, 28), true, Priority.Low),
        TodoTask("Gotowanie2", LocalDate.of(2024, 8, 18), false, Priority.Medium),
    )

    override fun getTasks(): List<TodoTask> = tasks.toList()

    override fun addTask(task: TodoTask) {
        tasks.add(task)
    }
}