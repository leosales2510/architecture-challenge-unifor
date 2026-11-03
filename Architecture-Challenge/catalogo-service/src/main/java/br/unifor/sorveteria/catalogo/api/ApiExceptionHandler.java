package br.unifor.sorveteria.catalogo.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.unifor.sorveteria.catalogo.domain.exception.ComposicaoInvalidaException;
import br.unifor.sorveteria.catalogo.domain.exception.RecursoNaoEncontradoException;

/**
 * Converte exceções em respostas RFC 7807 (application/problem+json) com um
 * campo "codigo" estável, que o pedido-service e o front-end usam para decidir o que fazer.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "ITEM_NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(ComposicaoInvalidaException.class)
    ProblemDetail composicaoInvalida(ComposicaoInvalidaException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "COMPOSICAO_INVALIDA", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> erros.put(f.getField(), f.getDefaultMessage()));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Requisição com campos inválidos");
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoInvalido(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO", "JSON malformado ou com valor desconhecido");
    }

    private static ProblemDetail problema(HttpStatus status, String codigo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(status.getReasonPhrase());
        problema.setProperty("codigo", codigo);
        return problema;
    }
}
