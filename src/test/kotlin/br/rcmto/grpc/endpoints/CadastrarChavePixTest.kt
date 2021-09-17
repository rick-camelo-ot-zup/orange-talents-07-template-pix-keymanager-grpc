package br.rcmto.grpc.endpoints

import br.rcmto.KeymanagerGrpcServiceGrpc
import br.rcmto.KeymanagerRequest
import br.rcmto.TipoChave
import br.rcmto.TipoConta
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.paraChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixTest(
    val repository: ChaveRepository,
    val client: KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceBlockingStub
){

    @BeforeEach
    fun cleanAll(){
        repository.deleteAll()
    }

    @Test
    internal fun `deve cadastrar chave aleatoria`() {
        val request = KeymanagerRequest.newBuilder()
            .setTipo(TipoChave.CHAVE_ALEATORIA)
            .setConta(TipoConta.CONTA_POUPANCA)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val response = client.cadastrar(request)
        assertNotNull{response.pixID}
    }

    @Test
    internal fun `deve cadastrar chave valida do tipo nao aleatoria`() {
        val request = KeymanagerRequest.newBuilder()
            .setTipo(TipoChave.TELEFONE_CELULAR)
            .setChave("+5563992198469")
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val response = client.cadastrar(request)
        assertNotNull{response.pixID}

        val request2 = KeymanagerRequest.newBuilder()
            .setTipo(TipoChave.CPF)
            .setChave("00000000000")
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val response2 = client.cadastrar(request2)
        assertNotNull{response2.pixID}
    }

    @Test
    internal fun `nao deve cadastrar uma chave com formato diferente do tipo informado`() {
        val request = KeymanagerRequest.newBuilder()
            .setTipo(TipoChave.EMAIL)
            .setChave("+5563992198469")
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException> { client.cadastrar(request) }

        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("A chave (${request.chave}) não corresponde ao tipo (${request.tipo.name}) ", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar uma chave ja cadastrada`() {
        val request = KeymanagerRequest.newBuilder()
            .setTipo(TipoChave.TELEFONE_CELULAR)
            .setChave("+5563992198469")
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val chave = request.paraChave()
        repository.save(chave)

        val error = assertThrows<StatusRuntimeException> { client.cadastrar(request) }

        with(error){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave  informada (${request.chave}) já está cadastrada.", status.description)
        }
    }

    @Test
    internal fun `nao deve cadastrar chave em branco para tipo nao aleatoria`() {
        val request = KeymanagerRequest.newBuilder()
            .setTipo(TipoChave.TELEFONE_CELULAR)
            .setConta(TipoConta.CONTA_CORRENTE)
            .setIdentificador(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException> { client.cadastrar(request) }

        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave vazia é inválida para tipo não aleatória.", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceBlockingStub? {
            return KeymanagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}