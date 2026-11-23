package com.ugurbuga.previo.runtime

data class ComposableNode(
    val name: String,
    val isNative: Boolean,
    val children: List<ComposableNode> = emptyList(),
    /** e.g. "topBar", "navigationIcon", "title", "content", "bottomBar", "floatingActionButton" */
    val slotOrRole: String? = null,
    /** e.g. the literal string from Text("Hello") or a short description */
    val infoOrText: String? = null,
) {
    /** Total number of nodes in this subtree including root */
    val totalNodeCount: Int
        get() = 1 + children.sumOf { it.totalNodeCount }

    /** Maximum depth of this node tree */
    val maxDepth: Int
        get() = 1 + (children.maxOfOrNull { it.maxDepth } ?: 0)
}

/**
 * Renders this node as an ASCII tree string matching AST tree formats:
 *
 * ```
 * RatingsScreenLightPreview
 * └── TvTrackerTheme
 *     └── RatingsScreenContent
 *         └── Scaffold
 *             ├── CenterAlignedTopAppBar (topBar)
 *             │   └── ...
 *             └── Box (content)
 * ```
 */
fun ComposableNode.toAsciiTreeString(): String {
    val sb = StringBuilder()
    appendAsciiTree(sb, "", isLast = true, isRoot = true)
    return sb.toString()
}

/**
 * Renders this node hierarchy into a formatted Markdown code block or Markdown bullet tree string.
 */
fun ComposableNode.toMarkdownTreeString(asCodeBlock: Boolean = true): String {
    val asciiTree = toAsciiTreeString()
    return if (asCodeBlock) {
        "```text\n$asciiTree```"
    } else {
        val sb = StringBuilder()
        appendMarkdownBulletTree(sb, indentLevel = 0)
        sb.toString()
    }
}

private fun ComposableNode.appendAsciiTree(
    sb: StringBuilder,
    prefix: String,
    isLast: Boolean,
    isRoot: Boolean
) {
    val connector = when {
        isRoot -> ""
        isLast -> "└── "
        else -> "├── "
    }
    val label = buildString {
        append(name)
        if (!slotOrRole.isNullOrBlank()) append(" (${slotOrRole})")
        if (!infoOrText.isNullOrBlank()) append(": \"${infoOrText}\"")
    }
    sb.append(prefix).append(connector).append(label).append("\n")

    val childPrefix = when {
        isRoot -> ""
        isLast -> "$prefix    "
        else -> "$prefix│   "
    }
    children.forEachIndexed { index, child ->
        child.appendAsciiTree(
            sb = sb,
            prefix = childPrefix,
            isLast = index == children.lastIndex,
            isRoot = false
        )
    }
}

private fun ComposableNode.appendMarkdownBulletTree(
    sb: StringBuilder,
    indentLevel: Int
) {
    val indent = "  ".repeat(indentLevel)
    val label = buildString {
        append("- **").append(name).append("**")
        if (!slotOrRole.isNullOrBlank()) append(" `(").append(slotOrRole).append(")`")
        if (!infoOrText.isNullOrBlank()) append(": \"").append(infoOrText).append("\"")
    }
    sb.append(indent).append(label).append("\n")
    children.forEach { child ->
        child.appendMarkdownBulletTree(sb, indentLevel + 1)
    }
}
