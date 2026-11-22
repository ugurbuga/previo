package com.ugurbuga.previo.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.File

data class FunctionInfo(
    val name: String,
    val bodyText: String,
    val calledFunctionNames: Set<String>,
    val bodyLines: List<String>
)

class PrevioSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val nativeComposables = setOf(
        "Text", "Button", "OutlinedButton", "TextButton",
        "IconButton", "Icon", "Switch", "OutlinedTextField", "TextField",
        "HorizontalDivider", "VerticalDivider", "Spacer", "Image", "FilterChip", "TopAppBar"
    )

    private val ignoredIdentifiers = setOf(
        "Modifier", "Arrangement", "Alignment", "Color", "Dp", "DpSize", "TextStyle",
        "RoundedCornerShape", "CutCornerShape", "CircleShape", "CornerSize",
        "BorderStroke", "PaddingValues", "Brush", "FontFamily", "FontWeight", "Font",
        "MutableState", "State", "Flow", "Job", "Coroutines", "Json", "Intent", "Bundle",
        "PreviewConfig", "PreviewEntry", "CategoryItem", "CardDefaults", "ButtonDefaults",
        "OutlinedTextFieldDefaults", "TopAppBarDefaults", "FilterChipDefaults"
    )

    private fun isComposableName(name: String): Boolean {
        if (name.firstOrNull()?.isUpperCase() != true) return false
        if (ignoredIdentifiers.contains(name)) return false
        if (name.endsWith("Shape") || name.endsWith("Style") || name.endsWith("Brush") ||
            name.endsWith("Color") || name.endsWith("Values") || name.endsWith("Config") ||
            name.endsWith("Entry") || name.endsWith("Defaults")) return false
        return true
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("androidx.compose.ui.tooling.preview.Preview")
            .filterIsInstance<KSFunctionDeclaration>()

        val unvalidated = symbols.filter { !it.validate() }.toList()
        val validSymbols = symbols.filter { it.validate() }.toList()

        if (validSymbols.isEmpty()) {
            return unvalidated
        }

        // Cache file function maps for recursive delegation resolution
        val fileFunctionMaps = mutableMapOf<String, Map<String, FunctionInfo>>()

        val entriesCode = StringBuilder()

        for (symbol in validSymbols) {
            val file = symbol.containingFile
            val packageName = file?.packageName?.asString() ?: continue
            val functionName = symbol.simpleName.asString()
            val fullName = "$packageName.$functionName"

            val filePath = (symbol.location as? FileLocation)?.filePath
            val funcMap = if (filePath != null) {
                fileFunctionMaps.getOrPut(filePath) {
                    try {
                        parseFileFunctions(File(filePath).readLines())
                    } catch (_: Exception) {
                        emptyMap()
                    }
                }
            } else {
                emptyMap()
            }

            val funcInfo = funcMap[functionName]

            // Get 'name' and 'backgroundColor' arguments from @Preview annotation
            var previewName = ""
            var backgroundColorVal: Long? = null
            for (annotation in symbol.annotations) {
                if (annotation.annotationType.resolve().declaration.qualifiedName?.asString()?.contains("Preview") == true) {
                    for (arg in annotation.arguments) {
                        if (arg.name?.asString() == "name") {
                            previewName = arg.value?.toString() ?: ""
                        }
                        if (arg.name?.asString() == "backgroundColor") {
                            backgroundColorVal = (arg.value as? Number)?.toLong()
                        }
                    }
                }
            }

            if (previewName.isBlank()) {
                previewName = functionName
            }

            // Parse 4-part format: [GROUP] [ORDER] [ALPHABETICAL_ORDER] [CUSTOM_INFO]
            val regex = Regex("""^\[(.*?)\]\s*\[(.*?)\]\s*\[(.*?)\]\s*(?:\[(.*?)\])?$""")
            val match = regex.matchEntire(previewName.trim())

            val group: String
            val order: Int
            val alphabeticalOrder: String
            val customInfo: String

            if (match != null) {
                group = match.groupValues[1].trim()
                order = match.groupValues[2].trim().toIntOrNull() ?: 0
                alphabeticalOrder = match.groupValues[3].trim()
                customInfo = match.groupValues[4].trim()
            } else {
                group = ""
                order = 0
                alphabeticalOrder = ""
                customInfo = ""
            }

            val resolvedBody = resolveRecursiveBody(functionName, funcMap)

            val isVerticallyScrollable = resolvedBody.contains("LazyColumn") ||
                                         resolvedBody.contains("LazyVerticalGrid") ||
                                         resolvedBody.contains("verticalScroll")

            val isHorizontallyScrollable = resolvedBody.contains("LazyRow") ||
                                           resolvedBody.contains("LazyHorizontalGrid") ||
                                           resolvedBody.contains("horizontalScroll")

            val components = mutableListOf<String>()
            if (resolvedBody.contains("LazyColumn") || resolvedBody.contains("LazyVerticalGrid")) {
                components.add("LazyColumn")
            }
            if (resolvedBody.contains("LazyRow") || resolvedBody.contains("LazyHorizontalGrid")) {
                components.add("LazyRow")
            }
            if (resolvedBody.contains("Column")) {
                if (isVerticallyScrollable) {
                    components.add("Column (scrollable)")
                } else {
                    components.add("Column")
                }
            }
            if (resolvedBody.contains("Row")) {
                if (isHorizontallyScrollable) {
                    components.add("Row (scrollable)")
                } else {
                    components.add("Row")
                }
            }
            if (resolvedBody.contains("Card")) {
                components.add("Card")
            }
            if (resolvedBody.contains("Button")) {
                components.add("Button")
            }
            if (resolvedBody.contains("OutlinedTextField") || resolvedBody.contains("TextField")) {
                components.add("TextField")
            }
            if (resolvedBody.contains("Surface")) {
                components.add("Surface")
            }

            val detectedCustomInfo = if (components.isNotEmpty()) {
                components.distinct().joinToString(", ")
            } else {
                customInfo.ifBlank { "Default" }
            }

            val finalPreviewName = if (group.isNotBlank()) {
                "[$group] [$order] [$alphabeticalOrder] [$detectedCustomInfo]"
            } else {
                previewName
            }

            val bgCode = if (backgroundColorVal != null) "${backgroundColorVal}L" else "null"
            val callTreeText = buildFormattedCallTreeFromFunction(functionName, funcMap)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")

            val rootNode = buildLexicalNodeTreeFromFunction(functionName, funcMap)
            val composableNodeCode = generateNodeCode(rootNode)

            entriesCode.append("""
                PreviewEntry(
                    id = "$fullName",
                    name = "$finalPreviewName",
                    group = "$group",
                    order = $order,
                    alphabeticalOrder = "$alphabeticalOrder",
                    customInfo = "$detectedCustomInfo",
                    isVerticallyScrollable = $isVerticallyScrollable,
                    isHorizontallyScrollable = $isHorizontallyScrollable,
                    backgroundColor = $bgCode,
                    callTree = "$callTreeText",
                    composableNode = $composableNodeCode,
                    content = { $fullName() }
                ),
            """.trimIndent())
        }

        val fileContent = """
            package com.ugurbuga.previo.runtime

            import androidx.compose.runtime.Composable
            import com.ugurbuga.previo.*

            object GeneratedPreviewRegistry {
                init {
                    PrevioRegistry.register(
                        listOf(
                            $entriesCode
                        )
                    )
                }

                fun touch() {}
            }
        """.trimIndent()

        val file = codeGenerator.createNewFile(
            Dependencies(true, *validSymbols.mapNotNull { it.containingFile }.toTypedArray()),
            "com.ugurbuga.previo.runtime",
            "GeneratedPreviewRegistry"
        )
        file.write(fileContent.toByteArray())
        file.close()

        logger.info("Previo generated registry for ${validSymbols.size} previews with recursive delegation scope ComposableNode trees.")
        return unvalidated
    }

    private fun parseFileFunctions(fileLines: List<String>): Map<String, FunctionInfo> {
        val result = mutableMapOf<String, FunctionInfo>()
        var i = 0
        while (i < fileLines.size) {
            val line = fileLines[i]
            val nameMatch = Regex("""\bfun\s+([a-zA-Z0-9_]+)\s*\(""").find(line)
            if (nameMatch != null) {
                val funcName = nameMatch.groupValues[1]
                val bodySb = StringBuilder()
                val bodyLines = mutableListOf<String>()
                var braceCount = 0
                var started = false
                var j = i
                while (j < fileLines.size) {
                    val l = fileLines[j]
                    bodyLines.add(l)
                    for (char in l) {
                        if (char == '{') {
                            if (!started) started = true
                            braceCount++
                        } else if (char == '}') {
                            braceCount--
                        }
                    }
                    bodySb.append(l).append("\n")
                    j++
                    if (started && braceCount == 0) break
                }
                val bodyText = bodySb.toString()
                val calledNames = Regex("""\b([a-zA-Z0-9_]+)\s*\(""").findAll(bodyText)
                    .map { it.groupValues[1] }
                    .toSet()

                result[funcName] = FunctionInfo(funcName, bodyText, calledNames, bodyLines)
                i = j - 1
            }
            i++
        }
        return result
    }

    private fun resolveRecursiveBody(funcName: String, funcMap: Map<String, FunctionInfo>, visited: MutableSet<String> = mutableSetOf()): String {
        if (!visited.add(funcName)) return ""
        val info = funcMap[funcName] ?: return ""
        val sb = StringBuilder(info.bodyText)
        for (called in info.calledFunctionNames) {
            if (called != funcName && funcMap.containsKey(called)) {
                sb.append("\n").append(resolveRecursiveBody(called, funcMap, visited))
            }
        }
        return sb.toString()
    }

    private fun buildLexicalNodeTreeFromFunction(funcName: String, funcMap: Map<String, FunctionInfo>, visited: MutableSet<String> = mutableSetOf()): ComposableNode {
        val info = funcMap[funcName]
        if (info == null || !visited.add(funcName)) {
            return ComposableNode(funcName, false, emptyList())
        }

        val rootNode = ComposableNode(funcName, false, mutableListOf())
        val stack = mutableListOf<MutableList<ComposableNode>>()
        stack.add(rootNode.children as MutableList<ComposableNode>)

        var pendingContainerName: String? = null
        val singleCallRegex = Regex("""\b([A-Z][a-zA-Z0-9_]*)\s*\(""")

        var braceDepth = 0

        for (line in info.bodyLines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("//") || trimmed.startsWith("/*")) continue

            if (pendingContainerName != null) {
                if (trimmed.contains("{")) {
                    val name = pendingContainerName
                    pendingContainerName = null
                    val isNative = nativeComposables.contains(name)
                    val newNode = ComposableNode(name, isNative, mutableListOf())
                    stack.last().add(newNode)
                    stack.add(newNode.children as MutableList<ComposableNode>)
                } else if (!trimmed.startsWith("modifier") && !trimmed.startsWith(".") &&
                           !trimmed.startsWith("verticalArrangement") && !trimmed.startsWith("horizontalArrangement") &&
                           !trimmed.startsWith("contentAlignment") && !trimmed.startsWith("shape") &&
                           !trimmed.startsWith("colors") && !trimmed.startsWith("label")) {
                    pendingContainerName = null
                }
            }

            val matches = singleCallRegex.findAll(trimmed)
            for (m in matches) {
                val name = m.groupValues[1]
                if (isComposableName(name) && name != funcName) {
                    if (funcMap.containsKey(name)) {
                        val delegatedNode = buildLexicalNodeTreeFromFunction(name, funcMap, visited.toMutableSet())
                        stack.last().add(delegatedNode)
                    } else {
                        val isContainer = !nativeComposables.contains(name)
                        if (isContainer) {
                            pendingContainerName = name
                        } else {
                            stack.last().add(ComposableNode(name, true, emptyList()))
                        }
                    }
                }
            }

            for (char in trimmed) {
                if (char == '{') {
                    braceDepth++
                } else if (char == '}') {
                    braceDepth--
                    if (stack.size > 1) {
                        stack.removeAt(stack.size - 1)
                    }
                }
            }
        }
        return if (rootNode.children.size == 1) rootNode.children.first() else rootNode
    }

    private fun buildFormattedCallTreeFromFunction(funcName: String, funcMap: Map<String, FunctionInfo>): String {
        val root = buildLexicalNodeTreeFromFunction(funcName, funcMap)
        return formatNodeTree(root)
    }

    private fun formatNodeTree(node: ComposableNode, indent: String = ""): String {
        val sb = StringBuilder()
        sb.append("$indent├── ${node.name}\n")
        for (child in node.children) {
            sb.append(formatNodeTree(child, "$indent│   "))
        }
        return sb.toString()
    }

    private fun generateNodeCode(node: ComposableNode): String {
        val childrenCode = if (node.children.isNotEmpty()) {
            ", listOf(" + node.children.joinToString(", ") { generateNodeCode(it) } + ")"
        } else {
            ""
        }
        return "ComposableNode(\"${node.name}\", ${node.isNative}$childrenCode)"
    }
}

class PrevioSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PrevioSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}
