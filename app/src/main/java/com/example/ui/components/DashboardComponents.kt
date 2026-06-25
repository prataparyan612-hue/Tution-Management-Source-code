package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FeePayment
import com.example.ui.theme.*
import java.util.*

// --- Analytical Charts (Custom Canvas Drawings) ---

@Composable
fun IncomeLineChart(
    modifier: Modifier = Modifier,
    data: List<Double> = listOf(1200.0, 1800.0, 1500.0, 2400.0, 2200.0, 3100.0, 2900.0),
    labels: List<String> = listOf("Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun")
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Income Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recent billing & collection analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "$${data.lastOrNull() ?: 0.0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxVal = (data.maxOrNull() ?: 1.0) * 1.15
                    val minVal = 0.0
                    val range = maxVal - minVal

                    val stepX = width / (data.size - 1)
                    val points = data.mapIndexed { idx, value ->
                        val x = idx * stepX
                        val y = height - (((value - minVal) / range) * height).toFloat()
                        Offset(x, y)
                    }

                    // Draw subtle background grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val gridY = height * i / gridLines
                        drawLine(
                            color = onSurface.copy(alpha = 0.1f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Create line path and fill path
                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            path.moveTo(point.x, point.y)
                            fillPath.moveTo(point.x, height)
                            fillPath.lineTo(point.x, point.y)
                        } else {
                            // Smooth bezier curves instead of sharp lines
                            val prevPoint = points[index - 1]
                            val controlPoint1 = Offset(prevPoint.x + (point.x - prevPoint.x) / 2, prevPoint.y)
                            val controlPoint2 = Offset(prevPoint.x + (point.x - prevPoint.x) / 2, point.y)
                            path.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                point.x, point.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                point.x, point.y
                            )
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Draw Area Gradient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.00f)
                            )
                        )
                    )

                    // Draw Stroke Line
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Highlight Circles
                    points.forEach { point ->
                        drawCircle(
                            color = primaryColor,
                            radius = 5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Draw Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceBarChart(
    modifier: Modifier = Modifier,
    presentCount: Int = 18,
    lateCount: Int = 4,
    absentCount: Int = 2
) {
    val total = (presentCount + lateCount + absentCount).coerceAtLeast(1)
    val presPercent = presentCount.toFloat() / total
    val latePercent = lateCount.toFloat() / total
    val absPercent = absentCount.toFloat() / total

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Attendance Split Ratio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Active tracking distribution (last 30 days)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                if (presPercent > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(presPercent)
                            .background(SuccessGreen)
                    )
                }
                if (latePercent > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(latePercent)
                            .background(WarningOrange)
                    )
                }
                if (absPercent > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(absPercent)
                            .background(ErrorRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(label = "Present ($presentCount)", color = SuccessGreen)
                LegendItem(label = "Late ($lateCount)", color = WarningOrange)
                LegendItem(label = "Absent ($absentCount)", color = ErrorRed)
            }
        }
    }
}

@Composable
fun GradeDistributionPie(
    modifier: Modifier = Modifier,
    grades: Map<String, Int> = mapOf("A/A+" to 12, "B" to 6, "C" to 3, "Fail" to 1)
) {
    val total = grades.values.sum().coerceAtLeast(1)
    val colors = listOf(PrimaryLight, SecondaryLight, TertiaryLight, ErrorRed)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "Grade Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Consolidated results across active batches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                grades.entries.forEachIndexed { index, entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(colors.getOrElse(index) { Color.Gray })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${entry.key}: ${entry.value} students (${String.format("%.1f", (entry.value.toFloat() / total) * 100)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension * 0.85f
                    val innerDiameter = diameter * 0.6f
                    val rect = Size(diameter, diameter)
                    val offset = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

                    var startAngle = -90f
                    grades.values.forEachIndexed { idx, valCount ->
                        val sweep = (valCount.toFloat() / total) * 360f
                        drawArc(
                            color = colors.getOrElse(idx) { Color.Gray },
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = offset,
                            size = rect,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Students",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- Quick Action/Status Cards ---

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// --- Simulated QR Attendance Dialog ---

@Composable
fun QrAttendanceDialog(
    studentName: String,
    onDismiss: () -> Unit,
    onAttendanceMarked: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (!isScanning && !scanCompleted) "QR Attendance Portal" else if (isScanning) "Scanning QR Pass..." else "Attendance Verified!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Student ID verification gateway",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!isScanning && !scanCompleted) {
                    // QR Code Generator simulation
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw realistic outer QR alignment boxes
                            val sizePx = size.width
                            val boxSize = sizePx * 0.22f

                            // Top Left Box
                            drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(boxSize, boxSize))
                            drawRect(color = Color.White, topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(boxSize - 8.dp.toPx(), boxSize - 8.dp.toPx()))
                            drawRect(color = Color.Black, topLeft = Offset(8.dp.toPx(), 8.dp.toPx()), size = Size(boxSize - 16.dp.toPx(), boxSize - 16.dp.toPx()))

                            // Top Right Box
                            drawRect(color = Color.Black, topLeft = Offset(sizePx - boxSize, 0f), size = Size(boxSize, boxSize))
                            drawRect(color = Color.White, topLeft = Offset(sizePx - boxSize + 4.dp.toPx(), 4.dp.toPx()), size = Size(boxSize - 8.dp.toPx(), boxSize - 8.dp.toPx()))
                            drawRect(color = Color.Black, topLeft = Offset(sizePx - boxSize + 8.dp.toPx(), 8.dp.toPx()), size = Size(boxSize - 16.dp.toPx(), boxSize - 16.dp.toPx()))

                            // Bottom Left Box
                            drawRect(color = Color.Black, topLeft = Offset(0f, sizePx - boxSize), size = Size(boxSize, boxSize))
                            drawRect(color = Color.White, topLeft = Offset(4.dp.toPx(), sizePx - boxSize + 4.dp.toPx()), size = Size(boxSize - 8.dp.toPx(), boxSize - 8.dp.toPx()))
                            drawRect(color = Color.Black, topLeft = Offset(8.dp.toPx(), sizePx - boxSize + 8.dp.toPx()), size = Size(boxSize - 16.dp.toPx(), boxSize - 16.dp.toPx()))

                            // Mock QR payload matrix
                            val dotSpacing = sizePx / 12
                            val random = Random(42) // Constant seed for same QR pattern
                            for (row in 1..10) {
                                for (col in 1..10) {
                                    if (row <= 3 && col <= 3) continue
                                    if (row <= 3 && col >= 7) continue
                                    if (row >= 7 && col <= 3) continue

                                    if (random.nextBoolean()) {
                                        drawRect(
                                            color = if (random.nextInt(10) < 8) Color.Black else primaryColor,
                                            topLeft = Offset(col * dotSpacing, row * dotSpacing),
                                            size = Size(dotSpacing * 0.8f, dotSpacing * 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isScanning = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("scan_pass_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Camera Scan")
                    }
                } else if (isScanning) {
                    // Scanning state with live red laser line
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw animated laser scan line
                            drawLine(
                                color = Color.Red,
                                start = Offset(0f, laserY.dp.toPx()),
                                end = Offset(size.width, laserY.dp.toPx()),
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            isScanning = false
                            scanCompleted = true
                            onAttendanceMarked()
                        }

                        Text(
                            text = "Locating scanning bounds...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CircularProgressIndicator()
                } else {
                    // Scan Completed visual feedback
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Verified: $studentName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}


// --- Simulated Fee Payment & Receipt PDF View ---

@Composable
fun PaymentSimulationDialog(
    fee: FeePayment,
    onDismiss: () -> Unit,
    onPaymentSuccess: (String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var isPaying by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Secure Tuition Payment Gateway",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Amount Due: $${fee.amount}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.take(16) },
                    label = { Text("16-Digit Card Number") },
                    placeholder = { Text("4000 1234 5678 9010") },
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                    modifier = Modifier.fillMaxWidth().testTag("card_number_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = { cardExpiry = it.take(5) },
                        label = { Text("Expiry (MM/YY)") },
                        placeholder = { Text("12/28") },
                        modifier = Modifier.weight(1f).testTag("expiry_input")
                    )

                    OutlinedTextField(
                        value = cardCvv,
                        onValueChange = { cardCvv = it.take(3) },
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        modifier = Modifier.weight(1f).testTag("cvv_input")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isPaying) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (cardNumber.length >= 12) {
                                isPaying = true
                            }
                        },
                        enabled = cardNumber.isNotEmpty() && cardExpiry.isNotEmpty() && cardCvv.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().testTag("pay_confirm_button")
                    ) {
                        Text("Authorise payment of $${fee.amount}")
                    }
                }

                LaunchedEffect(isPaying) {
                    if (isPaying) {
                        kotlinx.coroutines.delay(2000)
                        isPaying = false
                        onPaymentSuccess("ONLINE")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptPdfDialog(
    fee: FeePayment,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White), // White paper receipt style
            modifier = Modifier.padding(8.dp).border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SUPER TUITION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = PrimaryLight
                        )
                        Text(
                            text = "Academic Excellence Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryLight.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Metadata list
                ReceiptRow("Invoice Receipt", "#INV-2026-${fee.id}")
                ReceiptRow("Date Generated", fee.paymentDate ?: "2026-06-24")
                ReceiptRow("Student Name", fee.studentName)
                ReceiptRow("Payment Status", fee.status)
                ReceiptRow("Mode of Payment", fee.paymentMethod ?: "OFFLINE")
                ReceiptRow("Transaction Reference", fee.transactionId ?: "TRX_MOCK_OFFLINE")

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Calculation Table
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Item Description", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                    Text("Total Price", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Academic Monthly Tuition Fees", fontSize = 12.sp, color = Color.DarkGray)
                    Text("$${fee.amount}", fontSize = 12.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Payment Paid", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black)
                    Text("$${fee.amount}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PrimaryLight)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download PDF Receipt")
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
