package io.github.gustacodes.msavaliadorcredito.application;

import io.github.gustacodes.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.gustacodes.msavaliadorcredito.domain.model.DadosCliente;
import io.github.gustacodes.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.gustacodes.msavaliadorcredito.infra.CartaoResourceClient;
import io.github.gustacodes.msavaliadorcredito.infra.ClienteResourceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clientesClient;
    private final CartaoResourceClient cartaoResourceClient;

    public SituacaoCliente obterSituacaoCliente(String cpf) {
        ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosDoCliente(cpf);
        ResponseEntity<List<CartaoCliente>> cartoesCliente = cartaoResourceClient.getCartoesByCliente(cpf);

        return SituacaoCliente.builder()
                .cliente(dadosClienteResponse.getBody())
                .cartoes(cartoesCliente.getBody())
                .build();
    }

}
