package io.github.gustacodes.msavaliadorcredito.application;

import io.github.gustacodes.msavaliadorcredito.domain.model.DadosCliente;
import io.github.gustacodes.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.gustacodes.msavaliadorcredito.infra.ClienteResourceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clientesClient;

    public SituacaoCliente obterSituacaoCliente(String cpf) {
        ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosDoCliente(cpf);
        return SituacaoCliente.builder()
                .cliente(dadosClienteResponse.getBody())
                .build();
    }

}
