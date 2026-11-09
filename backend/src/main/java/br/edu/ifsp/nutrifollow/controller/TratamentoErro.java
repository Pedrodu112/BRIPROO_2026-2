package br.edu.ifsp.nutrifollow.controller;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import br.edu.ifsp.nutrifollow.exception.RecursoNaoEncontradoException;
import br.edu.ifsp.nutrifollow.exception.OperacaoNaoPermitidaException;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class TratamentoErro {

    // Dados enviados pelo corpo da requisição são inválidos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> tratarValidacao(
        MethodArgumentNotValidException erro) {

        String mensagem = erro.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(campo -> campo.getField() + ": "
                + (campo.getDefaultMessage() == null
                    ? "Valor inválido."
                    : campo.getDefaultMessage()))
            .distinct()
            .collect(Collectors.joining(" "));

        if (mensagem.isBlank()) {
            mensagem = "Os dados enviados são inválidos.";
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mensagem);
    }

    // Parâmetros ausentes, tipos incorretos ou JSON malformado.
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<String> tratarRequisicaoInvalida(Exception erro) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body("A requisição contém dados ausentes ou inválidos.");
    }

    // Recurso solicitado não encontrado.
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<String> tratarNaoEncontrado(
        RecursoNaoEncontradoException erro) {

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(erro.getMessage());
    }

    // Operação bloqueada por regra de autorização.
    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<String> tratarOperacaoNaoPermitida(
        OperacaoNaoPermitidaException erro) {

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(erro.getMessage());
    }

    // Dados informados não atendem às regras de negócio.
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

    // Operação incompatível com o estado atual do objeto.
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

    // Falha de acesso ou persistência no banco de dados.
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> tratarFalhaPersistencia(
        DataAccessException erro) {

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Ocorreu uma falha ao acessar ou persistir os dados.");
    }

    // Qualquer erro inesperado não tratado pelos métodos anteriores.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> tratarErroInterno(Exception erro) {

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Ocorreu um erro interno. Tente novamente mais tarde.");
    }
}