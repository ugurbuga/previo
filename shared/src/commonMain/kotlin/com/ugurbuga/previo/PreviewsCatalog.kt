package com.ugurbuga.previo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "[Buttons] [1] [Primary] [Enabled]")
@Composable
fun PrimaryButtonEnabledPreview() {
    Button(onClick = {}, shape = RoundedCornerShape(4.dp)) {
        Text("Primary Enabled")
    }
}

@Preview(name = "[Buttons] [1] [Primary] [Disabled]")
@Composable
fun PrimaryButtonDisabledPreview() {
    Button(onClick = {}, enabled = false, shape = RoundedCornerShape(4.dp)) {
        Text("Primary Disabled")
    }
}

@Preview(name = "[Buttons] [2] [Secondary] [Enabled]")
@Composable
fun SecondaryButtonEnabledPreview() {
    Button(onClick = {}, shape = RoundedCornerShape(4.dp)) {
        Text("Secondary Enabled")
    }
}

@Preview(name = "[Cards] [1] [Profile] [Default]")
@Composable
fun ProfileCardPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Profile Card", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("User: John Doe", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "[Screens] [1] [Settings] [Full]")
@Composable
fun LongSettingsScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Text("Personal Information")
                Text("Email & Password")
                Text("Linked Accounts")
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Preferences", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark Theme")
                    Switch(checked = true, onCheckedChange = {})
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Push Notifications")
                    Switch(checked = false, onCheckedChange = {})
                }
                Text("Language (English)")
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacy & Security", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Text("Two-Factor Authentication")
                Text("Data Sharing & Analytics")
                Text("Blocked Users")
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Text("App Version 1.0.0")
                Text("Terms of Service")
                Text("Privacy Policy")
            }
        }
    }
}

@Preview(name = "[Screens] [2] [Profile] [Detailed]")
@Composable
fun DetailedProfileScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomStart) {
                Text("Profile Header Banner", style = MaterialTheme.typography.titleLarge)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("Jane Doe", style = MaterialTheme.typography.headlineSmall)
            Text("@janedoe_dev", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Senior Compose Multiplatform Developer & Open Source Contributor. Building the future of cross-platform apps.")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("1.4K", style = MaterialTheme.typography.titleMedium)
                Text("Followers", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("420", style = MaterialTheme.typography.titleMedium)
                Text("Following", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("85", style = MaterialTheme.typography.titleMedium)
                Text("Posts", style = MaterialTheme.typography.bodySmall)
            }
        }

        HorizontalDivider()

        Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
        repeat(5) { index ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Activity Item #${index + 1}", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Published a new release for Previo CMP library with KSP integration.")
                }
            }
        }
    }
}

@Preview(name = "[Screens] [3] [Form] [Interactive]")
@Composable
fun FormInputScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Registration Form", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = "John",
            onValueChange = {},
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        OutlinedTextField(
            value = "Doe",
            onValueChange = {},
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        OutlinedTextField(
            value = "john.doe@example.com",
            onValueChange = {},
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        OutlinedTextField(
            value = "+1 (555) 019-2834",
            onValueChange = {},
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp)
        )

        OutlinedTextField(
            value = "Kotlin & Compose Multiplatform Enthusiast...",
            onValueChange = {},
            label = { Text("Short Bio") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(4.dp)
        )

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Save Profile Changes")
        }
    }
}

@Preview(name = "[Screens] [4] [Checkout] [OrderSummary]")
@Composable
fun EcommerceCheckoutScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Order Summary", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Items in Cart (3)", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Wireless Headphones x1")
                    Text("$149.99", style = MaterialTheme.typography.titleSmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Smart Watch Series 9 x1")
                    Text("$299.00", style = MaterialTheme.typography.titleSmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("USB-C Fast Cable x2")
                    Text("$19.99", style = MaterialTheme.typography.titleSmall)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Promo Code", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "PREVIO2026",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp)
                    )
                    Button(onClick = {}, shape = RoundedCornerShape(4.dp)) {
                        Text("Apply")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cost Breakdown", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal")
                    Text("$468.98")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Shipping Fee")
                    Text("$9.99")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Tax")
                    Text("$35.00")
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("$513.97", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Complete Order ($513.97)", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(name = "[Screens] [5] [Dashboard] [Crypto]")
@Composable
fun CryptoDashboardScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Total Portfolio Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$48,290.50", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(6.dp))
                Text("+ $2,480.12 (5.42% 24h)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                Text("Deposit")
            }
            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                Text("Transfer")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                Text("Buy")
            }
        }

        Text("Your Assets", style = MaterialTheme.typography.titleLarge)

        listOf(
            Triple("Bitcoin", "0.45 BTC", "$28,400.00"),
            Triple("Ethereum", "3.8 ETH", "$12,500.00"),
            Triple("Solana", "28 SOL", "$4,100.00"),
            Triple("Cardano", "8,200 ADA", "$3,290.50")
        ).forEach { (coin, amount, valUsd) ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(coin, style = MaterialTheme.typography.titleMedium)
                        Text(amount, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(valUsd, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Preview(name = "[Screens] [6] [Feed] [Social]")
@Composable
fun SocialFeedScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Social Timeline", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        // Stories Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Your Story", "Alex", "Sarah", "Michael", "Elena").forEach { name ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(name.take(1), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        HorizontalDivider()

        // Feed Post 1
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Box(contentAlignment = Alignment.Center) { Text("A") }
                    }
                    Column {
                        Text("Alex Rivera", style = MaterialTheme.typography.titleSmall)
                        Text("2 hours ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("Excited to launch our new Compose Multiplatform UI library Previo! Automatic `@Preview` discovery with zero reflection.")
                Surface(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Post Media Attachment Placeholder", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("❤️ 1,240 Likes", style = MaterialTheme.typography.labelMedium)
                    Text("💬 84 Comments", style = MaterialTheme.typography.labelMedium)
                    Text("🔄 32 Shares", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Feed Post 2
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Box(contentAlignment = Alignment.Center) { Text("S") }
                    }
                    Column {
                        Text("Sarah Connor", style = MaterialTheme.typography.titleSmall)
                        Text("5 hours ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("Kotlin 2.4.21 and Compose 1.12.2 performance optimizations are mind-blowing! Highly recommended to upgrade.")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("❤️ 512 Likes", style = MaterialTheme.typography.labelMedium)
                    Text("💬 19 Comments", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview(name = "[ScrollSamples] [1] [VerticalScroll] [VerticalScroll]")
@Composable
fun VerticalScrollSamplePreview() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("VerticalScroll Modifier Sample", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        repeat(20) { index ->
            Card(modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(4.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Vertical Scroll Item #${index + 1}")
                }
            }
        }
    }
}

@Preview(name = "[ScrollSamples] [2] [HorizontalScroll] [HorizontalScroll]")
@Composable
fun HorizontalScrollSamplePreview() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("HorizontalScroll Modifier Sample", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(15) { index ->
                Card(modifier = Modifier.size(120.dp, 100.dp), shape = RoundedCornerShape(4.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("H-Item #${index + 1}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Preview(name = "[ScrollSamples] [3] [LazyRow] [LazyRow]")
@Composable
fun LazyRowSamplePreview() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("LazyRow Sample", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(20) { index ->
                Card(modifier = Modifier.size(100.dp, 100.dp), shape = RoundedCornerShape(4.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("LazyRow #${index + 1}")
                    }
                }
            }
        }
    }
}

@Preview(name = "[Screens] [12] [LazyGrid] [LazyVerticalGrid]")
@Composable
fun LazyGridSamplePreview() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text("LazyVerticalGrid Sample (30 items)", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(30) { index ->
                Card(modifier = Modifier.height(80.dp), shape = RoundedCornerShape(4.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Grid Item #${index + 1}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Preview(name = "[Screens] [7] [LazyList] [LazyColumn]")
@Composable
fun InfiniteLazyListScreenPreview() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text("Conversations (50 items)", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(50) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("#${index + 1}")
                                }
                            }
                            Column {
                                Text("Contact Name #${index + 1}", style = MaterialTheme.typography.titleSmall)
                                Text("Hey there! How is the Compose Multiplatform build going?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("14:${(10 + index) % 60}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Preview(name = "[Screens] [8] [Settings] [Default]")
@Composable
fun SettingsInternalScrollScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Full Natively Settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        repeat(8) { sectionIndex ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Settings Section #${sectionIndex + 1}", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    Text("Option A: Configuration parameter for section #${sectionIndex + 1}")
                    Text("Option B: Enable high performance mode")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Option C: Auto-sync on Wi-Fi")
                        Switch(checked = sectionIndex % 2 == 0, onCheckedChange = {})
                    }
                }
            }
        }
    }
}

@Preview(name = "[Screens] [9] [Transactions] [LazyColumn]")
@Composable
fun TransactionsLazyListScreenPreview() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text("Account Transactions History (30)", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(30) { index ->
                val isIncome = index % 3 == 0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isIncome) "Payment Received #${index + 1}" else "Online Merchant Purchase #${index + 1}", style = MaterialTheme.typography.titleSmall)
                            Text("March ${(index % 28) + 1}, 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = if (isIncome) "+ $${150 + index * 12}.00" else "- $${25 + index * 4}.50",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "[Components] [1] [Media] [PlayerCard]")
@Composable
fun MediaPlaybackCardPreview() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎵", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Episode 42: Building Previo", style = MaterialTheme.typography.titleMedium)
                    Text("Kotlin Multiplatform Podcast", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            LinearProgressIndicator(progress = { 0.35f }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("03:45", style = MaterialTheme.typography.labelSmall)
                Text("45:00", style = MaterialTheme.typography.labelSmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Text("⏮", style = MaterialTheme.typography.titleLarge)
                Button(onClick = {}, shape = RoundedCornerShape(4.dp)) { Text("⏸ Pause") }
                Text("⏭", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview(name = "[Components] [2] [Notifications] [GroupedList]")
@Composable
fun NotificationListPreview() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Notifications", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

        listOf(
            Pair("Security Alert", "New login detected from macOS in San Francisco."),
            Pair("Release Update", "Previo version 1.0.0 has been published to Maven."),
            Pair("Social Mention", "Alex mentioned you in a comment: 'Great job!'")
        ).forEach { (title, body) ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(name = "[Components] [3] [Payment] [CreditCard]")
@Composable
fun CreditCardViewPreview() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PREVIO BANK", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                Text("VISA", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
            Text("**** **** **** 4829", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("CARDHOLDER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text("UGUR BUGA", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                Column {
                    Text("EXPIRES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text("08/28", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Preview(name = "Simple Unformatted Button")
@Composable
fun SimpleUnformattedButtonPreview() {
    Button(onClick = {}, shape = RoundedCornerShape(4.dp)) {
        Text("Simple Unformatted Button")
    }
}

@Preview(name = "Custom Header Banner")
@Composable
fun CustomHeaderBannerPreview() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Custom HeaderBanner", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("This preview does not use 4-part bracket formatting.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(name = "[Screens] [10] [Transactions] [LazyColumn]")
@Composable
fun TransactionsLazyList2ScreenPreview() {
    TransactionsLazyListScreenPreview()
}

@Preview(name = "[Screens] [11] [Settings] [Default]")
@Composable
fun SettingsInternalScroll2ScreenPreview() {
    SettingsInternalScrollScreenPreview()
}

@Preview(name = "Floating Action Button")
@Composable
fun FloatingActionButtonPreview() {
    FloatingActionButton(
        onClick = {},
        shape = RoundedCornerShape(4.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Text("+ Add Item", modifier = Modifier.padding(horizontal = 16.dp))
    }
}
