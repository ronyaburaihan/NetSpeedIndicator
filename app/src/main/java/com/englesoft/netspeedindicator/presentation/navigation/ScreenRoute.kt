package com.englesoft.netspeedindicator.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenRoute {

    @Serializable
    object Onboarding : ScreenRoute()

    @Serializable
    object Main : ScreenRoute()

    @Serializable
    object Home : ScreenRoute()

    @Serializable
    object History : ScreenRoute()

    @Serializable
    object Settings : ScreenRoute()
}