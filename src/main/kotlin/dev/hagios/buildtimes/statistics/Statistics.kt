package dev.hagios.buildtimes.statistics

import com.intellij.openapi.components.BaseState

class Statistics: BaseState() {
    var buildStartTimes by list<Long>()
    var buildEndTimes by map<Long, Long>()
    var buildSuccessful by map<Long, Boolean>()
}