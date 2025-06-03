package io.github.gustacodes.msavaliadorcredito.application;

import io.github.gustacodes.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.gustacodes.msavaliadorcredito.application.ex.ErroComunicacaoMicroserviceException;
import io.github.gustacodes.msavaliadorcredito.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<?> realizarAvaliacao(@RequestBody DadosAvalicao dadosAvalicao) {
        try {
            RetornoAvaliacaoCliente retornoAvaliacaoCliente = avaliadorCreditoService.realizarAvaliacao(dadosAvalicao.getCpf(), dadosAvalicao.getRenda());
            return ResponseEntity.status(HttpStatus.OK).body(retornoAvaliacaoCliente);
        } catch (DadosClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ErroComunicacaoMicroserviceException e) {
            return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
        }
    }

    @PostMapping("/solicitacoes-cartao")
    public ResponseEntity<?> solicitarCartao(@RequestBody DadosSolicitacaoEmissaoCartao emissaoCartao) {
        try {
            ProtocoloSolicitacaoCartao protocoloSolicitacaoCartao = avaliadorCreditoService.solicitarEmissaoCartao(emissaoCartao);
            return ResponseEntity.status(HttpStatus.OK).body(protocoloSolicitacaoCartao);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
