package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val name: String,
    val role: String, // "ADMIN", "TEACHER", "STUDENT", "PARENT"
    val phone: String,
    val otpCode: String? = null,
    val isVerified: Boolean = false
) : Serializable

@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // Links to users.id
    val batchId: Long, // Links to batches.id
    val parentName: String,
    val parentPhone: String,
    val admissionNo: String,
    val grade: String,
    val trackingProgress: Int = 100 // 0 to 100% (e.g., average attendance & grades)
) : Serializable

@Entity(tableName = "teacher_profiles")
data class TeacherProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // Links to users.id
    val subject: String,
    val qualification: String,
    val salary: Double,
    val trackingSalaryPaid: Boolean = false
) : Serializable

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String,
    val teacherId: Long, // Links to users.id (Teacher)
    val scheduleTime: String // e.g., "Mon, Wed 10:00 AM - 12:00 PM"
) : Serializable

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long, // Can link to Student user.id or Teacher user.id
    val isTeacher: Boolean,
    val date: String, // "YYYY-MM-DD"
    val status: String // "PRESENT", "ABSENT", "LATE"
) : Serializable

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long, // Links to users.id (Student)
    val studentName: String,
    val amount: Double,
    val dueDate: String, // "YYYY-MM-DD"
    val paymentDate: String? = null, // "YYYY-MM-DD" if paid
    val status: String, // "PAID", "PENDING", "OVERDUE"
    val paymentMethod: String? = null, // "ONLINE", "CASH", "CARD"
    val transactionId: String? = null
) : Serializable

@Entity(tableName = "class_schedules")
data class ClassSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val batchName: String,
    val subject: String,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val timeSlot: String, // "14:00 - 15:30"
    val room: String
) : Serializable

@Entity(tableName = "report_cards")
data class ReportCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long, // Links to users.id (Student)
    val studentName: String,
    val examName: String, // e.g., "Midterm", "Finals"
    val subject: String,
    val marksObtained: Int,
    val maxMarks: Int,
    val grade: String, // "A", "B", "C", "D", "F"
    val remarks: String
) : Serializable

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val dateString: String, // "YYYY-MM-DD HH:mm"
    val postedBy: String,
    val targetRole: String // "ALL", "TEACHER", "STUDENT", "PARENT"
) : Serializable

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val senderName: String,
    val receiverName: String,
    val content: String,
    val timestamp: Long
) : Serializable

@Entity(tableName = "student_documents")
data class StudentDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long, // Links to users.id
    val title: String,
    val fileName: String,
    val fileSize: String,
    val uploadDate: String, // "YYYY-MM-DD"
    val localUri: String // Simulated local path or safe cloud path
) : Serializable
