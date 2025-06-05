package com.singularityuniverse.prometheus.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize

infix fun DpSize.biggerThan(other: DpSize): Boolean {
    return width > other.width && height > other.height
}

infix fun DpSize.smallerThan(other: DpSize): Boolean {
    return width < other.width || height < other.height
}

infix fun Dp.to(other: Dp): DpSize {
    return DpSize(width = this, height = other)
}