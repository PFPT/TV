package com.pfpt.tv.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StoragePathsTest {

    @Test
    fun confineKeepsRelativeFileInsideRoot() {
        val root = File("build/storage-paths-root").apply { mkdirs() }
        val file = StoragePaths.confine(root, "/Movies/a.mp4")
        assertTrue(StoragePaths.contains(root.canonicalFile, file))
        assertTrue(file.path.endsWith("Movies/a.mp4") || file.path.endsWith("Movies\\a.mp4"))
    }

    @Test
    fun confineRejectsParentTraversal() {
        val root = File("build/storage-paths-root").apply { mkdirs() }
        assertNull(StoragePaths.confine(root, "../../etc/passwd"))
        assertNull(StoragePaths.confine(root, "/../../etc/passwd"))
        assertNull(StoragePaths.confine(root, "file:/../../etc/passwd"))
    }

    @Test
    fun confineRejectsNullBase() {
        assertNull(StoragePaths.confine(null, "a"))
    }

    @Test
    fun jsEscapesQuoteAndScriptBreakout() {
        assertEquals("a\\\"b\\\\c", StoragePaths.js("a\"b\\c"))
        assertFalse(StoragePaths.js("</script>").contains("<"))
        assertEquals("", StoragePaths.js(null))
    }
}
