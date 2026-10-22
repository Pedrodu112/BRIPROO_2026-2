
package br.edu.ifsp.nutrifollow.controller;

import java.util.stream.Collectors;

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
            .map(campo -> campo.getDefaultMessage())
            .distinct()
            .collect(Collectors.joining(" "));

        if (mensagem.isBlank()) {
            mensagem = "Os dados enviados são inválidos.";
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mensagem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErroDeRegra(
        IllegalArgumentException erro) {

        String mensagem = erro.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Os dados informados são inválidos.";
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mensagem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> tratarEstadoInvalido(
        IllegalStateException erro) {

        String mensagem = erro.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "A operação não pode ser realizada no estado atual.";
        }

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mensagem);
    }
}
