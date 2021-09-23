package br.rcmto.external

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client(value = "\${clients.http_url.bcb}")
interface BCBClient {

    @Get(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML],value = "/{key}")
    fun busca(@PathVariable(value = "key") key: String): HttpResponse<BCBResponse>

    @Post(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun cadastra(@Body bcbAdicionaChaveRequest: CriarChaveRequest): HttpResponse<CriarChaveResponse>

    @Delete(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML], value = "/{key}")
    fun deleta(@PathVariable(value = "key") key: String,@Body bcbRemoveChavePixRequest: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

    enum class KeyType { CPF, CNPJ, PHONE, EMAIL, RANDOM }
    enum class AccountType { CACC, SVGS }
    data class BankAccount(val participant: String, val branch: String, val accountNumber: String, val accountType: AccountType)
    enum class TypeOnner { NATURAL_PERSON, LEGAL_PERSON }
    data class Owner(val type: TypeOnner, val name: String, val taxIdNumber: String)
    data class CriarChaveRequest(val key: String, val keyType: KeyType, val bankAccount: BankAccount, val owner: Owner)
    data class CriarChaveResponse(val key: String, val keyType: KeyType, val bankAccount: BankAccount, val owner: Owner, val createdAt: LocalDateTime)
    data class DeletePixKeyRequest(val key: String, val participant: String)
    data class DeletePixKeyResponse(val key: String, val participant: String, val deletedAt: LocalDateTime)
    data class BCBResponse(
        val keyType: KeyType,
        val key: String,
        val createdAt: String
    )

}