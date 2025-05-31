package io.github.gustacodes.msavaliadorcredito.application;

import io.github.gustacodes.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.gustacodes.msavaliadorcredito.application.ex.ErroComunicacaoMicroserviceException;
import io.github.gustacodes.msavaliadorcredito.domain.model.SituacaoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/avaliacoes-credito")
public class AvaliadorCreditoController {

    private final AvaliadorCreditoService avaliadorCreditoService;

    @GetMapping("/situacao-cliente")
    public ResponseEntity<?> consultaSituacaoCliente(@RequestParam String cpf) {
        SituacaoCliente situacaoCliente = null;
        try {
            situacaoCliente = avaliadorCreditoService.obterSituacaoCliente(cpf);
        } catch (DadosClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ErroComunicacaoMicroserviceException e) {
            return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(situacaoCliente);
    }
}
