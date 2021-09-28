package br.rcmto.grpc.endpoints

import br.rcmto.ConsultaChaveExternoRequest
import br.rcmto.ConsultaChaveGrpcServiceGrpc
import br.rcmto.ConsultaKeyManagerRequest
import br.rcmto.enums.TipoChave
import br.rcmto.enums.TipoConta
import br.rcmto.external.BCBClient
import br.rcmto.external.ItauClient
import br.rcmto.model.Chave
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.paraConsultaChaveExternoResponse
import br.rcmto.util.paraConsultaKeyManagerResponse
import br.rcmto.util.paraContaBanco
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaChavePixTest(
    @Inject val repository: ChaveRepository,
    @Inject val client: ConsultaChaveGrpcServiceGrpc.ConsultaChaveGrpcServiceBlockingStub
) {

    @MockBean(BCBClient::class)
    fun bcb(): BCBClient? {
        return Mockito.mock(BCBClient::class.java)
    }

    @Inject
    lateinit var clientBcb: BCBClient

    @BeforeEach
    fun cleanAll() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve consultar chave no keymanager`() {
        val conta = conta().paraContaBanco()
        val chave = Chave(conta = conta, chave = "teste@teste.com", tipoChave = TipoChave.EMAIL, tipo = TipoConta.CONTA_CORRENTE)

        repository.save(chave)

        val responseConsulta = chave.paraConsultaKeyManagerResponse()

        val request = ConsultaKeyManagerRequest.newBuilder().setPixId(chave.id).setIdPortador(chave.conta.idTitular).build()
        val response = client.consultaKeyManager(request)

        assertEquals(responseConsulta,response)
    }

    @Test
    internal fun `deve consultar chave de requisicao externa`() {
        val conta = conta().paraContaBanco()
        val chave = Chave(conta = conta, chave = "teste@teste.com", tipoChave = TipoChave.EMAIL, tipo = TipoConta.CONTA_CORRENTE)

        repository.save(chave)

        val responseConsulta = chave.paraConsultaChaveExternoResponse()

        val request = ConsultaChaveExternoRequest.newBuilder().setChave(chave.chave).build()
        val response = client.consultaChaveExterno(request)

        assertEquals(responseConsulta,response)
    }

    @Test
    internal fun `deve consultar chave no BCB`() {
        val bankAccount = BCBClient.BankAccount("456123", "0001", "45661313", BCBClient.AccountType.CACC)
        val owner = BCBClient.Owner(BCBClient.TypeOnner.NATURAL_PERSON, "Fulano de Tal", "00000000000")

        val bcbResponse = BCBClient.BCBResponse(
            BCBClient.KeyType.EMAIL,
            "teste@teste.com",
            LocalDateTime.now().toString(),
            bankAccount,
            owner
        )

        val request = ConsultaChaveExternoRequest.newBuilder().setChave(bcbResponse.key).build()

        Mockito.`when`(
            clientBcb.busca(request.chave)
        ).thenReturn(
            HttpResponse.ok(bcbResponse)
        )

        val response = client.consultaChaveExterno(request)

        assertEquals(bcbResponse.paraConsultaChaveExternoResponse(),response)
    }

    @Test
    internal fun `nao deve aceitar requisicao com dados em branco - externo`() {

        val request = ConsultaChaveExternoRequest.newBuilder().setChave("").build()

        val error = assertThrows<StatusRuntimeException> { client.consultaChaveExterno(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "A chave não pode ser nula ou em branco.",
                status.description
            )
        }
    }

    @Test
    internal fun `nao deve aceitar requisicao com dados em branco - keymanager`() {

        val request = ConsultaKeyManagerRequest.newBuilder()
            .setIdPortador("").setPixId("").build()

        val error = assertThrows<StatusRuntimeException> { client.consultaKeyManager(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "PixID ou Identificador não devem ser nulos.",
                status.description
            )
        }
    }

    @Test
    internal fun `deve retornar mensagem de chave nao encontrada - externo`() {

        val request = ConsultaChaveExternoRequest.newBuilder().setChave("teste@teste.com").build()

        Mockito.`when`(
            clientBcb.busca(request.chave)
        ).thenReturn(
                HttpResponse.notFound()
        )

        val error = assertThrows<StatusRuntimeException> { client.consultaChaveExterno(request) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(
                "Chave Pix não encontrada.",
                status.description
            )
        }
    }

    @Test
    internal fun `deve retornar mensagem de chave nao encontrada - keymanager`() {

        val request = ConsultaKeyManagerRequest.newBuilder().setPixId("teste").setIdPortador("teste").build()

        val error = assertThrows<StatusRuntimeException> { client.consultaKeyManager(request) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(
                "Chave não encontrada.",
                status.description
            )
        }
    }

    @Test
    internal fun `nao deve aceitar requisicao com chave em formato inválido`() {

        val request = ConsultaChaveExternoRequest.newBuilder().setChave("teste").build()

        val error = assertThrows<StatusRuntimeException> { client.consultaChaveExterno(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Formato da chave inválido.",
                status.description
            )
        }
    }

    @Test
    internal fun `deve consultar chave de outro cliente no keymanager`() {
        val conta = conta().paraContaBanco()
        val chave = Chave(conta = conta, chave = "teste@teste.com", tipoChave = TipoChave.EMAIL, tipo = TipoConta.CONTA_CORRENTE)

        repository.save(chave)

        val responseConsulta = chave.paraConsultaKeyManagerResponse()

        val request = ConsultaKeyManagerRequest.newBuilder().setPixId(chave.id).setIdPortador("25235729-8f5a-4ff2-9cc9-7f1635d2817c").build()

        val error = assertThrows<StatusRuntimeException> { client.consultaKeyManager(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "A chave e o identificador não conferem.",
                status.description
            )
        }
    }

    private fun conta(): ItauClient.ResponseItau {
        val instituicao = ItauClient.Instituicao("ITAU", "54631")
        val titular = ItauClient.Titular("Fulano de tal", "25235729-8f5a-4ff2-9cc9-7f1635d2817b", "00000000000")
        return ItauClient.ResponseItau(TipoConta.CONTA_CORRENTE.name, "0001", "12121", titular, instituicao)

    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultaChaveGrpcServiceGrpc.ConsultaChaveGrpcServiceBlockingStub? {
            return ConsultaChaveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}