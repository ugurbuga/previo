package com.ugurbuga.previo.runtime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.ugurbuga.previo_runtime.generated.resources.Res
import io.github.ugurbuga.previo_runtime.generated.resources.ic_arrow_back
import io.github.ugurbuga.previo_runtime.generated.resources.ic_chevron_down
import io.github.ugurbuga.previo_runtime.generated.resources.ic_chevron_right
import io.github.ugurbuga.previo_runtime.generated.resources.ic_close
import io.github.ugurbuga.previo_runtime.generated.resources.ic_filter_list
import io.github.ugurbuga.previo_runtime.generated.resources.ic_search
import io.github.ugurbuga.previo_runtime.generated.resources.previo_all
import io.github.ugurbuga.previo_runtime.generated.resources.previo_back
import io.github.ugurbuga.previo_runtime.generated.resources.previo_clear
import io.github.ugurbuga.previo_runtime.generated.resources.previo_filter
import io.github.ugurbuga.previo_runtime.generated.resources.previo_four_column_format
import io.github.ugurbuga.previo_runtime.generated.resources.previo_no_previews_found
import io.github.ugurbuga.previo_runtime.generated.resources.previo_search_placeholder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class CategoryItem(
    val group: String,
    val minOrder: Int,
    val count: Int
)

enum class PreviewViewMode {
    FORMATTED,
    ALL
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

private fun isPreviewFormatted(name: String): Boolean {
    return try {
        PreviewNameParser.parse(name)
        true
    } catch (_: Exception) {
        false
    }
}

@Composable
fun PrevioTheme(
    config: PrevioConfig,
    content: @Composable () -> Unit
) {
    val colors = config.getColors()

    val customColorScheme = config.colorScheme?.copy(
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
        outline = colors.border,
        outlineVariant = colors.border
    ) ?: MaterialTheme.colorScheme.copy(
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
        outline = colors.border,
        outlineVariant = colors.border
    )

    MaterialTheme(
        colorScheme = customColorScheme,
        typography = config.typography ?: MaterialTheme.typography,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreviewExplorer(
    onBack: () -> Unit,
    config: PrevioConfig = PrevioConfig()
) {
    if (!config.enabled) {
        return
    }

    val rawPreviews = remember {
        loadGeneratedRegistry()
        PrevioRegistry.previews
    }

    val previews = remember(rawPreviews, config.collectPreviews) {
        if (config.collectPreviews) rawPreviews else emptyList()
    }

    SetStatusBarColor(isDarkTheme = config.isDarkTheme)

    PrevioTheme(config = config) {
        val colors = config.getColors()
        var viewMode by remember { mutableStateOf(PreviewViewMode.FORMATTED) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var selectedPreviewIndex by remember { mutableStateOf<Int?>(null) }

        val modeFilteredPreviews = remember(previews, viewMode) {
            when (viewMode) {
                PreviewViewMode.FORMATTED -> previews.filter { isPreviewFormatted(it.name) }
                PreviewViewMode.ALL -> previews
            }
        }

        val filteredPreviews = remember(modeFilteredPreviews, searchQuery) {
            if (searchQuery.isBlank()) {
                modeFilteredPreviews
            } else {
                modeFilteredPreviews.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.group.contains(searchQuery, ignoreCase = true) ||
                            it.customInfo.contains(searchQuery, ignoreCase = true) ||
                            it.alphabeticalOrder.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        val categories = remember(filteredPreviews) {
            filteredPreviews
                .groupBy { it.group.ifBlank { "Unformatted" } }
                .map { (group, entries) ->
                    val minOrder = entries.minOfOrNull { it.order } ?: 0
                    CategoryItem(group = group, minOrder = minOrder, count = entries.size)
                }
                .sortedWith(compareBy<CategoryItem> { it.minOrder }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.group })
        }

        val categoryPreviews = remember(filteredPreviews, selectedCategory) {
            if (selectedCategory == null) {
                emptyList()
            } else {
                filteredPreviews
                    .filter {
                        (it.group.ifBlank { "Unformatted" }).equals(
                            selectedCategory,
                            ignoreCase = true
                        )
                    }
                    .sortedWith(
                        compareBy<PreviewEntry> { it.order }
                            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.alphabeticalOrder }
                    )
            }
        }

        if (selectedCategory != null && selectedPreviewIndex != null) {
            PreviewDetailPagerScreen(
                previews = categoryPreviews,
                initialIndex = selectedPreviewIndex!!,
                onBack = { selectedPreviewIndex = null },
                config = config
            )
            return@PrevioTheme
        }

        if (selectedCategory != null) {
            CategoryItemsScreen(
                categoryName = selectedCategory!!,
                previews = categoryPreviews,
                onBack = { selectedCategory = null },
                onPreviewClick = { index -> selectedPreviewIndex = index },
                config = config
            )
            return@PrevioTheme
        }

        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            config.title,
                            style = config.typography?.titleMedium ?: MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.noRippleClickable { onBack() }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = stringResource(Res.string.previo_back),
                                tint = colors.icon
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_filter_list),
                        contentDescription = stringResource(Res.string.previo_filter),
                        modifier = Modifier.size(18.dp),
                        tint = colors.icon
                    )
                    FilterChip(
                        selected = viewMode == PreviewViewMode.FORMATTED,
                        onClick = { viewMode = PreviewViewMode.FORMATTED },
                        label = { Text(stringResource(Res.string.previo_four_column_format) + " (${previews.count { isPreviewFormatted(it.name) }})", color = colors.textPrimary) },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, colors.border),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surface,
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.textPrimary
                        ),
                        modifier = Modifier.noRippleClickable { viewMode = PreviewViewMode.FORMATTED }
                    )
                    FilterChip(
                        selected = viewMode == PreviewViewMode.ALL,
                        onClick = { viewMode = PreviewViewMode.ALL },
                        label = { Text(stringResource(Res.string.previo_all) + " (${previews.size})", color = colors.textPrimary) },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, colors.border),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surface,
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.textPrimary
                        ),
                        modifier = Modifier.noRippleClickable { viewMode = PreviewViewMode.ALL }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.previo_search_placeholder), color = colors.textSecondary) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = stringResource(Res.string.previo_search_placeholder),
                            tint = colors.icon
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.noRippleClickable { searchQuery = "" }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_close),
                                    contentDescription = stringResource(Res.string.previo_clear),
                                    tint = colors.icon
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(Res.string.previo_no_previews_found),
                            style = config.typography?.bodyLarge ?: MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(categories, key = { _, cat -> cat.group }) { _, category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable { selectedCategory = category.group },
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surface
                                ),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = category.group,
                                            style = config.typography?.titleMedium ?: MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${category.count} Previews",
                                            style = config.typography?.labelSmall ?: MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.surface,
                                        border = BorderStroke(1.dp, colors.border)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_chevron_right),
                                            contentDescription = "Navigate",
                                            modifier = Modifier.padding(8.dp),
                                            tint = colors.icon
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryItemsScreen(
    categoryName: String,
    previews: List<PreviewEntry>,
    onBack: () -> Unit,
    onPreviewClick: (Int) -> Unit,
    config: PrevioConfig = PrevioConfig()
) {
    PrevioTheme(config = config) {
        val colors = config.getColors()
        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            categoryName,
                            style = config.typography?.titleMedium ?: MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.noRippleClickable { onBack() }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = colors.icon
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (previews.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No previews in this category.", color = colors.textSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(previews, key = { _, entry -> entry.id }) { index, entry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable { onPreviewClick(index) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surface
                                ),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.alphabeticalOrder.ifBlank { entry.name },
                                            style = config.typography?.titleSmall ?: MaterialTheme.typography.titleSmall,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = entry.customInfo.ifBlank { "Default" },
                                            style = config.typography?.labelSmall ?: MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.surface,
                                        border = BorderStroke(1.dp, colors.border)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_chevron_right),
                                            contentDescription = "View",
                                            modifier = Modifier.padding(8.dp),
                                            tint = colors.icon
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreviewDetailPagerScreen(
    previews: List<PreviewEntry>,
    initialIndex: Int,
    onBack: () -> Unit,
    config: PrevioConfig = PrevioConfig()
) {
    PrevioTheme(config = config) {
        val colors = config.getColors()
        var currentIndex by remember { mutableStateOf(initialIndex) }
        var showTreeView by remember { mutableStateOf(false) }
        val currentPreview = previews.getOrNull(currentIndex)

        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                (currentPreview?.alphabeticalOrder?.ifBlank { currentPreview.name }) ?: "Preview",
                                style = config.typography?.titleMedium ?: MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            if (currentPreview != null) {
                                if (currentPreview.group.isNotBlank()) {
                                    Text(
                                        text = "Category: ${currentPreview.group}",
                                        style = config.typography?.labelSmall ?: MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                                if (currentPreview.customInfo.isNotBlank()) {
                                    Text(
                                        text = "Info: ${currentPreview.customInfo}",
                                        style = config.typography?.labelSmall ?: MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.noRippleClickable { onBack() }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = colors.icon
                            )
                        }
                    },
                    actions = {
                        if (config.collectHierarchyTree && currentPreview != null && currentPreview.composableNode != null) {
                            IconButton(
                                onClick = { showTreeView = !showTreeView },
                                modifier = Modifier.noRippleClickable { showTreeView = !showTreeView }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_filter_list),
                                    contentDescription = "Toggle Function Tree",
                                    tint = colors.icon
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )
            }
        ) { paddingValues ->
            if (currentPreview != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        PreviewContentContainer(preview = currentPreview, page = currentIndex, colors = colors)

                        if (config.collectHierarchyTree && showTreeView && currentPreview.composableNode != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(5f),
                                shape = RoundedCornerShape(4.dp),
                                color = colors.surface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, colors.border),
                                tonalElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Composable Hierarchy Tree",
                                            style = config.typography?.titleMedium ?: MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = { showTreeView = false },
                                            modifier = Modifier.noRippleClickable { showTreeView = false }
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_close),
                                                contentDescription = "Close",
                                                tint = colors.icon
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ComposableTreeView(
                                        node = currentPreview.composableNode,
                                        colors = colors,
                                        config = config
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                            modifier = Modifier.noRippleClickable { if (currentIndex > 0) currentIndex-- }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colors.icon
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous", color = colors.textPrimary)
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Text(
                                text = "${currentIndex + 1} / ${previews.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = config.typography?.labelMedium ?: MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = { if (currentIndex < previews.lastIndex) currentIndex++ },
                            enabled = currentIndex < previews.lastIndex,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                            modifier = Modifier.noRippleClickable { if (currentIndex < previews.lastIndex) currentIndex++ }
                        ) {
                            Text("Next", color = colors.textPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(Res.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colors.icon
                            )
                        }
                    }
                }
            }
        }
    }
}

fun toComposeColor(colorLong: Long): Color {
    val argb = if ((colorLong and 0xFF000000L) == 0L) {
        colorLong or 0xFF000000L
    } else {
        colorLong
    }
    val alpha = ((argb shr 24) and 0xFF).toFloat() / 255f
    val red = ((argb shr 16) and 0xFF).toFloat() / 255f
    val green = ((argb shr 8) and 0xFF).toFloat() / 255f
    val blue = (argb and 0xFF).toFloat() / 255f
    return Color(red, green, blue, alpha)
}

@Composable
private fun PreviewContentContainer(
    preview: PreviewEntry,
    page: Int,
    colors: PrevioColors
) {
    LaunchedEffect(preview.id, page) {
        println("[Previo Explorer] Active Preview Displayed: ${preview.name} (id: ${preview.id})")
    }

    val previewBg = if (preview.backgroundColor != null) toComposeColor(preview.backgroundColor) else Color.White

    key(preview.id, page) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(4.dp),
            color = previewBg,
            border = BorderStroke(
                1.dp,
                colors.border
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(previewBg),
                contentAlignment = Alignment.TopCenter
            ) {
                androidx.compose.material3.MaterialTheme(
                    colorScheme = androidx.compose.material3.lightColorScheme(
                        background = previewBg,
                        surface = previewBg,
                        onBackground = Color.Black,
                        onSurface = Color.Black
                    )
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides Color.Black
                    ) {
                        if (preview.isVerticallyScrollable) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(previewBg)
                                    .padding(16.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                preview.content()
                            }
                        } else {
                            val scrollState = rememberScrollState()
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(previewBg)
                                    .verticalScroll(scrollState)
                                    .padding(16.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                preview.content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComposableTreeView(
    node: ComposableNode,
    colors: PrevioColors,
    config: PrevioConfig,
    depth: Int = 0
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .noRippleClickable { if (!node.isNative && node.children.isNotEmpty()) isExpanded = !isExpanded },
                shape = RoundedCornerShape(4.dp),
                color = if (node.isNative) colors.surface else colors.accent,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!node.isNative && node.children.isNotEmpty()) {
                        Icon(
                            painter = painterResource(if (isExpanded) Res.drawable.ic_chevron_down else Res.drawable.ic_chevron_right),
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.size(14.dp),
                            tint = colors.icon
                        )
                    }
                    Text(
                        text = node.name,
                        style = config.typography?.labelSmall ?: MaterialTheme.typography.labelSmall,
                        color = colors.textPrimary
                    )
                }
            }
        }

        if (!node.isNative && isExpanded && node.children.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                node.children.forEach { child ->
                    ComposableTreeView(node = child, colors = colors, config = config, depth = depth + 1)
                }
            }
        }
    }
}

@Preview(name = "Previo Explorer Light Preview", showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewExplorerMockPreview() {
    PrevioTheme(config = PrevioConfig(title = "Component Catalog", isDarkTheme = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mock Previo Explorer Screen")
            }
        }
    }
}
