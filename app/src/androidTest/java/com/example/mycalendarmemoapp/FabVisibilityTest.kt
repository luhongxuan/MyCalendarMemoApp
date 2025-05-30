package com.example.mycalendarmemoapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mycalendarmemoapp.ui.screen.FAB_TEST_TAG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FabVisibilityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addMemoFab_isVisibleOnHomeScreen() {
        // 在 HomeScreen 上尋找具有特定 Test Tag 的節點
        composeTestRule.onNodeWithTag(FAB_TEST_TAG)
            .assertExists("FAB 不存在") // 確保節點存在
            .assertIsDisplayed() // 確保節點是可見的 [cite: 3]
    }
}