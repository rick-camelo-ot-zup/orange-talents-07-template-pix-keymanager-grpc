package br.rcmto.exceptions

import io.grpc.Status

abstract class GrpcException: Exception() {
    abstract fun getException():Status
}