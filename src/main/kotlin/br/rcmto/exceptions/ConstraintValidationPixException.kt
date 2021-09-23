package br.rcmto.exceptions

import io.grpc.Status

class ConstraintValidationPixException(val s: String) : GrpcException() {
    override fun getException(): Status {
        return Status.INVALID_ARGUMENT.withDescription(s)
    }

}