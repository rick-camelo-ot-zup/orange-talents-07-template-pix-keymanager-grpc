package br.rcmto.exceptions

import io.grpc.Status

class ClienteNotFoundException (val s: String) : GrpcException() {
    override fun getException(): Status {
        return Status.NOT_FOUND.withDescription(s)
    }
}