package com.ugurbuga.previo.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposableNodeAsciiTreeTest {

    // ---- Helpers ----------------------------------------------------------------

    private fun node(
        name: String,
        isNative: Boolean = false,
        slot: String? = null,
        info: String? = null,
        vararg children: ComposableNode
    ) = ComposableNode(
        name = name,
        isNative = isNative,
        children = children.toList(),
        slotOrRole = slot,
        infoOrText = info
    )

    // ---- Tests ------------------------------------------------------------------

    @Test
    fun singleRootNodeProducesJustName() {
        val root = node("MyScreen")
        val tree = root.toAsciiTreeString()
        assertEquals("MyScreen\n", tree)
    }

    @Test
    fun singleChildUsesLastConnector() {
        val root = node("Root", children = arrayOf(node("Child")))
        val tree = root.toAsciiTreeString()
        val expected = """
            Root
            └── Child
        """.trimIndent() + "\n"
        assertEquals(expected, tree)
    }

    @Test
    fun multipleChildrenUseCorrectConnectors() {
        val root = node(
            "Scaffold",
            children = arrayOf(
                node("TopBar", slot = "topBar"),
                node("BottomBar", slot = "bottomBar"),
                node("Box", slot = "content")
            )
        )
        val tree = root.toAsciiTreeString()
        // First two children use ├──, last uses └──
        assertTrue(tree.contains("├── TopBar (topBar)"))
        assertTrue(tree.contains("├── BottomBar (bottomBar)"))
        assertTrue(tree.contains("└── Box (content)"))
    }

    @Test
    fun deepNestingProducesCorrectPrefixes() {
        val root = node(
            "RatingsScreenLightPreview",
            children = arrayOf(
                node("TvTrackerTheme",
                    children = arrayOf(
                        node("Scaffold",
                            children = arrayOf(
                                node("TopBar", slot = "topBar"),
                                node("Column", slot = "content",
                                    children = arrayOf(
                                        node("Text", isNative = true, info = "Hello"),
                                        node("Text", isNative = true, info = "World")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val tree = root.toAsciiTreeString()
        println("=== ASCII TREE ===\n$tree")

        // Root
        assertTrue(tree.startsWith("RatingsScreenLightPreview\n"))
        // Level 1
        assertTrue(tree.contains("└── TvTrackerTheme"))
        // Level 2
        assertTrue(tree.contains("    └── Scaffold"))
        // Level 3 — TopBar has sibling so uses ├──
        assertTrue(tree.contains("        ├── TopBar (topBar)"))
        // Level 3 — Column is last child so uses └──
        assertTrue(tree.contains("        └── Column (content)"))
        // Level 4 — Text with info
        assertTrue(tree.contains("\"Hello\""))
        assertTrue(tree.contains("\"World\""))
    }

    @Test
    fun slotOrRoleAppearsInParentheses() {
        val root = node("Parent",
            children = arrayOf(
                node("Child", slot = "navigationIcon")
            )
        )
        val tree = root.toAsciiTreeString()
        assertTrue(tree.contains("Child (navigationIcon)"))
    }

    @Test
    fun infoOrTextAppearsQuoted() {
        val root = node("Text", isNative = true, info = "Reytingler")
        val tree = root.toAsciiTreeString()
        assertTrue(tree.contains("\"Reytingler\""))
    }

    @Test
    fun slotAndInfoBothAppear() {
        val root = node("Parent",
            children = arrayOf(
                node("Text", isNative = true, slot = "title", info = "Reytingler")
            )
        )
        val tree = root.toAsciiTreeString()
        assertTrue(tree.contains("Text (title): \"Reytingler\""))
    }

    @Test
    fun ratingScreenExampleMatchesExpected() {
        val root = node(
            "RatingsScreenLightPreview",
            children = arrayOf(
                node("TvTrackerTheme",
                    children = arrayOf(
                        node("RatingsScreenContent",
                            children = arrayOf(
                                node("Scaffold",
                                    children = arrayOf(
                                        node("CenterAlignedTopAppBar", slot = "topBar",
                                            children = arrayOf(
                                                node("DebugThemeToggleButton", slot = "navigationIcon"),
                                                node("Column", slot = "title",
                                                    children = arrayOf(
                                                        node("Text", isNative = true, info = "REYTİNG"),
                                                        node("Text", isNative = true, info = "Reytingler")
                                                    )
                                                ),
                                                node("HorizontalDivider", isNative = true)
                                            )
                                        ),
                                        node("Box", slot = "content",
                                            children = arrayOf(
                                                node("LazyColumn")
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val tree = root.toAsciiTreeString()
        println("=== RATING SCREEN ASCII TREE ===\n$tree")
        assertTrue(tree.contains("RatingsScreenLightPreview"))
        assertTrue(tree.contains("TvTrackerTheme"))
        assertTrue(tree.contains("CenterAlignedTopAppBar (topBar)"))
        assertTrue(tree.contains("DebugThemeToggleButton (navigationIcon)"))
        assertTrue(tree.contains("Column (title)"))
        assertTrue(tree.contains("\"REYTİNG\""))
        assertTrue(tree.contains("\"Reytingler\""))
        assertTrue(tree.contains("Box (content)"))
    }

    @Test
    fun nodeMetricsCalculateCorrectly() {
        val root = node(
            "Root",
            children = arrayOf(
                node("Child1", children = arrayOf(node("Leaf1", isNative = true))),
                node("Child2")
            )
        )
        assertEquals(4, root.totalNodeCount)
        assertEquals(3, root.maxDepth)
    }

    @Test
    fun markdownFormattingProducesValidOutput() {
        val root = node(
            "Screen",
            children = arrayOf(
                node("TopAppBar", slot = "topBar"),
                node("Text", isNative = true, slot = "title", info = "TitleText")
            )
        )
        val codeBlockMarkdown = root.toMarkdownTreeString(asCodeBlock = true)
        assertTrue(codeBlockMarkdown.startsWith("```text\n"))
        assertTrue(codeBlockMarkdown.endsWith("```"))
        assertTrue(codeBlockMarkdown.contains("TopAppBar (topBar)"))

        val bulletMarkdown = root.toMarkdownTreeString(asCodeBlock = false)
        assertTrue(bulletMarkdown.contains("- **Screen**"))
        assertTrue(bulletMarkdown.contains("  - **TopAppBar** `(topBar)`"))
        assertTrue(bulletMarkdown.contains("  - **Text** `(title)`: \"TitleText\""))
    }
}
