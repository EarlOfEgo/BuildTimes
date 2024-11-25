package dev.hagios.buildtimes.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.*
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
) : SimplePersistentStateComponent<Statistics>(Statistics()), Disposable {

//    private var state = Statistics()
    private val settings = BuildTimesSettings.getInstance(project)

    val builds: List<Build> = state.buildStartTimes.mapNotNull {
        val startTime = it
        val endTime = state.buildEndTimes[it] ?: return@mapNotNull null
        val successful = state.buildSuccessful[it] ?: return@mapNotNull null
        Build(startTime, endTime, successful)
    }

    fun addBuildTime(startTime: Long, endTime: Long, successful: Boolean) {
        state.buildStartTimes.add(startTime)
        state.buildEndTimes[startTime] = endTime
        state.buildSuccessful[startTime] = successful
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

class Statistics: BaseState() {
    var buildStartTimes by list<Long>()
    var buildEndTimes by map<Long, Long>()
    var buildSuccessful by map<Long, Boolean>()
}

data class Build(
    val started: Long,
    val ended: Long,
    val successful: Boolean,
)