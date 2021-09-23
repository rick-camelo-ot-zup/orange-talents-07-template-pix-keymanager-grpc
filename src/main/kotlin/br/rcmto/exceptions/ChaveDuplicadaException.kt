package br.rcmto.exceptions

import io.grpc.Status

class ChaveDuplicadaException (val s: String) : GrpcException() {
    override fun getException(): Status {
        return Status.ALREADY_EXISTS.withDescription(s)
    }
}