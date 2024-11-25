package dev.hagios.buildtimes.extensions

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import dev.hagios.buildtimes.services.BuildTimesService
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

class BuildTimesWindowsFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(project: Project) {

        private val service = project.service<BuildTimesService>()

        fun getContent() = panel {
            row {
                text("#")
                text("Duration")
                text("Finished")
            }.layout(RowLayout.PARENT_GRID)
            service.state.builds.reversed().forEachIndexed { index, build ->
                row {
                    val buildTime = build.ended - build.started
                    val finishTime =
                        Instant.fromEpochMilliseconds(build.ended).toLocalDateTime(TimeZone.currentSystemDefault())
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                    text(index.toString())
                    text("$buildTime ms")

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
}