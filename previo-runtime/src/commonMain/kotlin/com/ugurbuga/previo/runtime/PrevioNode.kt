package com.ugurbuga.previo.runtime

data class PrevioNode(
    val id: String,
    val name: String,
    val kind: NodeKind,
    val slotOrRole: String? = null,
    val infoOrText: String? = null,
    val children: List<PrevioNode> = emptyList(),
)

enum class NodeKind {
    Container,
    Leaf,
    Unknown,
}

fun ComposableNode.toPrevioNode(idPrefix: String = "node"): PrevioNode {
    val kind = when {
        isNative -> NodeKind.Leaf
        children.isNotEmpty() -> NodeKind.Container
        else -> NodeKind.Unknown
    }
    return PrevioNode(
        id = idPrefix,
        name = name,
        kind = kind,
        slotOrRole = slotOrRole,
        infoOrText = infoOrText,
        children = children.mapIndexed { index, child -> child.toPrevioNode("$idPrefix-$index") }
    )
}

/**
 * Renders this node as an ASCII tree string similar to:
 *
 * ```
 * RatingsScreenLightPreview
 * └── TvTrackerTheme
 *     └── RatingsScreenContent
 *         └── Scaffold
 *             ├── CenterAlignedTopAppBar (topBar)
 *             └── Box (content)
 * ```
 */
fun PrevioNode.toAsciiTreeString(): String {
    val sb = StringBuilder()
    appendAsciiTree(sb, "", isLast = true, isRoot = true)
    return sb.toString()
}

private fun PrevioNode.appendAsciiTree(
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
