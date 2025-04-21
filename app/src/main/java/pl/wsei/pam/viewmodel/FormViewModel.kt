package pl.wsei.pam.viewmodel

import androidx.lifecycle.ViewModel
import pl.wsei.pam.Priority
import pl.wsei.pam.TodoTask
import pl.wsei.pam.data.CurrentDateProvider
import pl.wsei.pam.data.TodoRepository
import java.time.LocalDate

class FormViewModel(
    private val todoRepository: TodoRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {

    var title: String = ""
    var deadline: LocalDate = LocalDate.now().plusDays(1)
    var isDone: Boolean = false
    var priority: Priority = Priority.Medium

    fun validate(): Boolean {
        return deadline.isAfter(currentDateProvider.currentDate)
    }

    fun saveTask(): Boolean {
        if (!validate()) return false

        val task = TodoTask(
            title = title,
            deadline = deadline,
            isDone = isDone,
            priority = priority
        )
        todoRepository.addTask(task)
        return true
    }
}