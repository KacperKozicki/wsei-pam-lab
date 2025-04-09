// app/src/main/java/pl/wsei/pam/data/AppContainer.kt
package pl.wsei.pam.data

interface AppContainer {
    val todoRepository: TodoRepository
    val todoTaskRepository: TodoTaskRepository
}