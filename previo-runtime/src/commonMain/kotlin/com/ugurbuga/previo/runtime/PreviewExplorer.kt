package com.ugurbuga.previo.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.rotate
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
import io.github.ugurbuga.previo_runtime.generated.resources.previo_category
import io.github.ugurbuga.previo_runtime.generated.resources.previo_clear
import io.github.ugurbuga.previo_runtime.generated.resources.previo_close
import io.github.ugurbuga.previo_runtime.generated.resources.previo_composable_hierarchy_tree
import io.github.ugurbuga.previo_runtime.generated.resources.previo_four_column_format
import io.github.ugurbuga.previo_runtime.generated.resources.previo_next
import io.github.ugurbuga.previo_runtime.generated.resources.previo_no_previews_found
import io.github.ugurbuga.previo_runtime.generated.resources.previo_previews
import io.github.ugurbuga.previo_runtime.generated.resources.previo_previous
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
        background = colors.screenBackground,
        onBackground = colors.primaryText,
        surface = colors.cardBackground,
        onSurface = colors.primaryText,
        outline = colors.secondaryText.copy(alpha = 0.2f),
        outlineVariant = colors.secondaryText.copy(alpha = 0.2f)
    ) ?: MaterialTheme.colorScheme.copy(
        background = colors.screenBackground,
        onBackground = colors.primaryText,
        surface = colors.cardBackground,
        onSurface = colors.primaryText,
        outline = colors.secondaryText.copy(alpha = 0.2f),
        outlineVariant = colors.secondaryText.copy(alpha = 0.2f)
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
    config: PrevioConfig = PrevioConfig(),
    previews: List<PreviewEntry> = remember {
        loadGeneratedRegistry()
        PrevioRegistry.previews
    }
) {
    if (!config.enabled) {
        return
    }

    val rawPreviews = previews

    val filteredPreviewsList = remember(rawPreviews, config.collectPreviews) {
        if (config.collectPreviews) rawPreviews else emptyList()
    }

    SetStatusBarColor(isDarkTheme = config.isDarkTheme)

    PrevioTheme(config = config) {
        val colors = config.getColors()
        var viewMode by remember { mutableStateOf(PreviewViewMode.FORMATTED) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var selectedPreviewIndex by remember { mutableStateOf<Int?>(null) }

        val modeFilteredPreviews = remember(filteredPreviewsList, viewMode) {
            when (viewMode) {
                PreviewViewMode.FORMATTED -> filteredPreviewsList.filter { isPreviewFormatted(it.name) }
                PreviewViewMode.ALL -> filteredPreviewsList
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
            containerColor = colors.screenBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            config.title,
                            style = config.typography?.titleMedium?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                ?: MaterialTheme.typography.titleMedium,
                            color = colors.primaryText
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
                                tint = colors.iconTint
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.screenBackground
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.screenBackground)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = viewMode == PreviewViewMode.FORMATTED,
                        onClick = { viewMode = PreviewViewMode.FORMATTED },
                        label = {
                            Text(
                                stringResource(Res.string.previo_four_column_format) + " (${
                                    filteredPreviewsList.count {
                                        isPreviewFormatted(
                                            it.name
                                        )
                                    }
                                })"
                            )
                        },
                        shape = RoundedCornerShape(4.dp),
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.cardBackground,
                            labelColor = colors.secondaryText,
                            selectedContainerColor = colors.chipSelectedBackground,
                            selectedLabelColor = colors.chipSelectedText
                        ),
                        modifier = Modifier.noRippleClickable {
                            viewMode = PreviewViewMode.FORMATTED
                        }
                    )
                    FilterChip(
                        selected = viewMode == PreviewViewMode.ALL,
                        onClick = { viewMode = PreviewViewMode.ALL },
                        label = { Text(stringResource(Res.string.previo_all) + " (${filteredPreviewsList.size})") },
                        shape = RoundedCornerShape(4.dp),
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.cardBackground,
                            labelColor = colors.secondaryText,
                            selectedContainerColor = colors.chipSelectedBackground,
                            selectedLabelColor = colors.chipSelectedText
                        ),
                        modifier = Modifier.noRippleClickable { viewMode = PreviewViewMode.ALL }
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            stringResource(Res.string.previo_search_placeholder),
                            color = colors.secondaryText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = stringResource(Res.string.previo_search_placeholder),
                            tint = colors.iconTint
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
                                    tint = colors.iconTint
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBackground,
                        unfocusedContainerColor = colors.cardBackground,
                        focusedBorderColor = colors.primaryText,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    )
                )

                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(colors.screenBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(Res.string.previo_no_previews_found),
                            style = config.typography?.bodyLarge
                                ?: MaterialTheme.typography.bodyLarge,
                            color = colors.secondaryText
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
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.cardBackground
                                ),
                                border = null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = category.group,
                                            style = config.typography?.titleMedium?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                                ?: MaterialTheme.typography.titleMedium,
                                            color = colors.primaryText
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = colors.badgeBackground
                                        ) {
                                            Text(
                                                text = "${category.count} " + stringResource(Res.string.previo_previews),
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                ),
                                                style = config.typography?.labelSmall
                                                    ?: MaterialTheme.typography.labelSmall,
                                                color = colors.badgeText
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.badgeBackground
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_chevron_right),
                                            contentDescription = "Navigate",
                                            modifier = Modifier.padding(8.dp),
                                            tint = colors.iconTint
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
            containerColor = colors.screenBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            categoryName,
                            style = config.typography?.titleMedium?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                ?: MaterialTheme.typography.titleMedium,
                            color = colors.primaryText
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
                                tint = colors.iconTint
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.screenBackground
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.screenBackground)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (previews.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(colors.screenBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(Res.string.previo_no_previews_found),
                            color = colors.secondaryText
                        )
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
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.cardBackground
                                ),
                                border = null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = entry.alphabeticalOrder.ifBlank { entry.name },
                                            style = config.typography?.titleSmall?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                                ?: MaterialTheme.typography.titleSmall,
                                            color = colors.primaryText
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = colors.badgeBackground
                                        ) {
                                            Text(
                                                text = entry.customInfo.ifBlank { "Default" },
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                ),
                                                style = config.typography?.labelSmall
                                                    ?: MaterialTheme.typography.labelSmall,
                                                color = colors.badgeText
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.badgeBackground
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_chevron_right),
                                            contentDescription = "View",
                                            modifier = Modifier.padding(8.dp),
                                            tint = colors.iconTint
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
            containerColor = colors.screenBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                (currentPreview?.alphabeticalOrder?.ifBlank { currentPreview.name })
                                    ?: "Preview",
                                style = config.typography?.titleMedium?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    ?: MaterialTheme.typography.titleMedium,
                                color = colors.primaryText
                            )
                            if (currentPreview != null) {
                                if (currentPreview.group.isNotBlank()) {
                                    Text(
                                        text = stringResource(Res.string.previo_category) + ": ${currentPreview.group}",
                                        style = config.typography?.labelSmall
                                            ?: MaterialTheme.typography.labelSmall,
                                        color = colors.secondaryText
                                    )
                                }
                                if (currentPreview.customInfo.isNotBlank()) {
                                    Text(
                                        text = currentPreview.customInfo,
                                        style = config.typography?.labelSmall
                                            ?: MaterialTheme.typography.labelSmall,
                                        color = colors.secondaryText
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
                                contentDescription = stringResource(Res.string.previo_back),
                                tint = colors.iconTint
                            )
                        }
                    },
                    actions = {
                        if (config.collectHierarchyTree && currentPreview != null && currentPreview.composableNode != null) {
                            IconButton(
                                onClick = { showTreeView = !showTreeView },
                                modifier = Modifier.noRippleClickable {
                                    showTreeView = !showTreeView
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_filter_list),
                                    contentDescription = stringResource(Res.string.previo_composable_hierarchy_tree),
                                    tint = colors.iconTint
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.screenBackground
                    )
                )
            }
        ) { paddingValues ->
            if (currentPreview != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.screenBackground)
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        PreviewContentContainer(
                            preview = currentPreview,
                            page = currentIndex,
                            colors = colors
                        )

                        if (config.collectHierarchyTree && showTreeView && currentPreview.composableNode != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(5f),
                                shape = RoundedCornerShape(4.dp),
                                color = colors.cardBackground.copy(alpha = 0.95f),
                                border = null,
                                tonalElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.previo_composable_hierarchy_tree),
                                            style = config.typography?.titleSmall?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                                ?: MaterialTheme.typography.titleSmall,
                                            color = colors.primaryText
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { showTreeView = false },
                                                modifier = Modifier.noRippleClickable {
                                                    showTreeView = false
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.ic_close),
                                                    contentDescription = stringResource(Res.string.previo_close),
                                                    tint = colors.iconTint
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        ComposableTreeView(
                                            node = currentPreview.composableNode,
                                            colors = colors,
                                            config = config
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Navigation Bar with explicit Active vs Disabled states
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prevEnabled = currentIndex > 0
                        val nextEnabled = currentIndex < previews.lastIndex

                        OutlinedButton(
                            onClick = { if (prevEnabled) currentIndex-- },
                            enabled = prevEnabled,
                            shape = RoundedCornerShape(4.dp),
                            border = null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (prevEnabled) colors.cardBackground else colors.cardBackground.copy(
                                    alpha = 0.4f
                                ),
                                contentColor = if (prevEnabled) colors.primaryText else colors.secondaryText
                            ),
                            modifier = Modifier.noRippleClickable { if (prevEnabled) currentIndex-- }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).rotate(180f),
                                tint = if (prevEnabled) colors.iconTint else colors.secondaryText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(Res.string.previo_previous))
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.badgeBackground
                        ) {
                            Text(
                                text = "${currentIndex + 1} / ${previews.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = config.typography?.labelMedium?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    ?: MaterialTheme.typography.labelMedium,
                                color = colors.badgeText
                            )
                        }

                        OutlinedButton(
                            onClick = { if (nextEnabled) currentIndex++ },
                            enabled = nextEnabled,
                            shape = RoundedCornerShape(4.dp),
                            border = null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (nextEnabled) colors.cardBackground else colors.cardBackground.copy(
                                    alpha = 0.4f
                                ),
                                contentColor = if (nextEnabled) colors.primaryText else colors.secondaryText
                            ),
                            modifier = Modifier.noRippleClickable { if (nextEnabled) currentIndex++ }
                        ) {
                            Text(stringResource(Res.string.previo_next))
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(Res.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (nextEnabled) colors.iconTint else colors.secondaryText
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

    val previewBg =
        if (preview.backgroundColor != null) toComposeColor(preview.backgroundColor) else Color.White

    key(preview.id, page) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(4.dp),
            color = previewBg,
            border = null
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
                    CompositionLocalProvider(
                        LocalContentColor provides Color.Black
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(previewBg)
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

@Composable
fun ComposableTreeView(
    node: ComposableNode,
    colors: PrevioColors,
    config: PrevioConfig,
    depth: Int = 0
) {
    val previoNode = remember(node) { node.toPrevioNode() }
    PrevioNodeTreeView(
        node = previoNode,
        colors = colors,
        config = config,
        depth = depth
    )
}

@Composable
fun PrevioNodeTreeView(
    node: PrevioNode,
    colors: PrevioColors,
    config: PrevioConfig,
    depth: Int = 0
) {
    var isExpanded by remember { mutableStateOf(true) }
    val isLeaf = node.kind == NodeKind.Leaf

    Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Main node chip
            Surface(
                modifier = Modifier
                    .noRippleClickable {
                        if (!isLeaf && node.children.isNotEmpty()) isExpanded = !isExpanded
                    }.padding(top = 2.dp),
                shape = RoundedCornerShape(4.dp),
                color = if (isLeaf) colors.primaryText.copy(alpha = 0.65f) else colors.primaryText,
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = node.name,
                        style = config.typography?.labelMedium?.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            ?: MaterialTheme.typography.labelMedium,
                        color = colors.chipSelectedBackground,
                    )
                    if (!isLeaf && node.children.isNotEmpty()) {
                        Icon(
                            painter = painterResource(if (isExpanded) Res.drawable.ic_chevron_down else Res.drawable.ic_chevron_right),
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.size(14.dp),
                            tint = colors.chipSelectedBackground
                        )
                    }
                }
            }

            if (config.showExtraTreeInfo) {
                // Slot/Role badge: e.g. "topBar", "navigationIcon"
                if (!node.slotOrRole.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.secondaryText.copy(alpha = 0.12f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = node.slotOrRole,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = config.typography?.labelSmall
                                ?: MaterialTheme.typography.labelSmall,
                            color = colors.secondaryText
                        )
                    }
                }

                // InfoOrText badge: e.g. "Reytingler" from Text("Reytingler")
                if (!node.infoOrText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.badgeBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = "\"${node.infoOrText}\"",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = config.typography?.labelSmall
                                ?: MaterialTheme.typography.labelSmall,
                            color = colors.badgeText
                        )
                    }
                }
            }
        }

        if (!isLeaf && isExpanded && node.children.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    node.children.forEach { child ->
                        PrevioNodeTreeView(
                            node = child,
                            colors = colors,
                            config = config,
                            depth = depth + 1
                        )
                    }
                }
            }
        }
    }
}

// ==================== Previo Runtime Previews (Light & Dark for all 4 screens) ====================

private val sampleCatalogEntries = listOf(
    PreviewEntry(
        id = "com.ugurbuga.previo.LongSettingsScreenPreview",
        name = "[Screens] [1] [Settings] [Full]",
        group = "Screens",
        order = 1,
        alphabeticalOrder = "Settings",
        customInfo = "Column, Row, Card",
        content = { Text("Settings Content") }
    ),
    PreviewEntry(
        id = "com.ugurbuga.previo.CryptoDashboardScreenPreview",
        name = "[Screens] [5] [Dashboard] [Crypto]",
        group = "Screens",
        order = 5,
        alphabeticalOrder = "Dashboard",
        customInfo = "Card, Row",
        content = { Text("Dashboard Content") }
    ),
    PreviewEntry(
        id = "com.ugurbuga.previo.VerticalScrollSamplePreview",
        name = "[ScrollSamples] [1] [VerticalScroll] [VerticalScroll]",
        group = "ScrollSamples",
        order = 1,
        alphabeticalOrder = "VerticalScroll",
        customInfo = "Column (scrollable)",
        content = { Text("Scroll Content") }
    )
)

private val sampleNodeForTree = ComposableNode(
    name = "LongSettingsScreenPreview",
    isNative = false,
    children = listOf(
        ComposableNode(
            "Column", false, listOf(
                ComposableNode("Text", true),
                ComposableNode(
                    "Card", false, listOf(
                        ComposableNode(
                            "Column", false, listOf(
                                ComposableNode("Text", true),
                                ComposableNode("HorizontalDivider", true)
                            )
                        )
                    )
                )
            )
        )
    )
)

// 1. Previo Explorer (Light & Dark)
@Preview(
    name = "Light - Previo Explorer",
    group = "1. Explorer",
    showBackground = true,
    backgroundColor = 0xFFF8FAFC
)
@Composable
fun PreviewExplorerPopulatedLightPreview() {
    PreviewExplorer(
        onBack = {},
        config = PrevioConfig(title = "Previo Component Catalog", isDarkTheme = false),
        previews = sampleCatalogEntries
    )
}

@Preview(
    name = "Dark - Previo Explorer",
    group = "1. Explorer",
    showBackground = true,
    backgroundColor = 0x0B0F19
)
@Composable
fun PreviewExplorerPopulatedDarkPreview() {
    PreviewExplorer(
        onBack = {},
        config = PrevioConfig(title = "Previo Component Catalog", isDarkTheme = true),
        previews = sampleCatalogEntries
    )
}

// 2. Category Items Screen (Light & Dark)
@Preview(
    name = "Light - Category Items Screen",
    group = "2. Category",
    showBackground = true,
    backgroundColor = 0xFFF8FAFC
)
@Composable
fun CategoryItemsScreenLightPreview() {
    CategoryItemsScreen(
        categoryName = "Screens",
        previews = sampleCatalogEntries,
        onBack = {},
        onPreviewClick = {},
        config = PrevioConfig(isDarkTheme = false)
    )
}

@Preview(
    name = "Dark - Category Items Screen",
    group = "2. Category",
    showBackground = true,
    backgroundColor = 0x0B0F19
)
@Composable
fun CategoryItemsScreenDarkPreview() {
    CategoryItemsScreen(
        categoryName = "Screens",
        previews = sampleCatalogEntries,
        onBack = {},
        onPreviewClick = {},
        config = PrevioConfig(isDarkTheme = true)
    )
}

// 3. Detail Pager Screen (Light & Dark)
@Preview(
    name = "Light - Detail Pager Screen",
    group = "3. Detail",
    showBackground = true,
    backgroundColor = 0xFFF8FAFC
)
@Composable
fun PreviewExplorerDetailLightPreview() {
    PreviewDetailPagerScreen(
        previews = sampleCatalogEntries,
        initialIndex = 0,
        onBack = {},
        config = PrevioConfig(isDarkTheme = false)
    )
}

@Preview(
    name = "Dark - Detail Pager Screen",
    group = "3. Detail",
    showBackground = true,
    backgroundColor = 0x0B0F19
)
@Composable
fun PreviewExplorerDetailDarkPreview() {
    PreviewDetailPagerScreen(
        previews = sampleCatalogEntries,
        initialIndex = 0,
        onBack = {},
        config = PrevioConfig(isDarkTheme = true)
    )
}

// 4. Detail Pager Screen with Hierarchy Tree Open (Light & Dark)
@Preview(
    name = "Light - Detail with Hierarchy Tree",
    group = "4. Detail Tree",
    showBackground = true,
    backgroundColor = 0xFFF8FAFC
)
@Composable
fun PreviewExplorerDetailTreeLightPreview() {
    val treeEntries = listOf(
        sampleCatalogEntries.first().copy(composableNode = sampleNodeForTree)
    )
    val colors = PrevioDefaults.LightColors

    PrevioTheme(config = PrevioConfig(isDarkTheme = false)) {
        Box(modifier = Modifier.fillMaxSize()) {
            PreviewDetailPagerScreen(
                previews = treeEntries,
                initialIndex = 0,
                onBack = {},
                config = PrevioConfig(isDarkTheme = false)
            )
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f),
                shape = RoundedCornerShape(4.dp),
                color = colors.cardBackground.copy(alpha = 0.95f),
                border = null,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.previo_composable_hierarchy_tree),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = colors.primaryText
                    )
                    ComposableTreeView(
                        node = sampleNodeForTree,
                        colors = colors,
                        config = PrevioConfig(isDarkTheme = false)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Dark - Detail with Hierarchy Tree",
    group = "4. Detail Tree",
    showBackground = true,
    backgroundColor = 0x0B0F19
)
@Composable
fun PreviewExplorerDetailTreeDarkPreview() {
    val treeEntries = listOf(
        sampleCatalogEntries.first().copy(composableNode = sampleNodeForTree)
    )
    val colors = PrevioDefaults.DarkColors

    PrevioTheme(config = PrevioConfig(isDarkTheme = true)) {
        Box(modifier = Modifier.fillMaxSize()) {
            PreviewDetailPagerScreen(
                previews = treeEntries,
                initialIndex = 0,
                onBack = {},
                config = PrevioConfig(isDarkTheme = true)
            )
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f),
                shape = RoundedCornerShape(4.dp),
                color = colors.cardBackground.copy(alpha = 0.95f),
                border = null,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.previo_composable_hierarchy_tree),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = colors.primaryText
                    )
                    ComposableTreeView(
                        node = sampleNodeForTree,
                        colors = colors,
                        config = PrevioConfig(isDarkTheme = true)
                    )
                }
            }
        }
    }
}
