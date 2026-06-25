package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

    // --- User Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRoleFlow(role: String): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<User>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)


    // --- Student Profile Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProfile(profile: StudentProfile): Long

    @Update
    suspend fun updateStudentProfile(profile: StudentProfile)

    @Query("SELECT * FROM student_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getStudentProfileByUserId(userId: Long): StudentProfile?

    @Query("SELECT * FROM student_profiles")
    fun getAllStudentProfilesFlow(): Flow<List<StudentProfile>>

    @Query("DELETE FROM student_profiles WHERE id = :id")
    suspend fun deleteStudentProfileById(id: Long)


    // --- Teacher Profile Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherProfile(profile: TeacherProfile): Long

    @Update
    suspend fun updateTeacherProfile(profile: TeacherProfile)

    @Query("SELECT * FROM teacher_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getTeacherProfileByUserId(userId: Long): TeacherProfile?

    @Query("SELECT * FROM teacher_profiles")
    fun getAllTeacherProfilesFlow(): Flow<List<TeacherProfile>>

    @Query("DELETE FROM teacher_profiles WHERE id = :id")
    suspend fun deleteTeacherProfileById(id: Long)


    // --- Batch Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch): Long

    @Query("SELECT * FROM batches")
    fun getAllBatchesFlow(): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Long): Batch?

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatchById(id: Long)


    // --- Attendance Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("SELECT * FROM attendance")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE personId = :personId AND isTeacher = :isTeacher")
    fun getAttendanceForPersonFlow(personId: Long, isTeacher: Boolean): Flow<List<Attendance>>

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)


    // --- Fee Payment Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(feePayment: FeePayment): Long

    @Update
    suspend fun updateFeePayment(feePayment: FeePayment)

    @Query("SELECT * FROM fee_payments ORDER BY dueDate DESC")
    fun getAllFeePaymentsFlow(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId")
    fun getFeePaymentsByStudentFlow(studentId: Long): Flow<List<FeePayment>>

    @Query("DELETE FROM fee_payments WHERE id = :id")
    suspend fun deleteFeePaymentById(id: Long)


    // --- Class Schedule Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSchedule(schedule: ClassSchedule): Long

    @Query("SELECT * FROM class_schedules")
    fun getAllClassSchedulesFlow(): Flow<List<ClassSchedule>>

    @Query("SELECT * FROM class_schedules WHERE batchId = :batchId")
    fun getClassSchedulesByBatchFlow(batchId: Long): Flow<List<ClassSchedule>>

    @Query("DELETE FROM class_schedules WHERE id = :id")
    suspend fun deleteClassScheduleById(id: Long)


    // --- Report Card Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportCard(reportCard: ReportCard): Long

    @Query("SELECT * FROM report_cards")
    fun getAllReportCardsFlow(): Flow<List<ReportCard>>

    @Query("SELECT * FROM report_cards WHERE studentId = :studentId")
    fun getReportCardsByStudentFlow(studentId: Long): Flow<List<ReportCard>>

    @Query("DELETE FROM report_cards WHERE id = :id")
    suspend fun deleteReportCardById(id: Long)


    // --- Announcement Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    @Query("SELECT * FROM announcements ORDER BY dateString DESC")
    fun getAllAnnouncementsFlow(): Flow<List<Announcement>>

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: Long)


    // --- Messaging Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Query("SELECT * FROM messages WHERE (senderId = :id1 AND receiverId = :id2) OR (senderId = :id2 AND receiverId = :id1) ORDER BY timestamp ASC")
    fun getConversationFlow(id1: Long, id2: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<Message>>


    // --- Student Document Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: StudentDocument): Long

    @Query("SELECT * FROM student_documents WHERE studentId = :studentId")
    fun getDocumentsByStudentFlow(studentId: Long): Flow<List<StudentDocument>>

    @Query("SELECT * FROM student_documents")
    fun getAllDocumentsFlow(): Flow<List<StudentDocument>>

    @Query("DELETE FROM student_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}
