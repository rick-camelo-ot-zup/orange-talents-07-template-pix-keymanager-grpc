package br.rcmto.external

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${clients.http_url.itau}")
interface ItauClient {

    @Get(value = "/{clientId}/contas")
    fun consultaCliente(
        @PathVariable("clientId") clienteId: String,
        @QueryValue tipo: String
    ): HttpResponse<ResponseItau>

    data class ResponseItau(
        val tipo: String,
        val agencia: String,
        val numero: String,
        val titular: Titular,
        val instituicao: Instituicao
    )

    data class Titular(val id: String, val nome: String, val cpf: String)
    data class Instituicao(val nome: String, val ispb: String)
}
