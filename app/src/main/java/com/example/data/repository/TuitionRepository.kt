package com.example.data.repository

import com.example.data.local.TuitionDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class TuitionRepository(private val tuitionDao: TuitionDao) {

    // --- User Delegates ---
    suspend fun insertUser(user: User): Long = tuitionDao.insertUser(user)
    suspend fun updateUser(user: User) = tuitionDao.updateUser(user)
    suspend fun getUserByEmail(email: String): User? = tuitionDao.getUserByEmail(email)
    fun getUserByIdFlow(id: Long): Flow<User?> = tuitionDao.getUserByIdFlow(id)
    suspend fun getUserById(id: Long): User? = tuitionDao.getUserById(id)
    fun getUsersByRoleFlow(role: String): Flow<List<User>> = tuitionDao.getUsersByRoleFlow(role)
    suspend fun getUsersByRole(role: String): List<User> = tuitionDao.getByRoleList(role)
    fun getAllUsersFlow(): Flow<List<User>> = tuitionDao.getAllUsersFlow()
    suspend fun deleteUserById(id: Long) = tuitionDao.deleteUserById(id)

    // Helper for non-flow list fetch
    private suspend fun TuitionDao.getByRoleList(role: String): List<User> = this.getUsersByRole(role)

    // --- Student Profile Delegates ---
    suspend fun insertStudentProfile(profile: StudentProfile): Long = tuitionDao.insertStudentProfile(profile)
    suspend fun updateStudentProfile(profile: StudentProfile) = tuitionDao.updateStudentProfile(profile)
    suspend fun getStudentProfileByUserId(userId: Long): StudentProfile? = tuitionDao.getStudentProfileByUserId(userId)
    fun getAllStudentProfilesFlow(): Flow<List<StudentProfile>> = tuitionDao.getAllStudentProfilesFlow()
    suspend fun deleteStudentProfileById(id: Long) = tuitionDao.deleteStudentProfileById(id)

    // --- Teacher Profile Delegates ---
    suspend fun insertTeacherProfile(profile: TeacherProfile): Long = tuitionDao.insertTeacherProfile(profile)
    suspend fun updateTeacherProfile(profile: TeacherProfile) = tuitionDao.updateTeacherProfile(profile)
    suspend fun getTeacherProfileByUserId(userId: Long): TeacherProfile? = tuitionDao.getTeacherProfileByUserId(userId)
    fun getAllTeacherProfilesFlow(): Flow<List<TeacherProfile>> = tuitionDao.getAllTeacherProfilesFlow()
    suspend fun deleteTeacherProfileById(id: Long) = tuitionDao.deleteTeacherProfileById(id)

    // --- Batch Delegates ---
    suspend fun insertBatch(batch: Batch): Long = tuitionDao.insertBatch(batch)
    fun getAllBatchesFlow(): Flow<List<Batch>> = tuitionDao.getAllBatchesFlow()
    suspend fun getBatchById(id: Long): Batch? = tuitionDao.getBatchById(id)
    suspend fun deleteBatchById(id: Long) = tuitionDao.deleteBatchById(id)

    // --- Attendance Delegates ---
    suspend fun insertAttendance(attendance: Attendance): Long = tuitionDao.insertAttendance(attendance)
    fun getAllAttendanceFlow(): Flow<List<Attendance>> = tuitionDao.getAllAttendanceFlow()
    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>> = tuitionDao.getAttendanceForDateFlow(date)
    fun getAttendanceForPersonFlow(personId: Long, isTeacher: Boolean): Flow<List<Attendance>> = 
        tuitionDao.getAttendanceForPersonFlow(personId, isTeacher)
    suspend fun deleteAttendanceById(id: Long) = tuitionDao.deleteAttendanceById(id)

    // --- Fee Payment Delegates ---
    suspend fun insertFeePayment(feePayment: FeePayment): Long = tuitionDao.insertFeePayment(feePayment)
    suspend fun updateFeePayment(feePayment: FeePayment) = tuitionDao.updateFeePayment(feePayment)
    fun getAllFeePaymentsFlow(): Flow<List<FeePayment>> = tuitionDao.getAllFeePaymentsFlow()
    fun getFeePaymentsByStudentFlow(studentId: Long): Flow<List<FeePayment>> = tuitionDao.getFeePaymentsByStudentFlow(studentId)
    suspend fun deleteFeePaymentById(id: Long) = tuitionDao.deleteFeePaymentById(id)

    // --- Class Schedule Delegates ---
    suspend fun insertClassSchedule(schedule: ClassSchedule): Long = tuitionDao.insertClassSchedule(schedule)
    fun getAllClassSchedulesFlow(): Flow<List<ClassSchedule>> = tuitionDao.getAllClassSchedulesFlow()
    fun getClassSchedulesByBatchFlow(batchId: Long): Flow<List<ClassSchedule>> = 
        tuitionDao.getClassSchedulesByBatchFlow(batchId)
    suspend fun deleteClassScheduleById(id: Long) = tuitionDao.deleteClassScheduleById(id)

    // --- Report Card Delegates ---
    suspend fun insertReportCard(reportCard: ReportCard): Long = tuitionDao.insertReportCard(reportCard)
    fun getAllReportCardsFlow(): Flow<List<ReportCard>> = tuitionDao.getAllReportCardsFlow()
    fun getReportCardsByStudentFlow(studentId: Long): Flow<List<ReportCard>> = 
        tuitionDao.getReportCardsByStudentFlow(studentId)
    suspend fun deleteReportCardById(id: Long) = tuitionDao.deleteReportCardById(id)

    // --- Announcement Delegates ---
    suspend fun insertAnnouncement(announcement: Announcement): Long = tuitionDao.insertAnnouncement(announcement)
    fun getAllAnnouncementsFlow(): Flow<List<Announcement>> = tuitionDao.getAllAnnouncementsFlow()
    suspend fun deleteAnnouncementById(id: Long) = tuitionDao.deleteAnnouncementById(id)

    // --- Messaging Delegates ---
    suspend fun insertMessage(message: Message): Long = tuitionDao.insertMessage(message)
    fun getConversationFlow(id1: Long, id2: Long): Flow<List<Message>> = tuitionDao.getConversationFlow(id1, id2)
    fun getAllMessagesFlow(): Flow<List<Message>> = tuitionDao.getAllMessagesFlow()

    // --- Student Document Delegates ---
    suspend fun insertDocument(document: StudentDocument): Long = tuitionDao.insertDocument(document)
    fun getDocumentsByStudentFlow(studentId: Long): Flow<List<StudentDocument>> = tuitionDao.getDocumentsByStudentFlow(studentId)
    fun getAllDocumentsFlow(): Flow<List<StudentDocument>> = tuitionDao.getAllDocumentsFlow()
    suspend fun deleteDocumentById(id: Long) = tuitionDao.deleteDocumentById(id)


    // --- Database Seeder ---
    suspend fun autoSeedDatabase() {
        // Only seed if there are zero users
        val existingAdmin = tuitionDao.getUserByEmail("admin@tuition.com")
        if (existingAdmin != null) return

        // 1. Seed Roles
        val adminId = tuitionDao.insertUser(User(
            email = "admin@tuition.com",
            passwordHash = "admin123",
            name = "Sarah Jenkins (Director)",
            role = "ADMIN",
            phone = "+1 (555) 019-2834",
            isVerified = true
        ))

        val teacher1Id = tuitionDao.insertUser(User(
            email = "teacher@tuition.com",
            passwordHash = "teacher123",
            name = "Prof. Marcus Vance",
            role = "TEACHER",
            phone = "+1 (555) 014-9921",
            isVerified = true
        ))

        val teacher2Id = tuitionDao.insertUser(User(
            email = "helen@tuition.com",
            passwordHash = "teacher123",
            name = "Dr. Helen Rostova",
            role = "TEACHER",
            phone = "+1 (555) 018-4411",
            isVerified = true
        ))

        val student1Id = tuitionDao.insertUser(User(
            email = "student@tuition.com",
            passwordHash = "student123",
            name = "Alex Mercer",
            role = "STUDENT",
            phone = "+1 (555) 017-3388",
            isVerified = true
        ))

        val student2Id = tuitionDao.insertUser(User(
            email = "emma@tuition.com",
            passwordHash = "student123",
            name = "Emma Watson",
            role = "STUDENT",
            phone = "+1 (555) 012-7744",
            isVerified = true
        ))

        val student3Id = tuitionDao.insertUser(User(
            email = "parent@tuition.com", // For the parent demo, they log in as parent, which accesses student Emma
            passwordHash = "parent123",
            name = "Robert Watson",
            role = "PARENT",
            phone = "+1 (555) 011-5566",
            isVerified = true
        ))

        // 2. Seed Profiles
        tuitionDao.insertTeacherProfile(TeacherProfile(
            userId = teacher1Id,
            subject = "Advanced Mathematics & Calculus",
            qualification = "Ph.D. in Applied Mathematics",
            salary = 4800.00,
            trackingSalaryPaid = true
        ))

        tuitionDao.insertTeacherProfile(TeacherProfile(
            userId = teacher2Id,
            subject = "Physics & Chemistry",
            qualification = "M.Sc. in Physics",
            salary = 4200.00,
            trackingSalaryPaid = false
        ))

        // 3. Seed Batches
        val batchMathId = tuitionDao.insertBatch(Batch(
            name = "Algebra & Calculus Elite",
            subject = "Mathematics",
            teacherId = teacher1Id,
            scheduleTime = "Mon, Wed, Fri 4:00 PM - 6:00 PM"
        ))

        val batchPhysicsId = tuitionDao.insertBatch(Batch(
            name = "AP Physics Advanced Mastery",
            subject = "Physics",
            teacherId = teacher2Id,
            scheduleTime = "Tue, Thu 5:00 PM - 7:00 PM"
        ))

        // 4. Seed Student Profiles
        tuitionDao.insertStudentProfile(StudentProfile(
            userId = student1Id,
            batchId = batchMathId,
            parentName = "Clarissa Mercer",
            parentPhone = "+1 (555) 019-2244",
            admissionNo = "STU-2026-001",
            grade = "Grade 11",
            trackingProgress = 88
        ))

        tuitionDao.insertStudentProfile(StudentProfile(
            userId = student2Id,
            batchId = batchPhysicsId,
            parentName = "Robert Watson",
            parentPhone = "+1 (555) 011-5566",
            admissionNo = "STU-2026-002",
            grade = "Grade 12",
            trackingProgress = 94
        ))

        // 5. Seed Attendance
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val todayStr = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dayBeforeStr = sdf.format(calendar.time)

        // Student 1 (Alex Mercer) attendance
        tuitionDao.insertAttendance(Attendance(personId = student1Id, isTeacher = false, date = todayStr, status = "PRESENT"))
        tuitionDao.insertAttendance(Attendance(personId = student1Id, isTeacher = false, date = yesterdayStr, status = "PRESENT"))
        tuitionDao.insertAttendance(Attendance(personId = student1Id, isTeacher = false, date = dayBeforeStr, status = "LATE"))

        // Student 2 (Emma Watson) attendance
        tuitionDao.insertAttendance(Attendance(personId = student2Id, isTeacher = false, date = todayStr, status = "PRESENT"))
        tuitionDao.insertAttendance(Attendance(personId = student2Id, isTeacher = false, date = yesterdayStr, status = "PRESENT"))
        tuitionDao.insertAttendance(Attendance(personId = student2Id, isTeacher = false, date = dayBeforeStr, status = "PRESENT"))

        // Teacher 1 (Marcus Vance) attendance
        tuitionDao.insertAttendance(Attendance(personId = teacher1Id, isTeacher = true, date = todayStr, status = "PRESENT"))
        tuitionDao.insertAttendance(Attendance(personId = teacher1Id, isTeacher = true, date = yesterdayStr, status = "PRESENT"))

        // Teacher 2 (Helen Rostova) attendance
        tuitionDao.insertAttendance(Attendance(personId = teacher2Id, isTeacher = true, date = todayStr, status = "PRESENT"))

        // 6. Seed Fee Payments
        tuitionDao.insertFeePayment(FeePayment(
            studentId = student1Id,
            studentName = "Alex Mercer",
            amount = 350.00,
            dueDate = todayStr,
            paymentDate = todayStr,
            status = "PAID",
            paymentMethod = "ONLINE",
            transactionId = "TXN_99210459821"
        ))

        tuitionDao.insertFeePayment(FeePayment(
            studentId = student1Id,
            studentName = "Alex Mercer",
            amount = 350.00,
            dueDate = "2026-07-05",
            paymentDate = null,
            status = "PENDING",
            paymentMethod = null,
            transactionId = null
        ))

        tuitionDao.insertFeePayment(FeePayment(
            studentId = student2Id,
            studentName = "Emma Watson",
            amount = 450.00,
            dueDate = todayStr,
            paymentDate = null,
            status = "PENDING"
        ))

        // 7. Seed Class Schedules
        tuitionDao.insertClassSchedule(ClassSchedule(
            batchId = batchMathId,
            batchName = "Algebra & Calculus Elite",
            subject = "Mathematics",
            dayOfWeek = "Monday",
            timeSlot = "16:00 - 18:00",
            room = "Alpha Room"
        ))

        tuitionDao.insertClassSchedule(ClassSchedule(
            batchId = batchMathId,
            batchName = "Algebra & Calculus Elite",
            subject = "Mathematics",
            dayOfWeek = "Wednesday",
            timeSlot = "16:00 - 18:00",
            room = "Alpha Room"
        ))

        tuitionDao.insertClassSchedule(ClassSchedule(
            batchId = batchPhysicsId,
            batchName = "AP Physics Advanced Mastery",
            subject = "Physics",
            dayOfWeek = "Tuesday",
            timeSlot = "17:00 - 19:00",
            room = "Tesla Lab"
        ))

        // 8. Seed Report Cards
        tuitionDao.insertReportCard(ReportCard(
            studentId = student1Id,
            studentName = "Alex Mercer",
            examName = "Mid-Term Examination 2026",
            subject = "Mathematics",
            marksObtained = 92,
            maxMarks = 100,
            grade = "A",
            remarks = "Excellent command of calculus and derivative applications. Showcases proactive problem solving."
        ))

        tuitionDao.insertReportCard(ReportCard(
            studentId = student1Id,
            studentName = "Alex Mercer",
            examName = "Mid-Term Examination 2026",
            subject = "Physics",
            marksObtained = 84,
            maxMarks = 100,
            grade = "B",
            remarks = "Solid foundational logic. Needs slightly more practice in electrostatics equations."
        ))

        tuitionDao.insertReportCard(ReportCard(
            studentId = student2Id,
            studentName = "Emma Watson",
            examName = "Mid-Term Examination 2026",
            subject = "Physics",
            marksObtained = 98,
            maxMarks = 100,
            grade = "A+",
            remarks = "Exceptional and flawless physics concepts. Emma is an asset to the study circle!"
        ))

        // 9. Seed Announcements
        tuitionDao.insertAnnouncement(Announcement(
            title = "Annual Science Exhibition & Tech Showcase",
            content = "The Elite Exhibition is scheduled for July 12th. All grade batches are required to present their experimental frameworks. Parents are cordially invited for the presentation.",
            dateString = "2026-06-23 09:00",
            postedBy = "Sarah Jenkins (Director)",
            targetRole = "ALL"
        ))

        tuitionDao.insertAnnouncement(Announcement(
            title = "Teacher Mid-Term Syllabi Review Protocol",
            content = "Please ensure all student report cards are populated before Sunday. The system will auto-lock report card marks entry for syncing with parent dashboards.",
            dateString = "2026-06-24 08:00",
            postedBy = "Sarah Jenkins (Director)",
            targetRole = "TEACHER"
        ))

        // 10. Seed Communication Messages
        tuitionDao.insertMessage(Message(
            senderId = teacher1Id,
            receiverId = student1Id,
            senderName = "Prof. Marcus Vance",
            receiverName = "Alex Mercer",
            content = "Hi Alex, remember to check the assignment on Limits and Continuity. Let me know if you hit any roadblocks.",
            timestamp = System.currentTimeMillis() - 7200000
        ))

        tuitionDao.insertMessage(Message(
            senderId = student1Id,
            receiverId = teacher1Id,
            senderName = "Alex Mercer",
            receiverName = "Prof. Marcus Vance",
            content = "Thank you Professor, I completed the assignment and uploaded it to my student documents. See you in class tomorrow!",
            timestamp = System.currentTimeMillis() - 3600000
        ))

        // 11. Seed Student Documents
        tuitionDao.insertDocument(StudentDocument(
            studentId = student1Id,
            title = "Calculus Assignment 1 - Limits.pdf",
            fileName = "limits_assignment_mercer.pdf",
            fileSize = "2.4 MB",
            uploadDate = yesterdayStr,
            localUri = "content://tuition_manager/docs/limits_assignment_mercer.pdf"
        ))

        tuitionDao.insertDocument(StudentDocument(
            studentId = student2Id,
            title = "Physics Lab Experiment Report.pdf",
            fileName = "physics_lab_watson.pdf",
            fileSize = "4.1 MB",
            uploadDate = yesterdayStr,
            localUri = "content://tuition_manager/docs/physics_lab_watson.pdf"
        ))
    }
}
