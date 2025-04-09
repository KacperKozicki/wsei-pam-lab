// app/src/main/java/pl/wsei/pam/data/AppDataContainer.kt
package pl.wsei.pam.data

import android.content.Context

class AppDataContainer(private val context: Context) : AppContainer {
    override val todoRepository: TodoRepository by lazy {
        TodoRepositoryImpl()
    }

    override val todoTaskRepository: TodoTaskRepository by lazy {
        DatabaseTodoTaskRepository(AppDatabase.getInstance(context).taskDao())
    }
}