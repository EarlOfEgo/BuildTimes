package dev.hagios.buildtimes.listeners

import com.intellij.build.BuildProgressListener
import com.intellij.build.BuildViewManager
import com.intellij.build.events.*
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import dev.hagios.buildtimes.statistics.BuildTimesStatistics

class BuildListener(
    project: Project
) : ExecutionListener, BuildProgressListener {

    private val buildTimesStatistics = project.service<BuildTimesStatistics>()

    init {
        val buildViewManager = project.service<BuildViewManager>()
        buildViewManager.addListener(this, buildTimesStatistics)
    }

    override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
        super.processStarted(executorId, env, handler)
        thisLogger().info("Build process started: $executorId")
    }

    private val buildStarts = mutableMapOf<Any, Long>()

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        super.processTerminated(executorId, env, handler, exitCode)
        thisLogger().info("Build process terminated: $executorId with exit code $exitCode")
    }

    override fun onEvent(buildId: Any, event: BuildEvent) {
        if (event is StartBuildEvent) {
            buildStarts[buildId] = event.eventTime
        }
        if (event is FinishBuildEvent) {
            val buildStart = buildStarts[buildId]
            val buildEnd = event.eventTime
            when (event.result) {
               is FailureResult -> buildStart?.let { buildTimesStatistics.addBuildTime(it, buildEnd, false) }
               is SuccessResult -> buildStart?.let { buildTimesStatistics.addBuildTime(it, buildEnd, true) }
            }
        }
    }
}