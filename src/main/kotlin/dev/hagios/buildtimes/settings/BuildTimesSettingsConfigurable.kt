package dev.hagios.buildtimes.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*

class BuildTimesSettingsConfigurable(project: Project) : BoundConfigurable("Build Times") {
    val buildTimesSettings = BuildTimesSettings.getInstance(project)

    override fun createPanel(): DialogPanel = panel {
        lateinit var checkBox: Cell<JBCheckBox>
        group("Notifications") {
            row {
                checkBox = checkBox("Show Notification when build takes longer")
                    .bindSelected(buildTimesSettings::areNotificationsEnabled)
            }.layout(RowLayout.PARENT_GRID)
            row("Notification time (ms)") {
                intTextField()
                    .bindIntText(buildTimesSettings::notificationTime)
            }.layout(RowLayout.PARENT_GRID).enabledIf(checkBox.selected)
        }
        group("Display") {
            row {
                checkBox = checkBox("Show failed builds in overview")
                    .bindSelected(buildTimesSettings::showFailedBuilds)
            }.layout(RowLayout.PARENT_GRID)
        }
    }
}

