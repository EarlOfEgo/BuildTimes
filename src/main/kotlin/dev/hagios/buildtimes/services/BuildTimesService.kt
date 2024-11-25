package dev.hagios.buildtimes.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import dev.hagios.buildtimes.extensions.formatDuration
import dev.hagios.buildtimes.settings.BuildTimesSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Service(Service.Level.PROJECT)
@State(
    name = "BuildTimesService", storages = [
        Storage("BuildTimesService.xml")
    ]
)
class BuildTimesService(
    private val project: Project
) : SimplePersistentStateComponent<Statistics>(Statistics()), Disposable {

    private val _buildStartTimes = MutableStateFlow<Map<Long, Long>>(emptyMap())
    val buildStartTimes: StateFlow<Map<Long, Long>> get() = _buildStartTimes

    private val settings = BuildTimesSettings.getInstance(project)

    fun addBuildTime(startTime: Long, endTime: Long, successful: Boolean) {
        state.buildStartTimes.add(startTime)
        state.buildEndTimes[startTime] = endTime
        state.buildSuccessful[startTime] = successful
        val totalTime = endTime - startTime
        if (totalTime > settings.notificationTime && settings.areNotificationsEnabled) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("BuildTimes Notifications")
                .createNotification("Build (${formatDuration(totalTime)}) took longer than ${formatDuration(settings.notificationTime.toLong())}.", NotificationType.WARNING)
                .apply {
                    setTitle("Build time")
                }
                .notify(project)
        }
        _buildStartTimes.value = _buildStartTimes.value.toMutableMap().apply {
            put(startTime, endTime)
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
    var testo by string()
}

data class Build(
    val started: Long,
    val ended: Long,
    val successful: Boolean,
)