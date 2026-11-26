package com.TallerJC.motogest.ui.util

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLayoutTest {

    @Test
    fun testCompactMapsToBottomNavAndSinglePane() {
        val state = MotoGestAdaptiveState.fromWidthSizeClass(
            widthSizeClass = WindowWidthSizeClass.Compact,
            heightSizeClass = WindowHeightSizeClass.Medium
        )
        assertEquals(MotoGestNavigationType.BOTTOM_NAVIGATION, state.navigationType)
        assertEquals(MotoGestContentType.SINGLE_PANE, state.contentType)
        assertTrue(state.isCompact)
        assertFalse(state.isDualPane)
        assertFalse(state.isCompactHeight)
    }

    @Test
    fun testMediumMapsToNavigationRailAndDualPane() {
        val state = MotoGestAdaptiveState.fromWidthSizeClass(
            widthSizeClass = WindowWidthSizeClass.Medium,
            heightSizeClass = WindowHeightSizeClass.Medium
        )
        assertEquals(MotoGestNavigationType.NAVIGATION_RAIL, state.navigationType)
        assertEquals(MotoGestContentType.DUAL_PANE, state.contentType)
        assertTrue(state.isMedium)
        assertTrue(state.isDualPane)
        assertFalse(state.isCompactHeight)
    }

    @Test
    fun testPhoneLandscapeDetectsCompactHeight() {
        val state = MotoGestAdaptiveState.fromWidthSizeClass(
            widthSizeClass = WindowWidthSizeClass.Medium,
            heightSizeClass = WindowHeightSizeClass.Compact
        )
        assertEquals(MotoGestNavigationType.NAVIGATION_RAIL, state.navigationType)
        assertTrue(state.isDualPane)
        assertTrue(state.isCompactHeight)
    }

    @Test
    fun testExpandedMapsToPermanentDrawerAndDualPane() {
        val state = MotoGestAdaptiveState.fromWidthSizeClass(
            widthSizeClass = WindowWidthSizeClass.Expanded,
            heightSizeClass = WindowHeightSizeClass.Expanded
        )
        assertEquals(MotoGestNavigationType.PERMANENT_NAVIGATION_DRAWER, state.navigationType)
        assertEquals(MotoGestContentType.DUAL_PANE, state.contentType)
        assertTrue(state.isExpanded)
        assertTrue(state.isDualPane)
        assertFalse(state.isCompactHeight)
    }
}
