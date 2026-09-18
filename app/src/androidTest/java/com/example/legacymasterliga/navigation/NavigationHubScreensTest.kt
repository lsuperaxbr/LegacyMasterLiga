package com.example.legacymasterliga.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.legacymasterliga.core.navigation.LegacyDestination
import com.example.legacymasterliga.feature.hubs.presentation.CentralHubScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationHubScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun back_button_delegates_to_navigation_stack() {
        var wentBack = false
        composeRule.setContent {
            MaterialTheme {
                CentralHubScreen(onBack = { wentBack = true }, onOpen = {})
            }
        }

        composeRule.onNodeWithContentDescription("Voltar").performClick()

        assertTrue(wentBack)
    }

    @Test
    fun central_opens_existing_news_route() {
        var openedRoute: String? = null
        composeRule.setContent {
            MaterialTheme {
                CentralHubScreen(onBack = {}, onOpen = { openedRoute = it })
            }
        }

        composeRule.onNodeWithText("Notícias").performClick()

        assertEquals(LegacyDestination.News.route, openedRoute)
    }
}
