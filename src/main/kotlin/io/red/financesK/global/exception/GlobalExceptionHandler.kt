package io.red.financesK.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ValidationException::class)
    @ResponseBody
    fun handleValidationException(ex: ValidationException): ResponseEntity<Any> {
        log.info("m='handleValidationException', acao='retornando bad request', mensagem='${ex.message}'")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        log.info("m='handleMethodArgumentNotValidException', acao='retornando bad request', erros='$errors'")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("errors" to errors))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseBody
    fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        val message = "Invalid parameter '${ex.name}': ${ex.value}"
        log.info("m='handleMethodArgumentTypeMismatchException', acao='retornando bad request', mensagem='$message'")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to message))
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleGenericException(ex: Exception): ResponseEntity<Any> {
        log.error("m='handleGenericException', acao='erro inesperado', mensagem='${ex.message}'", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Erro interno do servidor"))
    }
}
