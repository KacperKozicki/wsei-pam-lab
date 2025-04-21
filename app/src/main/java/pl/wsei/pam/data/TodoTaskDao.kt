// app/src/main/java/pl/wsei/pam/data/TodoTaskDao.kt
package pl.wsei.pam.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.wsei.pam.Priority
import java.time.LocalDate

@Dao
interface TodoTaskDao {
    @Insert
    suspend fun insertAll(vararg tasks: TodoTaskEntity)

    @Delete
    suspend fun removeById(item: TodoTaskEntity)

    @Update
    suspend fun update(item: TodoTaskEntity)

    @Query("SELECT * FROM tasks ORDER BY deadline DESC")
    fun findAll(): Flow<List<TodoTaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun find(id: Int): Flow<TodoTaskEntity>

    @Query("UPDATE tasks SET title = :title, deadline = :deadline, isDone = :isDone, priority = :priority WHERE id = :id")
    suspend fun updateTask(id: Int, title: String, deadline: LocalDate, isDone: Boolean, priority: Priority)
}