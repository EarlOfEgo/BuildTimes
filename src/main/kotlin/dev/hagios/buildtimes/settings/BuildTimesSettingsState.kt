package dev.hagios.buildtimes.settings

import com.intellij.openapi.components.BaseState

class BuildTimesSettingsState: BaseState() {
    var areNotificationsEnabled by property(true)
    var notificationTime by property(5000)
}