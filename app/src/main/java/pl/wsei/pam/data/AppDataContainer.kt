package pl.wsei.pam.data

import android.content.Context
import pl.wsei.pam.NotificationHandler

class AppDataContainer(private val context: Context) : AppContainer {
    override val todoTaskRepository: TodoTaskRepository by lazy {
        DatabaseTodoTaskRepository(AppDatabase.getInstance(context).taskDao())
    }

    override val currentDateProvider: CurrentDateProvider by lazy {
        DefaultCurrentDateProvider()
    }

    override val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(context)
    }
}