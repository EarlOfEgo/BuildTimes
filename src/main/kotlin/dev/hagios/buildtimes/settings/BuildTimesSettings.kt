package dev.hagios.buildtimes.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.*

@Service(Service.Level.PROJECT)
@State(name = "BuildTimesSettings", storages = [(Storage("buildtimes.xml"))])
class BuildTimesSettings: SimplePersistentStateComponent<BuildTimesSettingsState>(BuildTimesSettingsState()) {
    var areNotificationsEnabled
        get() = state.areNotificationsEnabled
        set(value) {
            state.areNotificationsEnabled = value
        }

    var notificationTime
    get() = state.notificationTime
        set(value) {
            state.notificationTime = value
        }

    companion object {
        fun getInstance(project: Project): BuildTimesSettings = project.service()
    }
}

