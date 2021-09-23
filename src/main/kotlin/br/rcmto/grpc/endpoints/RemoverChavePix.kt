package br.rcmto.grpc.endpoints

import br.rcmto.*
import br.rcmto.exceptions.ErrorHandler
import br.rcmto.service.RemoveChavePixService
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoverChavePix(
    @Inject private val service: RemoveChavePixService
) : RemoverChavePixGrpcServiceGrpc.RemoverChavePixGrpcServiceImplBase(), GrpcServer {
    private val logger = LoggerFactory.getLogger(RemoverChavePix::class.java)

    override fun remover(request: RemoveChavePixRequest?, responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        request?.let {
            val removeChave = service.removeChave(it)
            if(removeChave){
                responseObserver!!.onNext(RemoveChavePixResponse.newBuilder().setPixID(request?.identificador).build())
                responseObserver.onCompleted()
            }
        }
    }
}