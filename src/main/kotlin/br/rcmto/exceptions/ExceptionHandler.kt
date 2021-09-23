package br.rcmto.exceptions

import br.rcmto.grpc.endpoints.GrpcServer
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandler : MethodInterceptor<GrpcServer, Any?> {

    override fun intercept(context: MethodInvocationContext<GrpcServer, Any?>): Any? {
        try {
            context.proceed()
        } catch (e: GrpcException) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(e.getException().asRuntimeException())
        }
        return null
    }

}