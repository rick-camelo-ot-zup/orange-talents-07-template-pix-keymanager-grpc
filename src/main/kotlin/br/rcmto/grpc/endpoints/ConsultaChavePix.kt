package br.rcmto.grpc.endpoints

import br.rcmto.*
import br.rcmto.exceptions.ErrorHandler
import br.rcmto.service.ConsultaChavePixService
import br.rcmto.util.paraConsultaKeyManagerResponse
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ConsultaChavePix(
    @Inject private val service: ConsultaChavePixService
) : ConsultaChaveGrpcServiceGrpc.ConsultaChaveGrpcServiceImplBase(), GrpcServer {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    override fun consultaKeyManager(
        request: ConsultaKeyManagerRequest?,
        responseObserver: StreamObserver<ConsultaKeyManagerResponse>?
    ) {
        LOGGER.info("\n" + request.toString())
        val chave = service.buscarChaveKeyManager(request!!)
        responseObserver!!.onNext(chave.paraConsultaKeyManagerResponse())
        responseObserver.onCompleted()
    }

    override fun consultaChaveExterno(
        request: ConsultaChaveExternoRequest?,
        responseObserver: StreamObserver<ConsultaChaveExternoResponse>?
    ) {
        LOGGER.info("\n" + request.toString())
        val chave = service.consultaChaveExterno(request!!)
        responseObserver!!.onNext(chave)
        responseObserver.onCompleted()
    }
}