
package br.edu.ifsp.nutrifollow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratamentoErro {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> tratarValidacao(
        MethodArgumentNotValidException erro) {

        String mensagem = erro.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(campo -> campo.getDefaultMessage())
            .orElse("Os dados enviados são inválidos.");

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mensagem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErroDeRegra(
        IllegalArgumentException erro) {

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(erro.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> tratarEstadoInvalido(
        IllegalStateException erro) {

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(erro.getMessage());
    }
}
