package com.ugurbuga.previo.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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
        "Text", "Button", "OutlinedButton", "TextButton", "ElevatedButton", "FilledTonalButton",
        "IconButton", "Icon", "Switch", "Checkbox", "RadioButton", "Slider",
        "OutlinedTextField", "TextField",
        "HorizontalDivider", "VerticalDivider", "Spacer", "Image", "FilterChip", "AssistChip", "SuggestionChip", "InputChip",
        "TopAppBar", "CenterAlignedTopAppBar", "MediumTopAppBar", "LargeTopAppBar",
        "BottomAppBar", "NavigationBar", "NavigationRail", "FloatingActionButton", "ExtendedFloatingActionButton",
        "Badge", "BadgedBox", "CircularProgressIndicator", "LinearProgressIndicator",
        "Tab", "TabRow", "ScrollableTabRow", "DropdownMenu", "DropdownMenuItem"
    )

    private val ignoredIdentifiers = setOf(
        "Modifier", "Arrangement", "Alignment", "Color", "Dp", "DpSize", "TextStyle",
        "RoundedCornerShape", "CutCornerShape", "CircleShape", "CornerSize",
        "BorderStroke", "PaddingValues", "Brush", "FontFamily", "FontWeight", "Font",
        "MutableState", "State", "Flow", "Job", "Coroutines", "Json", "Intent", "Bundle",
        "LazyListState", "BackHandler", "LaunchedEffect", "CardDefaults", "ButtonDefaults",
        "OutlinedTextFieldDefaults", "TopAppBarDefaults", "FilterChipDefaults",
    )

    private fun isComposableName(name: String): Boolean {
        if (name.firstOrNull()?.isUpperCase() != true) return false
        if (ignoredIdentifiers.contains(name)) return false
        return true
    }

    private fun isTypeCheckOrCast(text: String, callStart: Int): Boolean {
        val prefix = text.substring(0, callStart).trimEnd()
        return prefix.endsWith(".") || prefix.endsWith("is") || prefix.endsWith("as") || prefix.endsWith("class") ||
               prefix.endsWith("object") || prefix.endsWith("typealias") || prefix.endsWith("import") ||
               prefix.endsWith("package")
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("androidx.compose.ui.tooling.preview.Preview")
            .filterIsInstance<KSFunctionDeclaration>()

        val unvalidated = symbols.filter { !it.validate() }.toList()
        val validSymbols = symbols.filter { it.validate() }.toList()

        if (validSymbols.isEmpty()) {
            return unvalidated
        }

        val composableSymbols = resolver.getSymbolsWithAnnotation("androidx.compose.runtime.Composable")
            .filterIsInstance<KSFunctionDeclaration>()
        val funcDeclMap = composableSymbols.associateBy { it.simpleName.asString() }

        val globalFuncMap = mutableMapOf<String, FunctionInfo>()
        for (kf in resolver.getAllFiles()) {
            val path = kf.filePath
            try {
                var f = File(path)
                if (!f.exists()) {
                    f = File(System.getProperty("user.dir"), path)
                }
                if (!f.exists()) {
                    var current: File? = File(System.getProperty("user.dir"))
                    while (current != null && !f.exists()) {
                        val candidate = File(current, path)
                        if (candidate.exists()) {
                            f = candidate
                            break
                        }
                        current = current.parentFile
                    }
                }
                val lines = if (f.exists()) {
                    f.readLines()
                } else {
                    emptyList()
                }
                val parsed = parseFileFunctions(lines)
                globalFuncMap.putAll(parsed)
            } catch (e: Exception) {
                logger.warn("Previo: Could not parse file $path: ${e.message}")
            }
        }

        val touchInits = StringBuilder()

        for (symbol in validSymbols) {
            val file = symbol.containingFile ?: continue
            val packageName = file.packageName.asString()
            val functionName = symbol.simpleName.asString()
            val fullName = "$packageName.$functionName"
            val safeName = functionName + "_" + packageName.replace(".", "_")

            val funcMap = globalFuncMap

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

            val rootNode = buildAstNodeTreeFromFunctionText(functionName, funcMap)
            val composableNodeCode = generateNodeCode(rootNode)

            val entryFileContent = """
                package com.ugurbuga.previo.runtime

                import androidx.compose.runtime.Composable
                import com.ugurbuga.previo.*

                internal object PrevioRegistry_$safeName {
                    init {
                        PrevioRegistry.register(
                            listOf(
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
                                    composableNode = $composableNodeCode,
                                    content = { $fullName() }
                                )
                            )
                        )
                    }
                    fun touch() {}
                }
            """.trimIndent()

            val entryFile = codeGenerator.createNewFile(
                Dependencies(true, file),
                "com.ugurbuga.previo.runtime",
                "PrevioRegistry_$safeName"
            )
            entryFile.write(entryFileContent.toByteArray())
            entryFile.close()

            touchInits.append("PrevioRegistry_$safeName.touch()\n")
        }

        val fileContent = """
            package com.ugurbuga.previo.runtime

            object GeneratedPreviewRegistry {
                init {
                    $touchInits
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

        logger.info("Previo generated registry for ${validSymbols.size} previews across separate registry files.")
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
                val bodyLines = mutableListOf<String>()
                var braceCount = 0
                var foundOpenBrace = false
                var isExpressionBody = false
                var j = i
                while (j < fileLines.size) {
                    val l = fileLines[j]
                    bodyLines.add(l)
                    for (ch in l) {
                        if (ch == '{' && !isExpressionBody) {
                            if (!foundOpenBrace) foundOpenBrace = true
                            braceCount++
                        } else if (ch == '}' && !isExpressionBody) {
                            braceCount--
                        } else if (ch == '=' && !foundOpenBrace && braceCount == 0) {
                            isExpressionBody = true
                        }
                    }
                    j++
                    if (foundOpenBrace && braceCount == 0) break
                    if (isExpressionBody && (j < fileLines.size && (fileLines[j].trimStart().startsWith("fun ") || fileLines[j].trimStart().startsWith("@")))) {
                        break
                    }
                }
                val bodyText = bodyLines.joinToString("\n")
                val bodyCode = bodyText.substringAfter('{', bodyText)
                val calledNames = Regex("""\b([a-zA-Z0-9_]+)\s*\(""").findAll(bodyCode)
                    .map { it.groupValues[1] }
                    .filter { it != funcName }
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

    // ============================================================
    // Known slot parameter names for common Compose composables.
    // These are the lambda params that act as "content slots".
    // ============================================================
    private val knownSlotParams = setOf(
        "topBar", "bottomBar", "floatingActionButton", "snackbarHost",
        "navigationIcon", "title", "actions", "content",
        "label", "leadingIcon", "trailingIcon", "supportingText",
        "indicator", "thumb", "track", "checked", "unchecked",
        "header", "footer", "placeholder", "prefix", "suffix",
        "icon", "headlineContent", "overliningContent", "leadingContent", "trailingContent",
        "item", "items"
    )

    private fun findMatchingCloseBrace(text: String, openBraceIndex: Int): Int {
        var depth = 1
        var idx = openBraceIndex + 1
        var inString = false
        var isEscaped = false
        while (idx < text.length && depth > 0) {
            val ch = text[idx]
            if (inString) {
                if (isEscaped) {
                    isEscaped = false
                } else if (ch == '\\') {
                    isEscaped = true
                } else if (ch == '"') {
                    inString = false
                }
            } else {
                if (ch == '"') {
                    inString = true
                } else if (ch == '{') {
                    depth++
                } else if (ch == '}') {
                    depth--
                }
            }
            if (depth == 0) return idx
            idx++
        }
        return idx - 1
    }

    /**
     * Extracts the first quoted string literal from a call like Text("Hello") or Text(text = "Hello")
     */
    private fun extractFirstStringLiteral(callText: String): String? {
        val parenOpen = callText.indexOf('(')
        if (parenOpen < 0) return null
        var idx = parenOpen + 1
        while (idx < callText.length && callText[idx] != '"') idx++
        if (idx >= callText.length) return null
        idx++ // skip opening quote
        val sb = StringBuilder()
        while (idx < callText.length && callText[idx] != '"') {
            if (callText[idx] == '\\' && idx + 1 < callText.length) {
                when (callText[idx + 1]) {
                    'n' -> sb.append("\n")
                    't' -> sb.append("\t")
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    else -> sb.append(callText[idx + 1])
                }
                idx += 2
            } else {
                sb.append(callText[idx])
                idx++
            }
        }
        val result = sb.toString().trim()
        return if (result.length in 1..80) result else null
    }

    /**
     * Given the full argument text of a composable call (between outer parens),
     * extracts named slot params that have lambda values: e.g. "topBar = { ... }"
     * Returns a map of slotName -> lambdaBodyText
     */
    private fun extractNamedSlotLambdas(argsText: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val slotRegex = Regex("""\b(${knownSlotParams.joinToString("|")})\s*=\s*(?:\w+@)?\{""")
        var searchStart = 0
        while (searchStart < argsText.length) {
            val m = slotRegex.find(argsText, searchStart) ?: break
            val slotName = m.groupValues[1]
            val braceStart = m.range.last // points to '{'
            val closeIndex = findMatchingCloseBrace(argsText, braceStart)
            if (closeIndex > braceStart) {
                val lambdaBody = argsText.substring(braceStart + 1, closeIndex)
                result[slotName] = lambdaBody
                searchStart = closeIndex + 1
            } else {
                searchStart = m.range.last + 1
            }
        }
        return result
    }

    private fun findMatchingCloseParen(text: String, openParenIndex: Int): Int {
        var depth = 1
        var idx = openParenIndex + 1
        var inString = false
        var isEscaped = false
        while (idx < text.length && depth > 0) {
            val ch = text[idx]
            if (inString) {
                if (isEscaped) isEscaped = false
                else if (ch == '\\') isEscaped = true
                else if (ch == '"') inString = false
            } else {
                if (ch == '"') inString = true
                else if (ch == '(') depth++
                else if (ch == ')') depth--
            }
            if (depth == 0) return idx
            idx++
        }
        return -1
    }

    private fun buildAstNodeTreeFromFunctionText(funcName: String, funcMap: Map<String, FunctionInfo>, visited: Set<String> = emptySet()): ComposableNode {
        val info = funcMap[funcName]
        if (info == null) {
            logger.warn("Previo: buildAstNodeTreeFromFunctionText info null for $funcName")
            return ComposableNode(funcName, false, emptyList())
        }
        val nextVisited = visited + funcName

        val fullText = info.bodyText
        val funIdx = fullText.indexOf("fun $funcName")
        val bodyText = if (funIdx >= 0) {
            val parenOpen = fullText.indexOf('(', funIdx)
            if (parenOpen >= 0) {
                val parenClose = findMatchingCloseParen(fullText, parenOpen)
                if (parenClose >= 0) {
                    val openBraceIdx = fullText.indexOf('{', parenClose)
                    val closeBraceIdx = if (openBraceIdx >= 0) findMatchingCloseBrace(fullText, openBraceIdx) else -1
                    if (openBraceIdx >= 0 && closeBraceIdx > openBraceIdx) {
                        fullText.substring(openBraceIdx + 1, closeBraceIdx)
                    } else {
                        val eqIdx = fullText.indexOf('=', parenClose)
                        if (eqIdx >= 0) fullText.substring(eqIdx + 1) else fullText
                    }
                } else fullText
            } else fullText
        } else fullText

        val children = parseBlockCalls(bodyText, funcMap, nextVisited, funcName)
        logger.warn("Previo AST for $funcName: bodyText length=${bodyText.length}, children count=${children.size}, children names=${children.map { it.name }}")

        return ComposableNode(funcName, false, children)
    }

    /**
     * Shared core logic to scan [text] for composable calls, extract scope, and build ComposableNode tree.
     * [slotContext] is the slot name from the parent's perspective (may be null for top-level calls).
     */
    private fun parseCallsInText(
        text: String,
        funcMap: Map<String, FunctionInfo>,
        visited: Set<String>,
        currentFuncName: String,
        slotContext: String? = null
    ): List<ComposableNode> {
        val nodes = mutableListOf<ComposableNode>()
        val callRegex = Regex("""\b([A-Z][a-zA-Z0-9_]*|item|items|itemsIndexed)\s*([({])""")

        var i = 0
        while (i < text.length) {
            val match = callRegex.find(text, i) ?: break
            val name = match.groupValues[1]
            val delimiter = match.groupValues[2]
            val callStart = match.range.first
            val delimIndex = match.range.last

            val isListItemDsl = name == "item" || name == "items" || name == "itemsIndexed"

            if (!isListItemDsl && (!isComposableName(name) || name == currentFuncName || isTypeCheckOrCast(text, callStart))) {
                i = delimIndex + 1
                continue
            }

            var argsText = ""
            var trailingLambdaBody = ""
            var scopeEnd = delimIndex + 1

            if (delimiter == "(") {
                var parenDepth = 1
                var pIndex = delimIndex + 1
                var inStr = false
                var esc = false
                while (pIndex < text.length && parenDepth > 0) {
                    val ch = text[pIndex]
                    if (inStr) {
                        if (esc) esc = false
                        else if (ch == '\\') esc = true
                        else if (ch == '"') inStr = false
                    } else {
                        if (ch == '"') inStr = true
                        else if (ch == '(') parenDepth++
                        else if (ch == ')') parenDepth--
                    }
                    pIndex++
                }
                val parenEnd = pIndex
                argsText = if (parenEnd - 1 > delimIndex + 1) text.substring(delimIndex + 1, parenEnd - 1) else ""
                scopeEnd = parenEnd

                var bIndex = parenEnd
                while (bIndex < text.length && (text[bIndex].isWhitespace() || text[bIndex] == '=' || text[bIndex] == ',')) {
                    bIndex++
                }
                if (bIndex < text.length && text[bIndex] == '{') {
                    val closeBraceIndex = findMatchingCloseBrace(text, bIndex)
                    trailingLambdaBody = text.substring(bIndex + 1, closeBraceIndex)
                    scopeEnd = closeBraceIndex + 1
                }
                i = scopeEnd
            } else {
                // delimiter == '{'
                val closeBraceIndex = findMatchingCloseBrace(text, delimIndex)
                trailingLambdaBody = text.substring(delimIndex + 1, closeBraceIndex)
                scopeEnd = closeBraceIndex + 1
                i = scopeEnd
            }

            if (isListItemDsl) {
                if (trailingLambdaBody.isNotBlank()) {
                    val itemChildren = parseCallsInText(trailingLambdaBody, funcMap, visited, currentFuncName, name)
                    nodes.addAll(itemChildren)
                }
                continue
            }

            val isNative = nativeComposables.contains(name)

            // Extract infoOrText for native leaf composables (e.g. Text("Hello"))
            val infoOrText: String? = if (isNative && argsText.isNotBlank()) {
                extractFirstStringLiteral("$name($argsText)")
            } else null

            // Extract slot children from named lambda params e.g. topBar = { ... }
            val slotChildren = mutableListOf<ComposableNode>()
            if (argsText.isNotBlank() && !isNative) {
                val slots = extractNamedSlotLambdas(argsText)
                for ((slotName, lambdaContent) in slots) {
                    val slotNodes = parseCallsInText(lambdaContent, funcMap, visited, currentFuncName, slotName)
                    slotChildren.addAll(slotNodes)
                }
            }

            // Extract children inside trailing lambda body
            val trailingChildren = if (trailingLambdaBody.isNotBlank()) {
                parseCallsInText(trailingLambdaBody, funcMap, visited, currentFuncName)
            } else emptyList()

            val targetInfo = funcMap[name]
            val finalNode = if (targetInfo != null && !visited.contains(name)) {
                val bodyNode = buildAstNodeTreeFromFunctionText(name, funcMap, visited + name)
                val mergedChildren = if (trailingChildren.isNotEmpty()) {
                    slotChildren + trailingChildren
                } else {
                    slotChildren + bodyNode.children
                }
                ComposableNode(name, isNative, mergedChildren, slotContext, infoOrText)
            } else {
                val allDirectChildren = slotChildren + trailingChildren
                ComposableNode(name, isNative, allDirectChildren, slotContext, infoOrText)
            }

            nodes.add(finalNode)
        }
        return nodes
    }

    private fun parseBlockCalls(text: String, funcMap: Map<String, FunctionInfo>, visited: Set<String>, currentFuncName: String): List<ComposableNode> {
        return parseCallsInText(text, funcMap, visited, currentFuncName)
    }

    private fun generateNodeCode(node: ComposableNode): String {
        val childrenCode = if (node.children.isNotEmpty()) {
            ", listOf(" + node.children.joinToString(", ") { generateNodeCode(it) } + ")"
        } else {
            ""
        }
        val slotCode = if (node.slotOrRole != null) ", slotOrRole = \"${node.slotOrRole}\"" else ""
        val infoCode = if (node.infoOrText != null) {
            val safeInfo = node.infoOrText
                .replace("\\", "\\\\")
                .replace("\"", "'")
                .replace("$", "\\$")
                .replace("\n", "\\n")
            ", infoOrText = \"$safeInfo\""
        } else ""
        return "ComposableNode(\"${node.name}\", ${node.isNative}$childrenCode$slotCode$infoCode)"
    }
}

class PrevioSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PrevioSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}
