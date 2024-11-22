package dev.hagios.buildtimes.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger


@Service(Service.Level.PROJECT)
@State(
    name = "BuildTimesService", storages = [
        Storage("BuildTimesService.xml")
    ]
)
class BuildTimesService : PersistentStateComponent<Statistics>, Disposable {

    private var state = Statistics()

    override fun getState(): Statistics {
        return state
    }

    override fun loadState(state: Statistics) {
        this.state = state
    }

    fun addBuildTime(startTime: Long, endTime: Long, successful: Boolean) {
        state = state.copy(builds = state.builds + Build(startTime, endTime, successful))
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