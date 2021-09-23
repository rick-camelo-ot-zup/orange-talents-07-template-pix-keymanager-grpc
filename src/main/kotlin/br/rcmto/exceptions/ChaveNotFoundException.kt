package br.rcmto.exceptions

import io.grpc.Status

class ChaveNotFoundException(val mensagem:String): GrpcException() {
    override fun getException(): Status {
        return Status.NOT_FOUND.withDescription(mensagem)
    }
}