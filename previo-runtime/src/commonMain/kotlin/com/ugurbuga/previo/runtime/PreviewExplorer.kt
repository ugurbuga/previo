package com.ugurbuga.previo.runtime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import com.github.ugurbuga.previo_runtime.generated.resources.Res
import com.github.ugurbuga.previo_runtime.generated.resources.ic_arrow_back
import com.github.ugurbuga.previo_runtime.generated.resources.ic_search
import com.github.ugurbuga.previo_runtime.generated.resources.ic_close
import com.github.ugurbuga.previo_runtime.generated.resources.ic_filter_list
import com.github.ugurbuga.previo_runtime.generated.resources.ic_chevron_right

data class CategoryItem(
    val group: String,
    val minOrder: Int,
    val count: Int
)

enum class PreviewViewMode {
    FORMATTED,
    ALL
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
    val currentTheme = MaterialTheme.colorScheme
    val customColorScheme = remember(config, currentTheme) {
        config.colorScheme ?: if (config.primaryColor != null) {
            currentTheme.copy(
                primary = config.primaryColor,
                primaryContainer = config.primaryColor.copy(alpha = 0.15f)
            )
        } else currentTheme
    }

    val customTypography = config.typography ?: MaterialTheme.typography

    MaterialTheme(
        colorScheme = customColorScheme,
        typography = customTypography,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewExplorer(
    onBack: () -> Unit,
    config: PrevioConfig = PrevioConfig()
) {
    // Automatically load previews from PrevioRegistry via cross-platform loadGeneratedRegistry()
    val previews = remember {
        loadGeneratedRegistry()
        PrevioRegistry.previews
    }

    PrevioTheme(config = config) {
        var viewMode by remember { mutableStateOf(PreviewViewMode.FORMATTED) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var selectedPreviewIndex by remember { mutableStateOf<Int?>(null) }

        // Filter previews based on view mode (4-part format vs All)
        val modeFilteredPreviews = remember(previews, viewMode) {
            when (viewMode) {
                PreviewViewMode.FORMATTED -> previews.filter { isPreviewFormatted(it.name) }
                PreviewViewMode.ALL -> previews
            }
        }

        // Filter previews based on search query
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

        // Group and sort categories by order (min order in group), then alphabetical group name
        val categories = remember(filteredPreviews) {
            filteredPreviews
                .groupBy { it.group.ifBlank { "Unformatted" } }
                .map { (group, entries) ->
                    val minOrder = entries.minOfOrNull { it.order } ?: 0
                    CategoryItem(group = group, minOrder = minOrder, count = entries.size)
                }
                .sortedWith(compareBy<CategoryItem> { it.minOrder }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.group })
        }

        // Entries for currently selected category, sorted by order then alphabeticalOrder
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

        // If viewing swipeable detail pager
        if (selectedCategory != null && selectedPreviewIndex != null) {
            PreviewDetailPagerScreen(
                previews = categoryPreviews,
                initialIndex = selectedPreviewIndex!!,
                onBack = { selectedPreviewIndex = null },
                config = config
            )
            return@PrevioTheme
        }

        // If viewing category items list
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

        // Home Screen: Category List sorted by order
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(config.title, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                config.customHeader?.invoke()

                // View Mode Selector Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_filter_list),
                        contentDescription = "Filter",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    FilterChip(
                        selected = viewMode == PreviewViewMode.FORMATTED,
                        onClick = { viewMode = PreviewViewMode.FORMATTED },
                        label = { Text("4'lü Format (${previews.count { isPreviewFormatted(it.name) }})") },
                        shape = RoundedCornerShape(4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = viewMode == PreviewViewMode.ALL,
                        onClick = { viewMode = PreviewViewMode.ALL },
                        label = { Text("Tüm Preview'lar (${previews.size})") },
                        shape = RoundedCornerShape(4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search categories or previews...") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_close),
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No previews found. Ensure KSP generated GeneratedPreviewRegistry.",
                            style = MaterialTheme.typography.bodyLarge
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
                                    .clickable { selectedCategory = category.group },
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
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
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ) {
                                                Text(
                                                    "${category.count} Previews",
                                                    modifier = Modifier.padding(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp
                                                    ),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ) {
                                                Text(
                                                    "Order: ${category.minOrder}",
                                                    modifier = Modifier.padding(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp
                                                    ),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_chevron_right),
                                            contentDescription = "Navigate",
                                            modifier = Modifier.padding(8.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
fun CategoryItemsScreen(
    categoryName: String,
    previews: List<PreviewEntry>,
    onBack: () -> Unit,
    onPreviewClick: (Int) -> Unit,
    config: PrevioConfig = PrevioConfig()
) {
    PrevioTheme(config = config) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(categoryName, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (previews.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No previews in this category.")
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
                                    .clickable { onPreviewClick(index) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(4.dp)
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
                                            text = entry.alphabeticalOrder.ifBlank { entry.name }, // 3rd info or full name
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            ) {
                                                Text(
                                                    text = "Status: ${entry.customInfo.ifBlank { "Default" }}", // 4th info
                                                    modifier = Modifier.padding(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_chevron_right),
                                            contentDescription = "View",
                                            modifier = Modifier.padding(8.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
fun PreviewDetailPagerScreen(
    previews: List<PreviewEntry>,
    initialIndex: Int,
    onBack: () -> Unit,
    config: PrevioConfig = PrevioConfig()
) {
    PrevioTheme(config = config) {
        var currentIndex by remember { mutableStateOf(initialIndex) }
        val currentPreview = previews.getOrNull(currentIndex)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text((currentPreview?.alphabeticalOrder?.ifBlank { currentPreview.name }) ?: "Preview", style = MaterialTheme.typography.titleMedium)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                text = "${currentIndex + 1} / ${previews.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (currentPreview != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Info header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Component: ${currentPreview.alphabeticalOrder.ifBlank { currentPreview.name }}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        Text(
                                            "Group: ${currentPreview.group.ifBlank { "Unformatted" }}",
                                            modifier = Modifier.padding(
                                                horizontal = 4.dp,
                                                vertical = 2.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ) {
                                        Text(
                                            "Status: ${currentPreview.customInfo.ifBlank { "Default" }}",
                                            modifier = Modifier.padding(
                                                horizontal = 4.dp,
                                                vertical = 2.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Rendered Composable container for active preview
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        PreviewContentContainer(preview = currentPreview, page = currentIndex)
                    }

                    // Bottom Navigation Buttons ("Previous" & "Next")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }

                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "${currentIndex + 1} / ${previews.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        FilledTonalButton(
                            onClick = { if (currentIndex < previews.lastIndex) currentIndex++ },
                            enabled = currentIndex < previews.lastIndex,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(Res.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewContentContainer(
    preview: PreviewEntry,
    page: Int
) {
    LaunchedEffect(preview.id, page) {
        println("[Previo Explorer] Active Preview Displayed: ${preview.name} (id: ${preview.id})")
    }

    key(preview.id, page) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (preview.isVerticallyScrollable) {
                    // Content already has internal vertical scroll (e.g. LazyColumn): Render directly with zero outer scroll modifier
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        preview.content()
                    }
                } else {
                    // Content is static: Apply outer verticalScroll with fresh ScrollState isolated for this preview & page
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
