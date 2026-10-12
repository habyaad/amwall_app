package com.amwaal.amwall_app.infrastructure.exceptions

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    //Credentials & Auth failures
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<Map<String, Any>> {
        // Return a uniform "Invalid email or password" to prevent username/email enumeration
        val clientMessage = when (ex) {
            is BadCredentialsException, is UsernameNotFoundException -> "Invalid Credentials"
            else -> ex.message ?: "Authentication failed"
        }
        return buildResponse(
            status = HttpStatus.UNAUTHORIZED,
            error = "Unauthorized",
            message = clientMessage
        )
    }

    //DTO Validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }
        val globalErrors = ex.bindingResult.globalErrors.mapNotNull { it.defaultMessage }

        val details = buildMap {
            if (fieldErrors.isNotEmpty()) put("fieldErrors", fieldErrors)
            if (globalErrors.isNotEmpty()) put("globalErrors", globalErrors)
        }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Validation Failed",
            message = "One or more fields failed validation",
            details = details
        )
    }

    //Malformed JSON (trailing commas, syntax errors, mismatched types)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        val rootCause = ex.mostSpecificCause
        val rawMessage = rootCause.message ?: ""

        val cleanMessage = when {
            rawMessage.contains("was expecting double-quote") || rawMessage.contains("Unexpected character") ->
                "Malformed JSON syntax. Please check for syntax errors like trailing commas or unquoted keys."

            rawMessage.contains("Cannot deserialize") || rawMessage.contains("not a valid") ->
                "Invalid data format provided for one or more fields."

            else -> "Malformed or unreadable request body."
        }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Bad Request",
            message = cleanMessage
        )
    }

    //Missing required query parameter
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(ex: MissingServletRequestParameterException): ResponseEntity<Map<String, Any>> {
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Bad Request",
            message = "Required parameter '${ex.parameterName}' is missing"
        )
    }

    //Query / path variable type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, Any>> {
        val expected = ex.requiredType?.simpleName ?: "unknown"
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Bad Request",
            message = "Parameter '${ex.name}' must be of type $expected"
        )
    }

    // Wrong HTTP Method (e.g. GET instead of POST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<Map<String, Any>> {
        val supported = ex.supportedHttpMethods?.joinToString(", ") ?: "None"
        return buildResponse(
            status = HttpStatus.METHOD_NOT_ALLOWED,
            error = "Method Not Allowed",
            message = "HTTP ${ex.method} is not supported. Supported: $supported"
        )
    }

    //Data integrity conflicts, such as duplicate unique values
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Map<String, Any>> {
        log.warn("Request rejected due to a data integrity conflict", ex)
        return buildResponse(
            status = HttpStatus.CONFLICT,
            error = "Conflict",
            message = "The request conflicts with existing data."
        )
    }

    //Unknown / Unhandled Server Errors (Robust 500 handler)
    @ExceptionHandler(Exception::class)
    fun handleUnknownException(ex: Exception): ResponseEntity<Map<String, Any>> {
        // Generate a tracking ID for logs
        val errorId = UUID.randomUUID().toString().substring(0, 8)

        // Log the actual stack trace privately to the server logs
        log.error("Unhandled Exception [Error ID: $errorId]: ${ex.message}", ex)

        // Send a sanitized, secure response to the client
        return buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please contact support with reference ID: $errorId",
            details = mapOf("errorReference" to errorId)
        )
    }

    // --- Helper to keep all API error structures consistent ---
    private fun buildResponse(
        status: HttpStatus,
        error: String,
        message: String,
        details: Map<String, Any>? = null
    ): ResponseEntity<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "timestamp" to Instant.now().toString(),
            "status" to status.value(),
            "error" to error,
            "message" to message
        )
        if (details != null) {
            body.putAll(details)
        }
        return ResponseEntity.status(status).body(body)
    }
}