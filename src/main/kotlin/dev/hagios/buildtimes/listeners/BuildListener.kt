package dev.hagios.buildtimes.listeners

import com.intellij.build.BuildProgressListener
import com.intellij.build.BuildViewManager
import com.intellij.build.events.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import dev.hagios.buildtimes.services.BuildTimesService

class BuildListener(
    project: Project
) : BuildProgressListener {

    private val buildTimesService = project.service<BuildTimesService>()

    init {
        val buildViewManager = project.service<BuildViewManager>()
        buildViewManager.addListener(this, buildTimesService)
    }

    private val buildStarts = mutableMapOf<Any, Long>()

    override fun onEvent(buildId: Any, event: BuildEvent) {
        if (event is StartBuildEvent) {
            buildStarts[buildId] = event.eventTime
        }
        if (event is FinishBuildEvent) {
            val buildStart = buildStarts[buildId]
            val buildEnd = event.eventTime
            when (event.result) {
                is FailureResult -> buildTimesService.addBuildTime(buildStart!!, buildEnd, false)
                is SuccessResult -> buildTimesService.addBuildTime(buildStart!!, buildEnd, true)
            }
        }
    }
}