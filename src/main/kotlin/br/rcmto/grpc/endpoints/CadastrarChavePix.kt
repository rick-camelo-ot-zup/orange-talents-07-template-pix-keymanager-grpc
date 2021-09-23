package br.rcmto.grpc.endpoints

import br.rcmto.*
import br.rcmto.exceptions.ErrorHandler
import br.rcmto.model.Chave
import br.rcmto.service.CadastraChavePixService
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional

@Singleton
@ErrorHandler
class CadastrarChavePix(
    @Inject private val service: CadastraChavePixService
) : CadastrarChavePixGrpcServiceGrpc.CadastrarChavePixGrpcServiceImplBase(), GrpcServer {
    private val logger = LoggerFactory.getLogger(CadastrarChavePix::class.java)

    @Transactional
    override fun cadastrar(
        request: CadastraChavePixRequest?,
        responseObserver: StreamObserver<CadastraChavePixResponse>?
    ) {
        logger.info("recebida requisicao de cadastro da chave ${request.toString()}")
        var chave: Chave
        request?.let {
            chave = service.cadastraChave(it)
        }
        responseObserver!!.onNext(CadastraChavePixResponse.newBuilder().setPixID(UUID.randomUUID().toString()).build())
        responseObserver?.onCompleted()
    }
}