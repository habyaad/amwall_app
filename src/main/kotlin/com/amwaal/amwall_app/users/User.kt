package com.amwaal.amwall_app.users
import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

enum class Role {
    USER,
    ADMIN,
}
enum class Gender {
    MALE,
    FEMALE,
    OTHER
}
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String,

    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String,

    @Column(name = "username", nullable = false, length = 100, unique = true)
    var username: String,

    @Column(name = "email", nullable = false, unique = true)
    var email: String,
    
    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "country", nullable = false, length = 2)
    var country: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    var gender: Gender,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: Role = Role.USER,
    
    @Column(name = "phone_number", nullable = false, unique = true)
    var phoneNumber: String,
    
    @Column(name = "pin")
    var pin: String? = null,
    
    @Column(name = "has_setup_pin", nullable = false)
    var hasSetupPin: Boolean = false,
    
    @Column(name = "phone_verified", nullable = false)
    var phoneVerified: Boolean = false,
    
    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)