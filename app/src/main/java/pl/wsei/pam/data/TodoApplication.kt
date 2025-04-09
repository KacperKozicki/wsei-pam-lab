// TodoApplication.kt
package pl.wsei.pam

import android.app.Application
import pl.wsei.pam.data.AppContainer
import pl.wsei.pam.data.AppDataContainer

class TodoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this.applicationContext)
    }
}