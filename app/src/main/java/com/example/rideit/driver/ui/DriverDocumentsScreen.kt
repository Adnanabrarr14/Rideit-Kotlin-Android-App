package com.example.rideit.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import kotlin.math.roundToInt

private enum class DriverDocumentStatus {
    Verified,
    PendingReview,
    Missing,
    ExpiringSoon
}

@Immutable
private data class DriverDocumentItem(
    val title: String,
    val subtitle: String,
    val status: DriverDocumentStatus,
    val icon: String,
    val expiryText: String,
    val lastUpdated: String,
    val helperText: String
)

@Composable
fun DriverDocumentsScreen(
    driverName: String = "Shameer Khan",
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val documents = remember {
        listOf(
            DriverDocumentItem(
                title = "CNIC",
                subtitle = "National identity verification",
                status = DriverDocumentStatus.Verified,
                icon = "ID",
                expiryText = "Valid",
                lastUpdated = "Verified",
                helperText = "Your identity document is approved."
            ),
            DriverDocumentItem(
                title = "Driving License",
                subtitle = "Professional driving eligibility",
                status = DriverDocumentStatus.Verified,
                icon = "DL",
                expiryText = "Expires Dec 2028",
                lastUpdated = "Verified",
                helperText = "Your driving license is approved."
            ),
            DriverDocumentItem(
                title = "Vehicle Registration",
                subtitle = "Toyota Corolla • LEA-4582",
                status = DriverDocumentStatus.Verified,
                icon = "VR",
                expiryText = "Valid",
                lastUpdated = "Verified",
                helperText = "Vehicle registration matches your active car."
            ),
            DriverDocumentItem(
                title = "Vehicle Insurance",
                subtitle = "Comprehensive coverage document",
                status = DriverDocumentStatus.Missing,
                icon = "IN",
                expiryText = "Upload required",
                lastUpdated = "Not uploaded",
                helperText = "Upload insurance to complete your driver verification."
            )
        )
    }

    val verifiedCount = documents.count { it.status == DriverDocumentStatus.Verified }
    val pendingCount = documents.count { it.status == DriverDocumentStatus.PendingReview }
    val missingCount = documents.count { it.status == DriverDocumentStatus.Missing }
    val expiringCount = documents.count { it.status == DriverDocumentStatus.ExpiringSoon }
    val progress = if (documents.isEmpty()) 0f else verifiedCount.toFloat() / documents.size.toFloat()
    val progressPercent = (progress * 100f).roundToInt()

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
            DriverDocumentsHeader(
                driverName = driverName,
                verifiedCount = verifiedCount,
                totalCount = documents.size,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            VerificationProgressCard(
                verifiedCount = verifiedCount,
                totalCount = documents.size,
                progress = progress,
                progressPercent = progressPercent,
                missingCount = missingCount,
                pendingCount = pendingCount,
                expiringCount = expiringCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            VehicleProfileCard()

            Spacer(modifier = Modifier.height(16.dp))

            DocumentStatusSummaryCard(
                verifiedCount = verifiedCount,
                pendingCount = pendingCount,
                missingCount = missingCount,
                expiringCount = expiringCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Required documents",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            documents.forEach { document ->
                DriverDocumentCard(
                    document = document,
                    onViewClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${document.title} preview is ready for future viewer connection."
                            )
                        }
                    },
                    onUploadClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${document.title} upload UI is ready for a later phase."
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            DriverDocumentGuidelinesCard()

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
private fun DriverDocumentsHeader(
    driverName: String,
    verifiedCount: Int,
    totalCount: Int,
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
                        text = "Driver Documents",
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
                    color = if (verifiedCount == totalCount) {
                        Color(0xFF22C55E).copy(alpha = 0.18f)
                    } else {
                        Color(0xFFE17A00).copy(alpha = 0.18f)
                    }
                ) {
                    Text(
                        text = "$verifiedCount/$totalCount Verified",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Keep your driver profile verified with valid identity, license, vehicle registration, and insurance documents.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VerificationProgressCard(
    verifiedCount: Int,
    totalCount: Int,
    progress: Float,
    progressPercent: Int,
    missingCount: Int,
    pendingCount: Int,
    expiringCount: Int
) {
    val actionText = when {
        missingCount > 0 -> "Upload missing documents to complete verification."
        pendingCount > 0 -> "Some documents are under review."
        expiringCount > 0 -> "Renew expiring documents to avoid account limits."
        verifiedCount == totalCount -> "All documents are verified. Your driver profile is active."
        else -> "Complete document verification to keep receiving trips."
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verification progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$verifiedCount of $totalCount documents verified",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF8A35F2).copy(alpha = 0.10f)
                ) {
                    Text(
                        text = "$progressPercent%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF8A35F2),
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = actionText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun VehicleProfileCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Vehicle profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A35F2).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚘",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Toyota Corolla",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "LEA-4582 • Comfort",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF16A34A).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VehicleMiniInfo("Year", "2021")
                VehicleMiniInfo("Color", "White")
                VehicleMiniInfo("Seats", "4")
            }
        }
    }
}

@Composable
private fun VehicleMiniInfo(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827)
        )
    }
}

@Composable
private fun DocumentStatusSummaryCard(
    verifiedCount: Int,
    pendingCount: Int,
    missingCount: Int,
    expiringCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Status overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMiniBox(
                    title = "Verified",
                    value = verifiedCount.toString(),
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )

                StatusMiniBox(
                    title = "Pending",
                    value = pendingCount.toString(),
                    color = Color(0xFFE17A00),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMiniBox(
                    title = "Missing",
                    value = missingCount.toString(),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )

                StatusMiniBox(
                    title = "Expiring",
                    value = expiringCount.toString(),
                    color = Color(0xFF8A35F2),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusMiniBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = color.copy(alpha = 0.09f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
private fun DriverDocumentCard(
    document: DriverDocumentItem,
    onViewClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    val statusColor = documentStatusColor(document.status)
    val statusLabel = documentStatusLabel(document.status)
    val canUpload = document.status == DriverDocumentStatus.Missing ||
            document.status == DriverDocumentStatus.ExpiringSoon

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
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
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = document.icon,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = document.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.10f)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = document.helperText.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = statusColor.copy(alpha = 0.07f)
                ) {
                    Text(
                        text = document.helperText,
                        modifier = Modifier.padding(13.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Document status",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = document.expiryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = document.lastUpdated,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (canUpload) {
                    Button(
                        onClick = onUploadClick,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8A35F2)
                        )
                    ) {
                        Text(
                            text = "Upload",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onViewClick,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "View",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverDocumentGuidelinesCard() {
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
                text = "Verification guidelines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            GuidelineRow("Upload clear, readable documents")
            GuidelineRow("Documents must match your driver profile")
            GuidelineRow("Vehicle registration must match active car")
            GuidelineRow("Insurance should be valid and not expired")
            GuidelineRow("Blurry or cropped images may be rejected")
        }
    }
}

@Composable
private fun GuidelineRow(
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

private fun documentStatusColor(status: DriverDocumentStatus): Color {
    return when (status) {
        DriverDocumentStatus.Verified -> Color(0xFF16A34A)
        DriverDocumentStatus.PendingReview -> Color(0xFFE17A00)
        DriverDocumentStatus.Missing -> Color(0xFFEF4444)
        DriverDocumentStatus.ExpiringSoon -> Color(0xFF8A35F2)
    }
}

private fun documentStatusLabel(status: DriverDocumentStatus): String {
    return when (status) {
        DriverDocumentStatus.Verified -> "Verified"
        DriverDocumentStatus.PendingReview -> "Review"
        DriverDocumentStatus.Missing -> "Missing"
        DriverDocumentStatus.ExpiringSoon -> "Expiring"
    }
}
