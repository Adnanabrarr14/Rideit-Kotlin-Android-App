package com.example.rideit.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class DriverSupportTopic(
    val title: String,
    val subtitle: String,
    val icon: String
)

private data class DriverSupportTicket(
    val title: String,
    val status: String,
    val date: String,
    val message: String
)

@Composable
fun DriverSupportScreen(
    driverName: String = "Shameer Khan",
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val supportTopics = remember {
        listOf(
            DriverSupportTopic(
                title = "Trip issue",
                subtitle = "Pickup, dropoff, fare or route problem",
                icon = "TR"
            ),
            DriverSupportTopic(
                title = "Payment issue",
                subtitle = "Wallet, payout, bonus or cash collection",
                icon = "₨"
            ),
            DriverSupportTopic(
                title = "Rider behavior",
                subtitle = "Report unsafe or inappropriate behavior",
                icon = "RB"
            ),
            DriverSupportTopic(
                title = "Account help",
                subtitle = "Profile, documents, rating or login support",
                icon = "AC"
            )
        )
    }

    val tickets = remember {
        listOf(
            DriverSupportTicket(
                title = "Payout delay question",
                status = "Resolved",
                date = "Today",
                message = "Your payout schedule was reviewed successfully."
            ),
            DriverSupportTicket(
                title = "Route fare review",
                status = "In review",
                date = "Yesterday",
                message = "Rideit support is checking your fare adjustment request."
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            DriverSupportHeader(
                driverName = driverName,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverEmergencySupportCard(
                onEmergencyClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Emergency support demo. Real emergency contact will be connected later."
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverSupportQuickActionsCard(
                onLiveChatClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Live chat demo opened."
                        )
                    }
                },
                onCallSupportClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Call support demo. Real call action will be connected later."
                        )
                    }
                },
                onReportIssueClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Report issue demo. Real support form will be connected later."
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Help topics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            supportTopics.forEach { topic ->
                DriverSupportTopicCard(
                    topic = topic,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${topic.title} support demo. Real details will be connected later."
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            DriverSafetyGuidelinesCard()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent support activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            tickets.forEach { ticket ->
                DriverSupportTicketCard(ticket = ticket)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DriverSupportHeader(
    driverName: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827),
        shadowElevation = 14.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF1F2937),
                            Color(0xFF8A35F2)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { onBackClick() },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "‹",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Driver Support",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.76f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF22C55E).copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "24/7",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Get fast help for trips, payments, safety, documents and driver account issues.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DriverEmergencySupportCard(
    onEmergencyClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF2A1111),
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6B6B)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emergency help",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Use only when you need urgent support during a trip.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEmergencyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = "Emergency Support Demo",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun DriverSupportQuickActionsCard(
    onLiveChatClick: () -> Unit,
    onCallSupportClick: () -> Unit,
    onReportIssueClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Quick support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SupportQuickButton(
                    title = "Chat",
                    icon = "💬",
                    color = Color(0xFF8A35F2),
                    modifier = Modifier.weight(1f),
                    onClick = onLiveChatClick
                )

                SupportQuickButton(
                    title = "Call",
                    icon = "☎",
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    onClick = onCallSupportClick
                )

                SupportQuickButton(
                    title = "Report",
                    icon = "!",
                    color = Color(0xFFE17A00),
                    modifier = Modifier.weight(1f),
                    onClick = onReportIssueClick
                )
            }
        }
    }
}

@Composable
private fun SupportQuickButton(
    title: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontWeight = FontWeight.Black,
                    color = color,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun DriverSupportTopicCard(
    topic: DriverSupportTopic,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A35F2).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = topic.icon,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8A35F2)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = topic.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
private fun DriverSafetyGuidelinesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF111827),
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Driver safety guidelines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            SupportGuidelineRow("Confirm rider name before starting trip")
            SupportGuidelineRow("Keep trips inside the Rideit app flow")
            SupportGuidelineRow("Report unsafe behavior immediately")
            SupportGuidelineRow("Do not share personal contact details")
        }
    }
}

@Composable
private fun SupportGuidelineRow(
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun DriverSupportTicketCard(
    ticket: DriverSupportTicket
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = ticket.date,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (ticket.status == "Resolved") {
                        Color(0xFF16A34A).copy(alpha = 0.10f)
                    } else {
                        Color(0xFFE17A00).copy(alpha = 0.10f)
                    }
                ) {
                    Text(
                        text = ticket.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (ticket.status == "Resolved") {
                            Color(0xFF16A34A)
                        } else {
                            Color(0xFFE17A00)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = ticket.message,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
        }
    }
}
