package br.rcmto.grpc.endpoints

import br.rcmto.CadastrarChavePixGrpcServiceGrpc
import br.rcmto.CadastraChavePixRequest
import br.rcmto.TipoChave
import br.rcmto.TipoConta
import br.rcmto.TipoConta.*
import br.rcmto.external.BCBClient
import br.rcmto.external.BCBClient.*
import br.rcmto.external.ItauClient
import br.rcmto.external.ItauClient.*
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.paraCadastroRequestBcb
import br.rcmto.util.paraChave
import br.rcmto.util.paraContaBanco
import br.rcmto.util.paraDeleteRequestBcb
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixTest(
    @Inject val repository: ChaveRepository,
    @Inject private val client: CadastrarChavePixGrpcServiceGrpc.CadastrarChavePixGrpcServiceBlockingStub
) {

    @MockBean(BCBClient::class)
    fun bcb(): BCBClient? {
        return Mockito.mock(BCBClient::class.java)
    }

    @MockBean(ItauClient::class)
    fun itau(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @Inject
    lateinit var clientBcb: BCBClient

    @Inject
    lateinit var clientItau: ItauClient

    @BeforeEach
    fun cleanAll() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve cadastrar chave aleatoria`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setTipo(TipoChave.CHAVE_ALEATORIA)
            .setConta(CONTA_POUPANCA)
            .setChave("")
            .setIdentificador("25235729-8f5a-4ff2-9cc9-7f1635d2817b")
            .build()

        val responseItau = getResponseItau()
        val contaBanco = responseItau.paraContaBanco()
        val chave = request.paraChave(contaBanco)
        val criarChaveRequest = chave.paraCadastroRequestBcb()

        Mockito.`when`(
            clientItau.consultaCliente(
                request.identificador,request.conta.name
            )
        ).thenReturn(
            HttpResponse.ok(responseItau)
        )

        Mockito.`when`(
            clientBcb.cadastra(criarChaveRequest)
        ).thenReturn(
            HttpResponse.created(getResponseBcb(criarChaveRequest))
        )

        val response = client.cadastrar(request)
        assertNotNull { response.pixID }
    }

    @Test
    internal fun `deve cadastrar chave valida do tipo nao aleatoria`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setTipo(TipoChave.TELEFONE_CELULAR)
            .setChave("+5563992198469")
            .setConta(CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val responseItau = getResponseItau()
        val contaBanco = responseItau.paraContaBanco()
        val chave = request.paraChave(contaBanco)
        val criarChaveRequest = chave.paraCadastroRequestBcb()

        Mockito.`when`(
            clientItau.consultaCliente(
                request.identificador,request.conta.name
            )
        ).thenReturn(
            HttpResponse.ok(responseItau)
        )

        Mockito.`when`(
            clientBcb.cadastra(criarChaveRequest)
        ).thenReturn(
            HttpResponse.created(getResponseBcb(criarChaveRequest))
        )

        val response = client.cadastrar(request)
        assertNotNull { response.pixID }

    }

    @Test
    internal fun `nao deve cadastrar uma chave com formato diferente do tipo informado`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setTipo(TipoChave.EMAIL)
            .setChave("+5563992198469")
            .setConta(CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException> { client.cadastrar(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "A chave (${request.chave}) não corresponde ao tipo (${request.tipo.name})",
                status.description
            )
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave ja cadastrada`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setTipo(TipoChave.TELEFONE_CELULAR)
            .setChave("+5563992198469")
            .setConta(CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val responseItau = getResponseItau()
        val contaBanco = responseItau.paraContaBanco()
        val chave = request.paraChave(contaBanco)
        repository.save(chave)

        val error = assertThrows<StatusRuntimeException> { client.cadastrar(request) }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave  informada (${request.chave}) já está cadastrada.", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar chave em branco para tipo nao aleatoria`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setTipo(TipoChave.TELEFONE_CELULAR)
            .setConta(CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException> { client.cadastrar(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave vazia é inválida para tipo não aleatória.", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CadastrarChavePixGrpcServiceGrpc.CadastrarChavePixGrpcServiceBlockingStub? {
            return CadastrarChavePixGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    fun getResponseItau(): ResponseItau {
        val titular = Titular(
            "5260263c-a3c1-4727-ae32-3bdb2538841b",
            "Yuri Matheus",
            "86135457004"
        )
        val instituicao = Instituicao(
            "ITAÚ UNIBANCO S.A.",
            "60701190"
        )
        return ResponseItau("CONTA_CORRENTE", "0001", "123455", titular, instituicao)
    }

    fun getResponseBcb(request: CriarChaveRequest): CriarChaveResponse{
        return CriarChaveResponse("25235729-8f5a-4ff2-9cc9-7f1635d2817c",request.keyType,request.bankAccount,request.owner, LocalDateTime.now())
    }
}