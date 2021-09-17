package br.rcmto.grpc.endpoints

import br.rcmto.*
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.isChaveValida
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import br.rcmto.util.paraChave
import br.rcmto.util.retornaChaveAleatoria

@Singleton
class CadastrarChavePix(
    val repository: ChaveRepository
) : KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceImplBase() {
    private val logger = LoggerFactory.getLogger(CadastrarChavePix::class.java)

    override fun cadastrar(request: KeymanagerRequest?, responseObserver: StreamObserver<KeymanagerResponse>?) {
        logger.info("recebida requisicao de cadastro da chave ${request.toString()}")

        request?.let {
            if (it.tipo.equals(TipoChave.CHAVE_ALEATORIA)) {
                val chave = request.retornaChaveAleatoria()
                repository.save(chave)
                logger.info("recebida requisicao de cadastro da chave ${request.toString()}")
                responseObserver!!.onNext(KeymanagerResponse.newBuilder().setPixID(chave.id).build())
                responseObserver.onCompleted()
            }else if(it.chave.isBlank() || it.chave.isEmpty()){
                responseObserver!!.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Chave vazia é inválida para tipo não aleatória.")
                        .asRuntimeException()
                )
            }else if (isChaveValida(it.chave, it.tipo)) {
                val isChaveCadastrada = repository.existsByChave(it.chave)
                if (isChaveCadastrada) {
                    responseObserver!!.onError(
                        Status.ALREADY_EXISTS
                            .withDescription("A chave  informada (${it.chave}) já está cadastrada.")
                            .asRuntimeException()
                    )
                    return
                }
                val chave = request.paraChave()
                repository.save(chave)
                responseObserver!!.onNext(KeymanagerResponse.newBuilder().setPixID(chave.id).build())
                responseObserver.onCompleted()
            }else{
                responseObserver!!.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("A chave (${it.chave}) não corresponde ao tipo (${it.tipo.name}) ")
                        .asRuntimeException()
                )
            }
        }

    }
}