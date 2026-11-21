package com.ugurbuga.previo.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.File

data class FunctionInfo(
    val name: String,
    val bodyText: String,
    val calledFunctionNames: Set<String>
)

class PrevioSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

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

            // Get 'name' argument from @Preview annotation
            var previewName = ""
            for (annotation in symbol.annotations) {
                if (annotation.annotationType.resolve().declaration.qualifiedName?.asString()?.contains("Preview") == true) {
                    for (arg in annotation.arguments) {
                        if (arg.name?.asString() == "name") {
                            previewName = arg.value?.toString() ?: ""
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

            // Recursive AST / Call Graph Analysis
            val resolvedBody = resolveRecursiveBody(functionName, funcMap)

            val isVerticallyScrollable = resolvedBody.contains("LazyColumn") ||
                                         resolvedBody.contains("LazyVerticalGrid") ||
                                         resolvedBody.contains("verticalScroll")

            val isHorizontallyScrollable = resolvedBody.contains("LazyRow") ||
                                           resolvedBody.contains("LazyHorizontalGrid") ||
                                           resolvedBody.contains("horizontalScroll")

            // Detect native composables for 4th parameter
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

        logger.info("Previo generated registry for ${validSymbols.size} previews with recursive call graph scroll detection.")
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
                var braceCount = 0
                var started = false
                var j = i
                while (j < fileLines.size) {
                    val l = fileLines[j]
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

                result[funcName] = FunctionInfo(funcName, bodyText, calledNames)
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
}

class PrevioSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PrevioSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}
