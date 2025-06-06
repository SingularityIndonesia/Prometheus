package com.singularityuniverse.prometheus.ui.component

import androidx.compose.runtime.Composable
import com.singularityuniverse.prometheus.entity.Project

@Composable
fun Landscape(project: Project) {
    val metadata = project.metadata
    val bias = project.biasIs.readBytes().map { it.toFloat() }
    println(bias)
    // TODO create surface plot from project.biasOs
    //  the biasOs is a file contains array of float in 4byte chuck
    //  read the value as
}