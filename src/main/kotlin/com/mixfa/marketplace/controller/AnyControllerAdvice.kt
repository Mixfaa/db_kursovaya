package com.mixfa.marketplace.controller

import com.mixfa.marketplace.shared.ErrorModel
import com.mixfa.marketplace.shared.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class AnyControllerAdvice {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(exception: NotFoundException): ResponseEntity<ErrorModel> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorModel(exception))
    }

    @ExceptionHandler(Throwable::class)
    fun handleException(exception: Throwable): ResponseEntity<ErrorModel> {
        return ResponseEntity.badRequest().body(ErrorModel(exception))
    }
}