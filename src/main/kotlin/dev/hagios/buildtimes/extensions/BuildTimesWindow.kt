package dev.hagios.buildtimes.extensions

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import dev.hagios.buildtimes.statistics.BuildTimesStatistics
import dev.hagios.buildtimes.settings.BuildTimesSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.awt.BorderLayout
import java.time.format.DateTimeFormatter
import javax.swing.JPanel


class BuildTimesWindow(project: Project) {

    private val buildTimesStatistics = project.service<BuildTimesStatistics>()
    private val settings = project.service<BuildTimesSettings>()
    private val coroutineScope = CoroutineScope(Dispatchers.EDT)
    private val panel: JPanel = DialogPanel()

    init {
        coroutineScope.launch {
            buildTimesStatistics.buildStartTimes.collect {
                updateContent()
            }
        }
    }

    fun getContent(): JPanel {
        return panel
    }

    private fun updateContent() {
        panel.removeAll()
        val newContent = generateContentPanel()
        val scrollPane = JBScrollPane(newContent)
        panel.add(scrollPane, BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }

    private fun generateContentPanel(): JPanel {
        return panel {
            row {
                text("#")
                text("Duration")
                text("Finished")
            }.layout(RowLayout.PARENT_GRID)
                populateBuildRows()
        }
    }

    private fun Panel.populateBuildRows() {
        buildTimesStatistics.state.buildStartTimes.mapNotNull {
            val startTime = it
            val endTime = buildTimesStatistics.state.buildEndTimes[it] ?: return@mapNotNull null
            val successful = buildTimesStatistics.state.buildSuccessful[it] ?: return@mapNotNull null
            Build(startTime, endTime, successful)
        }
            .filter { if (settings.showFailedBuilds) true else it.successful }
            .mapIndexed { index, build -> index + 1 to build }
            .reversed()
            .forEach { (index, build) ->
                row {
                    val buildTime = build.ended - build.started
                    val finishTime =
                        Instant.fromEpochMilliseconds(build.ended).toLocalDateTime(TimeZone.currentSystemDefault())
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                    text(index.toString())
                    text(formatDuration(buildTime))

                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val today = now.date
                    val yesterday = today.minus(1, DateTimeUnit.DAY)

                    val dateString = when (finishTime.date) {
                        today -> "Today,"
                        yesterday -> "Yesterday,"
                        else -> finishTime.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy,"))
                            .toString()
                    } + " " + finishTime.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        .toString()
                    text(dateString)
                    if (!build.successful) {
                        icon(AllIcons.General.Error)
                    }
                }.layout(RowLayout.PARENT_GRID)
            }
    }
}

private data class Build(
    val started: Long,
    val ended: Long,
    val successful: Boolean,
)

fun formatDuration(milliseconds: Long): String {
    return when {
        milliseconds >= 60_000 -> {
            val minutes = milliseconds / 60_000
            val seconds = (milliseconds % 60_000) / 1000
            "${minutes}m ${seconds}s"
        }

        milliseconds >= 1000 -> {
            val seconds = milliseconds / 1000
            "${seconds}s"
        }

        else -> {
            "${milliseconds}ms"
        }
    }
}