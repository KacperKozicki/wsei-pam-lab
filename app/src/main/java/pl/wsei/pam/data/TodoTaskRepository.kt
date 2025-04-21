// app/src/main/java/pl/wsei/pam/data/TodoTaskRepository.kt
package pl.wsei.pam.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.wsei.pam.TodoTask

interface TodoTaskRepository {
    fun getAllAsStream(): Flow<List<TodoTask>>
    fun getItemAsStream(id: Int): Flow<TodoTask?>
    suspend fun insertItem(item: TodoTask)
    suspend fun deleteItem(item: TodoTask)
    suspend fun updateItem(item: TodoTask)
}

class DatabaseTodoTaskRepository(private val dao: TodoTaskDao) : TodoTaskRepository {
    override fun getAllAsStream(): Flow<List<TodoTask>> {
        return dao.findAll().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getItemAsStream(id: Int): Flow<TodoTask?> {
        return dao.find(id).map { it.toModel() }
    }

    override suspend fun insertItem(item: TodoTask) {
        dao.insertAll(TodoTaskEntity.fromModel(item))
    }

    override suspend fun deleteItem(item: TodoTask) {
        dao.removeById(TodoTaskEntity.fromModel(item))
    }

    override suspend fun updateItem(item: TodoTask) {
        val entity = TodoTaskEntity.fromModel(item)
        dao.updateTask(entity.id, entity.title, entity.deadline, entity.isDone, entity.priority)
    }
}