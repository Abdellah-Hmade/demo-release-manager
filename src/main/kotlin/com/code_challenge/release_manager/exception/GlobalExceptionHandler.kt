package com.code_challenge.release_manager.exception

import com.code_challenge.release_manager.dto.ApiError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        log.error("Validation failed: {}", details)
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        log.error("Unreadable request body: {}", ex.message)
        return build(HttpStatus.BAD_REQUEST, "Request body is missing, malformed or has missing fields")
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(ex: MissingServletRequestParameterException): ResponseEntity<ApiError> {
        log.error("Missing request parameter: {}", ex.parameterName)
        return build(HttpStatus.BAD_REQUEST, "Required parameter '${ex.parameterName}' is missing")
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiError> {
        log.error("Invalid value for parameter {}: {}", ex.name, ex.value)
        return build(HttpStatus.BAD_REQUEST, "Parameter '${ex.name}' has an invalid value")
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiError> {
        if (ex is ErrorResponse) {
            val status = HttpStatus.valueOf(ex.statusCode.value())
            log.error("{}: {}", status, ex.message)
            return build(status, ex.body.detail ?: status.reasonPhrase)
        }
        log.error("Unexpected error", ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error")
    }

    private fun build(status: HttpStatus, message: String, details: List<String> = emptyList()) =
        ResponseEntity.status(status).body(ApiError(status.value(), status.reasonPhrase, message, details))
}
