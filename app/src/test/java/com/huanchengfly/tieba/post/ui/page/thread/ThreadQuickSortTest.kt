package com.huanchengfly.tieba.post.ui.page.thread

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadQuickSortTest {
    @Test
    fun ascendingSortOnlyEnablesDescendingShortcut() {
        val options = buildThreadQuickSortOptions(ThreadSortType.SORT_TYPE_ASC)
        val asc = options.first { it.sortType == ThreadSortType.SORT_TYPE_ASC }
        val desc = options.first { it.sortType == ThreadSortType.SORT_TYPE_DESC }

        assertTrue(asc.isSelected)
        assertFalse(asc.isEnabled)
        assertFalse(desc.isSelected)
        assertTrue(desc.isEnabled)
    }

    @Test
    fun descendingSortOnlyEnablesAscendingShortcut() {
        val options = buildThreadQuickSortOptions(ThreadSortType.SORT_TYPE_DESC)
        val asc = options.first { it.sortType == ThreadSortType.SORT_TYPE_ASC }
        val desc = options.first { it.sortType == ThreadSortType.SORT_TYPE_DESC }

        assertFalse(asc.isSelected)
        assertTrue(asc.isEnabled)
        assertTrue(desc.isSelected)
        assertFalse(desc.isEnabled)
    }
}
