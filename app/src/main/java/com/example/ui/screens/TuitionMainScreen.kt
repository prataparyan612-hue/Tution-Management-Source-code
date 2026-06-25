package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionMainScreen(
    viewModel: TuitionViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe data flows
    val users by viewModel.allUsers.collectAsState()
    val students by viewModel.studentProfiles.collectAsState()
    val teachers by viewModel.teacherProfiles.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val attendance by viewModel.attendanceList.collectAsState()
    val feePayments by viewModel.feePayments.collectAsState()
    val classSchedules by viewModel.classSchedules.collectAsState()
    val reportCards by viewModel.reportCards.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val messages by viewModel.allMessages.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()

    // Nav Drawer and Tab State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedView by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Dialog & Interaction State
    var showStudentDialog by remember { mutableStateOf(false) }
    var showTeacherDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showReportCardDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf<FeePayment?>(null) }
    var showReceiptDialog by remember { mutableStateOf<FeePayment?>(null) }
    var selectedAiStudentId by remember { mutableStateOf<Long?>(null) }
    var activeChatPartner by remember { mutableStateOf<User?>(null) }

    // Sync State
    var isSyncing by remember { mutableStateOf(false) }
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            rotationAngle.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotationAngle.snapTo(0f)
        }
    }

    if (currentUser == null) {
        // Fallback or navigate to login
        return
    }

    val user = currentUser!!

    // Filter Navigation Options Based on Role
    val navItems = remember(user.role) {
        listOf(
            Triple("Dashboard", Icons.Default.Home, Screen.Dashboard),
            Triple("Students", Icons.Default.Person, Screen.StudentManagement),
            Triple("Teachers", Icons.Default.AccountBox, Screen.TeacherManagement),
            Triple("Attendance Register", Icons.Default.List, Screen.AttendanceSystem),
            Triple("Fees Tracker", Icons.Default.ShoppingCart, Screen.FeeManagement),
            Triple("Schedules & Timetable", Icons.Default.DateRange, Screen.ClassScheduling),
            Triple("Report Cards", Icons.Default.Star, Screen.ReportCards),
            Triple("Communication Boards", Icons.Default.Email, Screen.Communication),
            Triple("Documents Storage", Icons.Default.Send, Screen.Documents),
            Triple("Analytics Portal", Icons.Default.Settings, Screen.Analytics)
        ).filter { item ->
            when (user.role) {
                "STUDENT" -> item.third in listOf(Screen.Dashboard, Screen.ClassScheduling, Screen.ReportCards, Screen.Communication, Screen.Documents)
                "PARENT" -> item.third in listOf(Screen.Dashboard, Screen.ReportCards, Screen.FeeManagement, Screen.Communication)
                "TEACHER" -> item.third in listOf(Screen.Dashboard, Screen.StudentManagement, Screen.AttendanceSystem, Screen.ClassScheduling, Screen.ReportCards, Screen.Communication, Screen.Documents)
                else -> true // Admin has access to all screens
            }
        }
    }

    // Modal Drawer wrapper
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (user.role == "ADMIN") Icons.Default.Build else Icons.Default.AccountBox,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${user.role} Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(navItems) { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.second, contentDescription = null) },
                            label = { Text(item.first) },
                            selected = selectedView == item.third,
                            onClick = {
                                selectedView = item.third
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                            label = { Text("Sign Out Session") },
                            selected = false,
                            onClick = { viewModel.logout() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Super Tuition",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = when (selectedView) {
                                    Screen.Dashboard -> "Portal Overview"
                                    Screen.StudentManagement -> "Student Base Management"
                                    Screen.TeacherManagement -> "Teacher Faculty Board"
                                    Screen.AttendanceSystem -> "Daily Attendance Register"
                                    Screen.FeeManagement -> "Fees Ledger & Collections"
                                    Screen.ClassScheduling -> "Timetable & Batch Scheduling"
                                    Screen.ReportCards -> "Exam Results & AI Insights"
                                    Screen.Communication -> "Communication Forums"
                                    Screen.Documents -> "Secure Document Vault"
                                    Screen.Analytics -> "Corporate Analytics"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // Offline Sync simulation trigger button
                        IconButton(onClick = {
                            scope.launch {
                                isSyncing = true
                                kotlinx.coroutines.delay(1800)
                                isSyncing = false
                                Toast.makeText(context, "Database synced offline to secure cloud vault successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Cloud",
                                tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.rotate(rotationAngle.value)
                            )
                        }

                        // Light/Dark Toggle
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Star else Icons.Default.Settings,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Dynamic render of active tab viewport
                AnimatedContent(
                    targetState = selectedView,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    }
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.Dashboard -> DashboardScreen(
                            viewModel = viewModel,
                            user = user,
                            onNavigateTo = { selectedView = it },
                            onTriggerReminders = {
                                Toast.makeText(context, "Smart reminders pushed to outstanding parent accounts!", Toast.LENGTH_LONG).show()
                            },
                            onShowQr = { showQrDialog = true }
                        )
                        Screen.StudentManagement -> StudentManagementScreen(
                            viewModel = viewModel,
                            students = students,
                            users = users,
                            batches = batches,
                            onShowAddDialog = { showStudentDialog = true }
                        )
                        Screen.TeacherManagement -> TeacherManagementScreen(
                            viewModel = viewModel,
                            teachers = teachers,
                            users = users,
                            onShowAddDialog = { showTeacherDialog = true }
                        )
                        Screen.AttendanceSystem -> AttendanceScreen(
                            viewModel = viewModel,
                            users = users,
                            attendance = attendance,
                            batches = batches,
                            onShowQrScan = { showQrDialog = true }
                        )
                        Screen.FeeManagement -> FeeTrackerScreen(
                            viewModel = viewModel,
                            feePayments = feePayments,
                            students = users.filter { it.role == "STUDENT" },
                            onShowPayDialog = { showPaymentDialog = it },
                            onShowInvoice = { showReceiptDialog = it },
                            onAddInvoice = {
                                Toast.makeText(context, "Batch invoices successfully computed & posted!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Screen.ClassScheduling -> ClassScheduleScreen(
                            viewModel = viewModel,
                            batches = batches,
                            schedules = classSchedules,
                            onShowAddDialog = { showScheduleDialog = true }
                        )
                        Screen.ReportCards -> ReportCardsScreen(
                            viewModel = viewModel,
                            users = users,
                            reportCards = reportCards,
                            attendance = attendance,
                            selectedAiStudentId = selectedAiStudentId,
                            onSelectAiStudent = { selectedAiStudentId = it },
                            onShowAddDialog = { showReportCardDialog = true }
                        )
                        Screen.Communication -> CommScreen(
                            viewModel = viewModel,
                            users = users,
                            announcements = announcements,
                            messages = messages,
                            activeChatPartner = activeChatPartner,
                            onSelectChatPartner = { activeChatPartner = it },
                            onShowPostAnnouncement = { showAnnouncementDialog = true }
                        )
                        Screen.Documents -> DocumentsScreen(
                            viewModel = viewModel,
                            users = users,
                            documents = documents,
                            onShowUpload = { showUploadDialog = true }
                        )
                        Screen.Analytics -> AnalyticsScreen(
                            viewModel = viewModel,
                            feePayments = feePayments,
                            attendance = attendance
                        )
                        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("In Progress") }
                    }
                }

                // --- Dialog Overlays ---

                if (showStudentDialog) {
                    AddStudentDialog(
                        batches = batches,
                        onDismiss = { showStudentDialog = false },
                        onSubmit = { studentUser, profile ->
                            viewModel.saveStudentProfile(studentUser, profile)
                            showStudentDialog = false
                        }
                    )
                }

                if (showTeacherDialog) {
                    AddTeacherDialog(
                        onDismiss = { showTeacherDialog = false },
                        onSubmit = { teacherUser, profile ->
                            viewModel.saveTeacherProfile(teacherUser, profile)
                            showTeacherDialog = false
                        }
                    )
                }

                if (showScheduleDialog) {
                    AddScheduleDialog(
                        batches = batches,
                        onDismiss = { showScheduleDialog = false },
                        onSubmit = { batchId, batchName, subject, day, time, room ->
                            viewModel.addClassSchedule(batchId, batchName, subject, day, time, room)
                            showScheduleDialog = false
                        }
                    )
                }

                if (showReportCardDialog) {
                    AddReportCardDialog(
                        students = users.filter { it.role == "STUDENT" },
                        onDismiss = { showReportCardDialog = false },
                        onSubmit = { studId, name, exam, subject, marks, max, grade, remarks ->
                            viewModel.addReportCardEntry(studId, name, exam, subject, marks, max, grade, remarks)
                            showReportCardDialog = false
                        }
                    )
                }

                if (showAnnouncementDialog) {
                    AddAnnouncementDialog(
                        onDismiss = { showAnnouncementDialog = false },
                        onSubmit = { title, content, target ->
                            viewModel.postAnnouncement(title, content, target)
                            showAnnouncementDialog = false
                        }
                    )
                }

                if (showUploadDialog) {
                    UploadDocumentDialog(
                        students = users.filter { it.role == "STUDENT" },
                        onDismiss = { showUploadDialog = false },
                        onSubmit = { studentId, title, file, size ->
                            viewModel.uploadStudentDocument(studentId, title, file, size)
                            showUploadDialog = false
                        }
                    )
                }

                if (showQrDialog) {
                    // Pick default student or mock user for scan check
                    val mockName = users.find { it.role == "STUDENT" }?.name ?: "Alex Mercer"
                    val mockId = users.find { it.role == "STUDENT" }?.id ?: 4
                    QrAttendanceDialog(
                        studentName = mockName,
                        onDismiss = { showQrDialog = false },
                        onAttendanceMarked = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val todayStr = sdf.format(Date())
                            viewModel.markAttendance(mockId, isTeacher = false, date = todayStr, status = "PRESENT")
                            Toast.makeText(context, "QR Scan: Marked $mockName as PRESENT!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                showPaymentDialog?.let { fee ->
                    PaymentSimulationDialog(
                        fee = fee,
                        onDismiss = { showPaymentDialog = null },
                        onPaymentSuccess = { method ->
                            viewModel.payFeeOnline(fee.id, method) { success ->
                                if (success) {
                                    showPaymentDialog = null
                                    showReceiptDialog = fee.copy(status = "PAID", paymentMethod = method)
                                }
                            }
                        }
                    )
                }

                showReceiptDialog?.let { fee ->
                    ReceiptPdfDialog(
                        fee = fee,
                        onDismiss = { showReceiptDialog = null }
                    )
                }
            }
        }
    }
}

private val Int.soul: Int get() = this // Helper infix replacement

// --- Screen Viewports ---

@Composable
fun DashboardScreen(
    viewModel: TuitionViewModel,
    user: User,
    onNavigateTo: (Screen) -> Unit,
    onTriggerReminders: () -> Unit,
    onShowQr: () -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    val feePayments by viewModel.feePayments.collectAsState()
    val schedules by viewModel.classSchedules.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    val studentCount = users.count { it.role == "STUDENT" }
    val teacherCount = users.count { it.role == "TEACHER" }
    val totalIncome = feePayments.filter { it.status == "PAID" }.sumOf { it.amount }
    val pendingFees = feePayments.filter { it.status == "PENDING" || it.status == "OVERDUE" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome Back,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Portal clearance validated for Role: ${user.role}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Analytical Metrics Grid (Responsive/Grid Layout)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricStatCard(
                        title = "Total Students",
                        value = studentCount.toString(),
                        subText = "+12% MoM",
                        icon = Icons.Default.Person,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Total Teachers",
                        value = teacherCount.toString(),
                        subText = "+4% MoM",
                        icon = Icons.Default.AccountBox,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricStatCard(
                        title = "Monthly Income",
                        value = "$$totalIncome",
                        subText = "100% cloud",
                        icon = Icons.Default.ShoppingCart,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Pending Fees",
                        value = "$$pendingFees",
                        subText = "Smart reminders",
                        icon = Icons.Default.ShoppingCart,
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Extra Premium Actions Bar
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Premium Automated Actions",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onShowQr,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("dashboard_qr_action")
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QR Register", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (user.role == "ADMIN" || user.role == "TEACHER") {
                            Button(
                                onClick = onTriggerReminders,
                                colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("dashboard_reminders_action")
                            ) {
                                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Reminder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Charts Snippet
        item {
            IncomeLineChart(modifier = Modifier.fillMaxWidth())
        }

        // Attendance & Grades split
        item {
            AttendanceBarChart(modifier = Modifier.fillMaxWidth())
        }

        // Upcoming Schedules
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Class Timetable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View All",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.clickable { onNavigateTo(Screen.ClassScheduling) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (schedules.isEmpty()) {
                        Text(
                            "No active schedules found.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        schedules.take(3).forEach { sched ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            sched.subject,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            sched.batchName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = sched.timeSlot,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent System Announcements
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Announcements Bulletin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (announcements.isEmpty()) {
                        Text("No notifications compiled.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        announcements.take(2).forEach { board ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    board.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    board.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Posted on: ${board.dateString} by ${board.postedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Divider(modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StudentManagementScreen(
    viewModel: TuitionViewModel,
    students: List<StudentProfile>,
    users: List<User>,
    batches: List<Batch>,
    onShowAddDialog: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) }
    var isDropdownOpen by remember { mutableStateOf(false) }

    // Map profiles against users
    val studentDetails = remember(students, users, searchQuery, selectedBatchFilter) {
        students.mapNotNull { profile ->
            val user = users.find { it.id == profile.userId && it.role == "STUDENT" }
            if (user != null) {
                Pair(user, profile)
            } else null
        }.filter { (user, profile) ->
            val nameMatch = user.name.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true)
            val batchMatch = selectedBatchFilter == null || profile.batchId == selectedBatchFilter
            nameMatch && batchMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Active Students (${studentDetails.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onShowAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp).testTag("add_student_fab")
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search and Filters layout
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by student name...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().testTag("student_search_bar"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lazy grid/list of student profiles
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (studentDetails.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No matching students registered.", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(studentDetails) { (studentUser, profile) ->
                    val batchName = batches.find { it.id == profile.batchId }?.name ?: "General Batch"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    studentUser.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    studentUser.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Roll: ${profile.admissionNo} | ${profile.grade}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    "Batch: $batchName",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress tracking rating bar
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Academic Index: ",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    LinearProgressIndicator(
                                        progress = profile.trackingProgress / 100f,
                                        color = SuccessGreen,
                                        trackColor = Color.LightGray.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        " ${profile.trackingProgress}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteStudent(studentUser.id, profile.id)
                                },
                                modifier = Modifier.testTag("delete_student_${studentUser.id}")
                            ) {
                                Icon(Icons.Default.Delete, null, tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TeacherManagementScreen(
    viewModel: TuitionViewModel,
    teachers: List<TeacherProfile>,
    users: List<User>,
    onShowAddDialog: () -> Unit
) {
    val teacherDetails = remember(teachers, users) {
        teachers.mapNotNull { profile ->
            val user = users.find { it.id == profile.userId && it.role == "TEACHER" }
            if (user != null) Pair(user, profile) else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Faculty Roster (${teacherDetails.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onShowAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp).testTag("add_teacher_fab")
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(teacherDetails) { (teacherUser, profile) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                teacherUser.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                teacherUser.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                profile.qualification,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                "Department: ${profile.subject}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Allocated Monthly Salary: $${profile.salary}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Paid/Unpaid salary status switcher
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (profile.trackingSalaryPaid) SuccessGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.clickable {
                                    viewModel.updateTeacherProfile(
                                        teacherUser,
                                        profile.copy(trackingSalaryPaid = !profile.trackingSalaryPaid)
                                    )
                                }
                            ) {
                                Text(
                                    text = if (profile.trackingSalaryPaid) "PAID" else "UNPAID",
                                    color = if (profile.trackingSalaryPaid) SuccessGreen else WarningOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(onClick = { viewModel.deleteTeacher(teacherUser.id, profile.id) }) {
                                Icon(Icons.Default.Delete, null, tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AttendanceScreen(
    viewModel: TuitionViewModel,
    users: List<User>,
    attendance: List<Attendance>,
    batches: List<Batch>,
    onShowQrScan: () -> Unit
) {
    var selectedDate by remember { mutableStateOf("") }
    var isTeacherRegistry by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    if (selectedDate.isEmpty()) {
        selectedDate = sdf.format(Date())
    }

    // Filter students or teachers
    val filteredPeople = remember(users, isTeacherRegistry) {
        users.filter { if (isTeacherRegistry) it.role == "TEACHER" else it.role == "STUDENT" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attendance Registry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // QR Attendance Trigger
            Button(
                onClick = onShowQrScan,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("QR Scan Gate")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Tab Bar
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isTeacherRegistry = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isTeacherRegistry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Students Register", color = if (!isTeacherRegistry) Color.White else MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isTeacherRegistry = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTeacherRegistry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Faculty Register", color = if (isTeacherRegistry) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Current Working Date: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(selectedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of People to mark attendance
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPeople) { person ->
                val record = attendance.find { it.personId == person.id && it.isTeacher == isTeacherRegistry && it.date == selectedDate }
                val activeStatus = record?.status ?: "PENDING"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(person.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(person.email, fontSize = 11.sp, color = Color.Gray)
                        }

                        // Mark buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1.9f)
                        ) {
                            StatusTagButton("PRESENT", SuccessGreen, activeStatus == "PRESENT") {
                                viewModel.markAttendance(person.id, isTeacherRegistry, selectedDate, "PRESENT")
                            }
                            StatusTagButton("LATE", WarningOrange, activeStatus == "LATE") {
                                viewModel.markAttendance(person.id, isTeacherRegistry, selectedDate, "LATE")
                            }
                            StatusTagButton("ABSENT", ErrorRed, activeStatus == "ABSENT") {
                                viewModel.markAttendance(person.id, isTeacherRegistry, selectedDate, "ABSENT")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTagButton(
    label: String,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) color else Color.LightGray.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.take(1),
            color = if (isActive) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}


@Composable
fun FeeTrackerScreen(
    viewModel: TuitionViewModel,
    feePayments: List<FeePayment>,
    students: List<User>,
    onShowPayDialog: (FeePayment) -> Unit,
    onShowInvoice: (FeePayment) -> Unit,
    onAddInvoice: () -> Unit
) {
    var isFilterPaid by remember { mutableStateOf(false) }

    val filteredFees = remember(feePayments, isFilterPaid) {
        feePayments.filter { if (isFilterPaid) it.status == "PAID" else it.status == "PENDING" || it.status == "OVERDUE" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Fees & Billings Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = onAddInvoice) {
                Icon(Icons.Default.ShoppingCart, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Monthly Bills")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ledger Toggle Bar
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isFilterPaid = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isFilterPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Outstanding Debts", color = if (!isFilterPaid) Color.White else MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isFilterPaid = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFilterPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Payment Receipts", color = if (isFilterPaid) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredFees.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No records compiled in this ledger segment.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredFees) { fee ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    fee.studentName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Invoice #INV-2026-${fee.id} | Due: ${fee.dueDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    "Amount Due: $${fee.amount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (fee.status == "PAID") SuccessGreen else PendingGold
                                )
                            }

                            if (fee.status == "PENDING" || fee.status == "OVERDUE") {
                                Button(
                                    onClick = { onShowPayDialog(fee) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("pay_online_btn_${fee.id}")
                                ) {
                                    Icon(Icons.Default.ShoppingCart, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pay Online")
                                }
                            } else {
                                IconButton(onClick = { onShowInvoice(fee) }) {
                                    Icon(Icons.Default.Check, null, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ClassScheduleScreen(
    viewModel: TuitionViewModel,
    batches: List<Batch>,
    schedules: List<ClassSchedule>,
    onShowAddDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Timetable & Schedules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onShowAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp).testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (schedules.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No active lecture schedules configured.", color = Color.Gray)
                    }
                }
            } else {
                items(schedules) { sched ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    sched.subject,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    sched.batchName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Room: ${sched.room} | Day: ${sched.dayOfWeek}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = sched.timeSlot,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = { viewModel.removeClassSchedule(sched.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ReportCardsScreen(
    viewModel: TuitionViewModel,
    users: List<User>,
    reportCards: List<ReportCard>,
    attendance: List<Attendance>,
    selectedAiStudentId: Long?,
    onSelectAiStudent: (Long) -> Unit,
    onShowAddDialog: () -> Unit
) {
    val students = remember(users) { users.filter { it.role == "STUDENT" } }
    val isGeneratingAi by viewModel.isGeneratingAiInsights.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Report Cards & Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onShowAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp).testTag("add_marks_fab")
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Performance Analysis Gateway Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI-Powered Performance Insights", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Prompt Google Gemini to dynamically evaluate selected student grades and generate supportive recommended action reviews.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select student dropdown mock list
                Text("Select Target Student for AI Review:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    students.forEach { stud ->
                        val isSelected = selectedAiStudentId == stud.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelectAiStudent(stud.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                stud.name.split(" ").firstOrNull() ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedAiStudentId != null) {
                    Button(
                        onClick = { viewModel.generateAiFeedback(selectedAiStudentId) },
                        enabled = !isGeneratingAi,
                        modifier = Modifier.fillMaxWidth().testTag("trigger_ai_insights_button")
                    ) {
                        if (isGeneratingAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Star, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compute Gemini Analytics report")
                        }
                    }
                }

                // AI Response card
                AnimatedVisibility(visible = aiInsights != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gemini Live Review", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiInsights ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Standard Report Card List
        Text("Recorded Report Sheets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (reportCards.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No reports posted.", color = Color.Gray)
                    }
                }
            } else {
                items(reportCards) { card ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                        card.studentName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${card.examName} | ${card.subject}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        card.grade,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Result Mark: ${card.marksObtained}/${card.maxMarks}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Instructor Remarks: ${card.remarks}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { viewModel.deleteReportCard(card.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CommScreen(
    viewModel: TuitionViewModel,
    users: List<User>,
    announcements: List<Announcement>,
    messages: List<Message>,
    activeChatPartner: User?,
    onSelectChatPartner: (User?) -> Unit,
    onShowPostAnnouncement: () -> Unit
) {
    var isAnnouncementTab by remember { mutableStateOf(true) }
    var chatInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Communication Center",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isAnnouncementTab) {
                FloatingActionButton(
                    onClick = onShowPostAnnouncement,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp).testTag("post_announcement_fab")
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comm Switcher
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isAnnouncementTab = true; onSelectChatPartner(null) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnnouncementTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Board Bulletin", color = if (isAnnouncementTab) Color.White else MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isAnnouncementTab = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isAnnouncementTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Direct Messages", color = if (!isAnnouncementTab) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnnouncementTab) {
            // Renders Bulletin
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(announcements) { board ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    board.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
                                    Text(
                                        text = board.targetRole,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(board.content, style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "By: ${board.postedBy} | ${board.dateString}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )

                                IconButton(onClick = { viewModel.deleteAnnouncement(board.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Direct chat panel
            if (activeChatPartner == null) {
                // List users to message
                Text("Choose Portal contact to message:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users) { usr ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectChatPartner(usr) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(usr.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(usr.role, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } else {
                // Conversation
                val meId = viewModel.currentUser.value?.id ?: 0
                val conversation = remember(messages, activeChatPartner) {
                    messages.filter {
                        (it.senderId == meId && it.receiverId == activeChatPartner.id) ||
                                (it.senderId == activeChatPartner.id && it.receiverId == meId)
                    }.sortedBy { it.timestamp }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onSelectChatPartner(null) }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                        Text(activeChatPartner.name, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(conversation) { msg ->
                            val isMe = msg.senderId == meId
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isMe) 12.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 12.dp
                                    )
                                ) {
                                    Text(
                                        msg.content,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Write message...") },
                            modifier = Modifier.weight(1f).testTag("chat_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    viewModel.sendChatMessage(activeChatPartner.id, activeChatPartner.name, chatInput)
                                    chatInput = ""
                                }
                            },
                            modifier = Modifier.testTag("send_chat_btn")
                        ) {
                            Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DocumentsScreen(
    viewModel: TuitionViewModel,
    users: List<User>,
    documents: List<StudentDocument>,
    onShowUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Documents Vault",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = onShowUpload) {
                Icon(Icons.Default.Send, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Doc")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (documents.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("Vault is empty. Add homework files, templates, or syllabi.", color = Color.Gray)
                    }
                }
            } else {
                items(documents) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = ErrorRed, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        doc.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${doc.fileName} | Size: ${doc.fileSize} | Posted: ${doc.uploadDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    // Simulation download click
                                }) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = { viewModel.deleteStudentDocument(doc.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AnalyticsScreen(
    viewModel: TuitionViewModel,
    feePayments: List<FeePayment>,
    attendance: List<Attendance>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Consolidated Metrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            IncomeLineChart(modifier = Modifier.fillMaxWidth())
        }

        item {
            AttendanceBarChart(modifier = Modifier.fillMaxWidth())
        }

        item {
            GradeDistributionPie(modifier = Modifier.fillMaxWidth())
        }
    }
}

// --- Dynamic Form Dialog Components ---

@Composable
fun AddStudentDialog(
    batches: List<Batch>,
    onDismiss: () -> Unit,
    onSubmit: (User, StudentProfile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("student123") }
    var phone by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var selectedBatchId by remember { mutableStateOf(1L) }
    var grade by remember { mutableStateOf("Grade 10") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Register Student Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") }, modifier = Modifier.fillMaxWidth().testTag("add_stu_name"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = parentName, onValueChange = { parentName = it }, label = { Text("Parent Guardian Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = parentPhone, onValueChange = { parentPhone = it }, label = { Text("Parent Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade Level") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            val user = User(email = email, passwordHash = pass, name = name, role = "STUDENT", phone = phone, isVerified = true)
                            val profile = StudentProfile(userId = 0, batchId = selectedBatchId, parentName = parentName, parentPhone = parentPhone, admissionNo = "STU-2026-${(100..999).random()}", grade = grade)
                            onSubmit(user, profile)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_stu_submit")
                ) {
                    Text("Save Student")
                }
            }
        }
    }
}


@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onSubmit: (User, TeacherProfile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("teacher123") }
    var phone by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var qual by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Register Faculty Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Faculty Name") }, modifier = Modifier.fillMaxWidth().testTag("add_teach_name"))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Specialist Department") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = qual, onValueChange = { qual = it }, label = { Text("Degree / Qualification") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Monthly Contract Salary ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            val user = User(email = email, passwordHash = pass, name = name, role = "TEACHER", phone = phone, isVerified = true)
                            val profile = TeacherProfile(userId = 0, subject = subject, qualification = qual, salary = salary.toDoubleOrNull() ?: 3000.0)
                            onSubmit(user, profile)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_teach_submit")
                ) {
                    Text("Save Faculty Profile")
                }
            }
        }
    }
}


@Composable
fun AddScheduleDialog(
    batches: List<Batch>,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String, String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("Monday") }
    var timeSlot by remember { mutableStateOf("10:00 - 12:00") }
    var room by remember { mutableStateOf("Alpha Room") }
    var selectedBatchId by remember { mutableStateOf(1L) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Class Schedule Slot", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day of Week") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("Time Slot (HH:MM - HH:MM)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room / Location") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (subject.isNotEmpty()) {
                            val batchName = batches.find { it.id == selectedBatchId }?.name ?: "Elite Batch"
                            onSubmit(selectedBatchId, batchName, subject, day, timeSlot, room)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schedule Slot")
                }
            }
        }
    }
}


@Composable
fun AddReportCardDialog(
    students: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String, String, Int, Int, String, String) -> Unit
) {
    var exam by remember { mutableStateOf("Mid-Term Examination 2026") }
    var subject by remember { mutableStateOf("Mathematics") }
    var score by remember { mutableStateOf("") }
    var max by remember { mutableStateOf("100") }
    var grade by remember { mutableStateOf("A") }
    var remarks by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableStateOf(4L) }

    // Dropdown selection state
    if (selectedStudentId == 4L && students.isNotEmpty()) {
        selectedStudentId = students.first().id
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Log Exam Marks", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = exam, onValueChange = { exam = it }, label = { Text("Exam Label") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = score, onValueChange = { score = it }, label = { Text("Marks Obtained") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = max, onValueChange = { max = it }, label = { Text("Max Marks") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade (A/B/C/F)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Teacher Remarks") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val stud = students.find { it.id == selectedStudentId }
                        if (stud != null && score.isNotEmpty()) {
                            onSubmit(
                                selectedStudentId,
                                stud.name,
                                exam,
                                subject,
                                score.toIntOrNull() ?: 85,
                                max.toIntOrNull() ?: 100,
                                grade,
                                remarks
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Record Marks Sheet")
                }
            }
        }
    }
}


@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("ALL") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Publish Announcement", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Subject Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Message Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target Audience (ALL/TEACHER/STUDENT/PARENT)") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            onSubmit(title, content, target)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Push Announcement")
                }
            }
        }
    }
}


@Composable
fun UploadDocumentDialog(
    students: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var file by remember { mutableStateOf("limit_homework_2.pdf") }
    var size by remember { mutableStateOf("1.5 MB") }
    var selectedStudentId by remember { mutableStateOf(4L) }

    if (selectedStudentId == 4L && students.isNotEmpty()) {
        selectedStudentId = students.first().id
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Secure Document Upload", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Document Label") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = file, onValueChange = { file = it }, label = { Text("Filename (.pdf / .docx)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Simulated File Size") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            onSubmit(selectedStudentId, title, file, size)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload to Vault")
                }
            }
        }
    }
}
