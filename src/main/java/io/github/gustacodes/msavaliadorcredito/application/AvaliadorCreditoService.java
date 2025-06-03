package io.github.gustacodes.msavaliadorcredito.application;

import feign.FeignException;
import io.github.gustacodes.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.gustacodes.msavaliadorcredito.application.ex.ErroComunicacaoMicroserviceException;
import io.github.gustacodes.msavaliadorcredito.application.ex.ErroSolicitacaoCartaoException;
import io.github.gustacodes.msavaliadorcredito.domain.model.*;
import io.github.gustacodes.msavaliadorcredito.infra.CartaoResourceClient;
import io.github.gustacodes.msavaliadorcredito.infra.ClienteResourceClient;
import io.github.gustacodes.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clientesClient;
    private final CartaoResourceClient cartoesClient;
    private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

    public SituacaoCliente obterSituacaoCliente(String cpf) throws DadosClienteNotFoundException, ErroComunicacaoMicroserviceException {
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosDoCliente(cpf);
            ResponseEntity<List<CartaoCliente>> cartoesCliente = cartoesClient.getCartoesByCliente(cpf);

            return SituacaoCliente.builder()
                    .cliente(dadosClienteResponse.getBody())
                    .cartoes(cartoesCliente.getBody())
                    .build();
        } catch (FeignException.FeignClientException e) {
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            } else {
                throw new ErroComunicacaoMicroserviceException(e.getMessage(), status);
            }
        }

    }

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErroComunicacaoMicroserviceException {
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosDoCliente(cpf);
            ResponseEntity<List<Cartao>> cartoesReponse = cartoesClient.getCartoesRendaAte(renda);

            List<Cartao> cartoes = cartoesReponse.getBody();

            var listaCartoesAprovados = cartoes.stream().map(cartao -> {
                DadosCliente dadosCliente = dadosClienteResponse.getBody();
                BigDecimal limiteBasico = cartao.getLimiteBasico();
                BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
                var fator = idadeBD.divide(BigDecimal.valueOf(10));
                BigDecimal limiteAprovado = fator.multiply(limiteBasico);

                CartaoAprovado aprovado = new CartaoAprovado();

                aprovado.setCartao(cartao.getNome());
                aprovado.setBandeira(cartao.getBandeira());
                aprovado.setLimiteAprovado(limiteAprovado);

                return aprovado;
            }).collect(Collectors.toList());
            return new RetornoAvaliacaoCliente(listaCartoesAprovados);
        } catch (FeignException.FeignClientException e) {
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            } else {
                throw new ErroComunicacaoMicroserviceException(e.getMessage(), status);
            }
        }

    }

    public ProtocoloSolicitacaoCartao solicitarEmissaoCartao(DadosSolicitacaoEmissaoCartao emissaoCartao) {
        try {
            emissaoCartaoPublisher.solicitarCartao(emissaoCartao);
            var protocolo = UUID.randomUUID().toString();
            return new ProtocoloSolicitacaoCartao(protocolo);
        } catch (Exception e) {
            throw new ErroSolicitacaoCartaoException(e.getMessage());
        }
    }

}
