package dev.hagios.buildtimes.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import dev.hagios.buildtimes.settings.BuildTimesSettings


@Service(Service.Level.PROJECT)
@State(
    name = "BuildTimesService", storages = [
        Storage("BuildTimesService.xml")
    ]
)
class BuildTimesService(
    private val project: Project
) : PersistentStateComponent<Statistics>, Disposable {

    private var state = Statistics()
    private val settings = BuildTimesSettings.getInstance(project)

    override fun getState(): Statistics {
        return state
    }

    override fun loadState(state: Statistics) {
        this.state = state
    }

    fun addBuildTime(startTime: Long, endTime: Long, successful: Boolean) {
        state = state.copy(builds = state.builds + Build(startTime, endTime, successful))
        val totalTime = endTime - startTime
        if (totalTime > settings.notificationTime && settings.areNotificationsEnabled) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("BuildTimes Notifications")
                .createNotification("Build (${totalTime}ms) takes longer than ${settings.notificationTime}ms.", NotificationType.WARNING)
                .apply {
                    setTitle("Build time")
                }
                .notify(project)
        }
    }

    override fun dispose() {
        thisLogger().info("Disposing BuildTimesService")
    }
}

data class Statistics(
    val builds: List<Build> = emptyList()
)

data class Build(
    val started: Long,
    val ended: Long,
    val successful: Boolean,
)