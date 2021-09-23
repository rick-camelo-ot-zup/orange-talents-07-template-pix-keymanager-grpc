package br.rcmto.grpc.endpoints

import br.rcmto.*
import br.rcmto.enums.TipoChave.*
import br.rcmto.enums.TipoConta.*
import br.rcmto.external.BCBClient
import br.rcmto.external.BCBClient.*
import br.rcmto.model.Chave
import br.rcmto.model.ContaBanco
import br.rcmto.repository.ChaveRepository
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
import java.util.UUID.randomUUID
import javax.inject.Inject
import javax.inject.Singleton
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class RemoverChavePixTest(
    @Inject val repository: ChaveRepository,
    @Inject private val client: RemoverChavePixGrpcServiceGrpc.RemoverChavePixGrpcServiceBlockingStub
) {

    var chave: Chave? = null

    @MockBean(BCBClient::class)
    fun bcb(): BCBClient? {
        return Mockito.mock(BCBClient::class.java)
    }

    @Inject
    lateinit var clientBcb: BCBClient

    @BeforeEach
    fun cleanAll() {
        repository.deleteAll()
        chave = Chave(
            CONTA_CORRENTE, "teste@teste", EMAIL, ContaBanco(
                "rick",
                "00000000000",
                "0001",
                "000000",
                "ITAU",
                "00000000",
                "25235729-8f5a-4ff2-9cc9-7f1635d2817b"
            )
        )
    }

    @Test
    internal fun `deve remover chave`() {

        repository.save(chave)

        Mockito.`when`(clientBcb.deleta(key = chave?.chave!!, bcbRemoveChavePixRequest = chave?.paraDeleteRequestBcb()!!))
            .thenReturn(
                HttpResponse.ok(DeletePixKeyResponse(chave?.chave!!, chave?.conta?.ispbInstituicao!!, LocalDateTime.now()))
            )

        val request = RemoveChavePixRequest.newBuilder()
            .setIdentificador("25235729-8f5a-4ff2-9cc9-7f1635d2817b")
            .setPixID(chave?.id)
            .build()

        val response = client.remover(request)
        assertNotNull { response.pixID }
    }

    @Test
    fun `nao deve remover uma chave nao cadastrada`() {

        val request = RemoveChavePixRequest.newBuilder()
            .setIdentificador(randomUUID().toString())
            .setPixID(randomUUID().toString())
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> { client.remover(request) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada no sistema.", status.description)
        }
    }

    @Test
    fun `nao deve aceitar identificador vazio`() {

        val request = RemoveChavePixRequest.newBuilder()
            .setIdentificador("")
            .setPixID(randomUUID().toString())
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> { client.remover(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("A Chave ou identificador não podem ser vazios.", status.description)
        }
    }

    @Test
    fun `nao deve aceitar Pix ID vazio`() {

        val request = RemoveChavePixRequest.newBuilder()
            .setIdentificador(randomUUID().toString())
            .setPixID("")
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> { client.remover(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("A Chave ou identificador não podem ser vazios.", status.description)
        }
    }

    @Test
    fun `somente o dono da chave pode remover`() {

        repository.save(chave)

        val request = RemoveChavePixRequest.newBuilder()
            .setIdentificador("25235729-8f5a-4ff2-9cc9-7f1635d2817c")
            .setPixID(chave?.id)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> { client.remover(request) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "A chave (${request.pixID}) não pertence ao identificador (${request.identificador}) ",
                status.description
            )
        }

    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemoverChavePixGrpcServiceGrpc.RemoverChavePixGrpcServiceBlockingStub? {
            return RemoverChavePixGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}