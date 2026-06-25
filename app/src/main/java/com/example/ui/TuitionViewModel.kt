package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TuitionDatabase
import com.example.data.model.*
import com.example.data.repository.TuitionRepository
import com.example.network.GeminiAiHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface Screen {
    object Login : Screen
    object Signup : Screen
    object ForgotPassword : Screen
    object OtpVerification : Screen
    object Dashboard : Screen
    object StudentManagement : Screen
    object TeacherManagement : Screen
    object AttendanceSystem : Screen
    object FeeManagement : Screen
    object ClassScheduling : Screen
    object ReportCards : Screen
    object Communication : Screen
    object Documents : Screen
    object Analytics : Screen
}

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TuitionRepository
    
    // UI Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen

    // Pre-filled signup email to preserve flow into OTP
    var tempSignupUser: User? = null

    // Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Database Reactive Flows
    val allUsers: StateFlow<List<User>>
    val studentProfiles: StateFlow<List<StudentProfile>>
    val teacherProfiles: StateFlow<List<TeacherProfile>>
    val batches: StateFlow<List<Batch>>
    val attendanceList: StateFlow<List<Attendance>>
    val feePayments: StateFlow<List<FeePayment>>
    val classSchedules: StateFlow<List<ClassSchedule>>
    val reportCards: StateFlow<List<ReportCard>>
    val announcements: StateFlow<List<Announcement>>
    val allMessages: StateFlow<List<Message>>
    val allDocuments: StateFlow<List<StudentDocument>>

    // AI Performance Analytics State
    private val _isGeneratingAiInsights = MutableStateFlow(false)
    val isGeneratingAiInsights: StateFlow<Boolean> = _isGeneratingAiInsights

    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights: StateFlow<String?> = _aiInsights

    init {
        val database = TuitionDatabase.getDatabase(application)
        repository = TuitionRepository(database.tuitionDao())

        // Start database seeding and fetch initial flows
        viewModelScope.launch {
            repository.autoSeedDatabase()
        }

        allUsers = repository.getAllUsersFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        studentProfiles = repository.getAllStudentProfilesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        teacherProfiles = repository.getAllTeacherProfilesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        batches = repository.getAllBatchesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        attendanceList = repository.getAllAttendanceFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        feePayments = repository.getAllFeePaymentsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        classSchedules = repository.getAllClassSchedulesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        reportCards = repository.getAllReportCardsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        announcements = repository.getAllAnnouncementsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allMessages = repository.getAllMessagesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allDocuments = repository.getAllDocumentsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- Navigation Controls ---
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Authentication ---
    fun login(email: String, passwordHash: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                onResult(false, "User does not exist. Please register.")
            } else if (user.passwordHash != passwordHash) {
                onResult(false, "Invalid credentials. Please retry.")
            } else if (!user.isVerified) {
                tempSignupUser = user
                _currentScreen.value = Screen.OtpVerification
                onResult(false, "Account is unverified. Verify OTP.")
            } else {
                _currentUser.value = user
                _currentScreen.value = Screen.Dashboard
                onResult(true, "Successfully authenticated.")
            }
        }
    }

    fun signup(email: String, passwordHash: String, name: String, role: String, phone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onResult(false, "Email is already registered.")
                return@launch
            }

            // Generate a simple 4 digit OTP for demonstration
            val otpCode = String.format("%04d", (1000..9999).random())
            val newUser = User(
                email = email,
                passwordHash = passwordHash,
                name = name,
                role = role,
                phone = phone,
                otpCode = otpCode,
                isVerified = false
            )

            val insertedId = repository.insertUser(newUser)
            val finalUser = newUser.copy(id = insertedId)
            tempSignupUser = finalUser

            // Link profile tables automatically
            if (role == "TEACHER") {
                repository.insertTeacherProfile(TeacherProfile(
                    userId = insertedId,
                    subject = "General Electives",
                    qualification = "Trained Professional",
                    salary = 3000.00
                ))
            } else if (role == "STUDENT") {
                repository.insertStudentProfile(StudentProfile(
                    userId = insertedId,
                    batchId = 1, // Default fallback batch
                    parentName = "Guardian of $name",
                    parentPhone = phone,
                    admissionNo = "STU-2026-${String.format("%03d", (10..999).random())}",
                    grade = "Grade 10"
                ))
            }

            _currentScreen.value = Screen.OtpVerification
            onResult(true, "OTP verification code sent: $otpCode")
        }
    }

    fun verifyOtp(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = tempSignupUser
            if (user == null) {
                onResult(false, "No active session in signup.")
                return@launch
            }

            if (user.otpCode == code || code == "1234") { // Allow standard demo fallback 1234
                val verifiedUser = user.copy(isVerified = true)
                repository.updateUser(verifiedUser)
                _currentUser.value = verifiedUser
                _currentScreen.value = Screen.Dashboard
                onResult(true, "Verification successful.")
            } else {
                onResult(false, "Incorrect OTP. Retry.")
            }
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                onResult(false, "Account not found.")
            } else {
                onResult(true, "Reset link dispatched to: $email")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        tempSignupUser = null
        _aiInsights.value = null
        _currentScreen.value = Screen.Login
    }


    // --- Student Profile Operations ---
    fun saveStudentProfile(studentUser: User, profile: StudentProfile) {
        viewModelScope.launch {
            val userId = repository.insertUser(studentUser)
            val updatedProfile = profile.copy(userId = userId)
            repository.insertStudentProfile(updatedProfile)
        }
    }

    fun updateStudentProfile(studentUser: User, profile: StudentProfile) {
        viewModelScope.launch {
            repository.updateUser(studentUser)
            repository.updateStudentProfile(profile)
        }
    }

    fun deleteStudent(studentUserId: Long, profileId: Long) {
        viewModelScope.launch {
            repository.deleteUserById(studentUserId)
            repository.deleteStudentProfileById(profileId)
        }
    }


    // --- Teacher Profile Operations ---
    fun saveTeacherProfile(teacherUser: User, profile: TeacherProfile) {
        viewModelScope.launch {
            val userId = repository.insertUser(teacherUser)
            val updatedProfile = profile.copy(userId = userId)
            repository.insertTeacherProfile(updatedProfile)
        }
    }

    fun updateTeacherProfile(teacherUser: User, profile: TeacherProfile) {
        viewModelScope.launch {
            repository.updateUser(teacherUser)
            repository.updateTeacherProfile(profile)
        }
    }

    fun deleteTeacher(teacherUserId: Long, profileId: Long) {
        viewModelScope.launch {
            repository.deleteUserById(teacherUserId)
            repository.deleteTeacherProfileById(profileId)
        }
    }


    // --- Batch Operations ---
    fun addBatch(name: String, subject: String, teacherId: Long, scheduleTime: String) {
        viewModelScope.launch {
            repository.insertBatch(Batch(
                name = name,
                subject = subject,
                teacherId = teacherId,
                scheduleTime = scheduleTime
            ))
        }
    }

    fun deleteBatch(id: Long) {
        viewModelScope.launch {
            repository.deleteBatchById(id)
        }
    }


    // --- Attendance Operations ---
    fun markAttendance(personId: Long, isTeacher: Boolean, date: String, status: String) {
        viewModelScope.launch {
            // Check if there is already an entry for this person on this date
            val allAtt = attendanceList.value
            val existing = allAtt.find { it.personId == personId && it.isTeacher == isTeacher && it.date == date }
            if (existing != null) {
                repository.deleteAttendanceById(existing.id)
            }
            repository.insertAttendance(Attendance(
                personId = personId,
                isTeacher = isTeacher,
                date = date,
                status = status
            ))
        }
    }


    // --- Fee Collection Operations ---
    fun addFeeInvoice(studentId: Long, name: String, amount: Double, dueDate: String) {
        viewModelScope.launch {
            repository.insertFeePayment(FeePayment(
                studentId = studentId,
                studentName = name,
                amount = amount,
                dueDate = dueDate,
                status = "PENDING"
            ))
        }
    }

    fun payFeeOnline(feeId: Long, method: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val fees = feePayments.value
            val fee = fees.find { it.id == feeId }
            if (fee != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                val txnId = "TXN_" + System.currentTimeMillis()

                val updatedFee = fee.copy(
                    paymentDate = todayStr,
                    status = "PAID",
                    paymentMethod = method,
                    transactionId = txnId
                )
                repository.updateFeePayment(updatedFee)
                callback(true)
            } else {
                callback(false)
            }
        }
    }


    // --- Timetable / Class Schedules ---
    fun addClassSchedule(batchId: Long, batchName: String, subject: String, day: String, timeSlot: String, room: String) {
        viewModelScope.launch {
            repository.insertClassSchedule(ClassSchedule(
                batchId = batchId,
                batchName = batchName,
                subject = subject,
                dayOfWeek = day,
                timeSlot = timeSlot,
                room = room
            ))
        }
    }

    fun removeClassSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteClassScheduleById(id)
        }
    }


    // --- Report Cards & Results ---
    fun addReportCardEntry(studentId: Long, name: String, exam: String, subject: String, marks: Int, max: Int, grade: String, remarks: String) {
        viewModelScope.launch {
            repository.insertReportCard(ReportCard(
                studentId = studentId,
                studentName = name,
                examName = exam,
                subject = subject,
                marksObtained = marks,
                maxMarks = max,
                grade = grade,
                remarks = remarks
            ))
        }
    }

    fun deleteReportCard(id: Long) {
        viewModelScope.launch {
            repository.deleteReportCardById(id)
        }
    }


    // --- Push Announcements ---
    fun postAnnouncement(title: String, content: String, targetRole: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            repository.insertAnnouncement(Announcement(
                title = title,
                content = content,
                dateString = dateStr,
                postedBy = _currentUser.value?.name ?: "Sarah Jenkins (Director)",
                targetRole = targetRole
            ))
        }
    }

    fun deleteAnnouncement(id: Long) {
        viewModelScope.launch {
            repository.deleteAnnouncementById(id)
        }
    }


    // --- Messaging Support ---
    fun sendChatMessage(receiverId: Long, receiverName: String, content: String) {
        val sender = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertMessage(Message(
                senderId = sender.id,
                receiverId = receiverId,
                senderName = sender.name,
                receiverName = receiverName,
                content = content,
                timestamp = System.currentTimeMillis()
            ))
        }
    }


    // --- Student Document Management ---
    fun uploadStudentDocument(studentId: Long, title: String, fileName: String, fileSize: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            repository.insertDocument(StudentDocument(
                studentId = studentId,
                title = title,
                fileName = fileName,
                fileSize = fileSize,
                uploadDate = todayStr,
                localUri = "content://tuition_manager/docs/$fileName"
            ))
        }
    }

    fun deleteStudentDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteDocumentById(id)
        }
    }


    // --- AI Student Performance Insights (Gemini integration) ---
    fun generateAiFeedback(studentUserId: Long) {
        val studentUser = allUsers.value.find { it.id == studentUserId }
        val profile = studentProfiles.value.find { it.userId == studentUserId }
        val cards = reportCards.value.filter { it.studentId == studentUserId }
        
        if (studentUser == null || profile == null) {
            _aiInsights.value = "Unable to find student profile."
            return
        }

        // Calculate custom attendance rate for student
        val studentAttendance = attendanceList.value.filter { it.personId == studentUserId && !it.isTeacher }
        val totalDays = studentAttendance.size
        val presentDays = studentAttendance.count { it.status == "PRESENT" || it.status == "LATE" }
        val rate = if (totalDays > 0) (presentDays.toFloat() / totalDays * 100) else 100.0f

        _isGeneratingAiInsights.value = true
        _aiInsights.value = null

        viewModelScope.launch {
            val response = GeminiAiHelper.generateStudentAnalytics(
                studentName = studentUser.name,
                grade = profile.grade,
                attendanceRate = rate,
                reportCards = cards
            )
            _aiInsights.value = response
            _isGeneratingAiInsights.value = false
        }
    }
}
