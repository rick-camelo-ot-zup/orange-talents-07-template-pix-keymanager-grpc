package br.rcmto.exceptions

import io.grpc.Status

class ClientRequestErrorException(val mensagem:String): GrpcException() {
    override fun getException(): Status {
        return Status.FAILED_PRECONDITION.withDescription(mensagem)
    }
}