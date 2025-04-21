// app/src/main/java/pl/wsei/pam/data/TodoTaskEntity.kt
package pl.wsei.pam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.wsei.pam.Priority
import pl.wsei.pam.TodoTask
import java.time.LocalDate

@Entity(tableName = "tasks")
data class TodoTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val deadline: LocalDate,
    var isDone: Boolean,
    val priority: Priority
) {
    fun toModel(): TodoTask {
        return TodoTask(
            id = id,
            title = title,
            deadline = deadline,
            isDone = isDone,
            priority = priority
        )
    }

    companion object {
        fun fromModel(model: TodoTask): TodoTaskEntity {
            return TodoTaskEntity(
                id = model.id,  // Use existing ID, not 0
                title = model.title,
                priority = model.priority,
                isDone = model.isDone,
                deadline = model.deadline
            )
        }
    }
}