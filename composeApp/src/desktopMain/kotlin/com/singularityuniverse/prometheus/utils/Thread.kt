package com.singularityuniverse.prometheus.utils

import androidx.annotation.MainThread

@MainThread
fun runInMainThread(bloc: () -> Unit) {
    bloc.invoke()
}