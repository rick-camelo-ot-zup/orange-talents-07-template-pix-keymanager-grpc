package br.rcmto.util

import br.rcmto.*
import br.rcmto.enums.TipoChave
import br.rcmto.enums.TipoConta
import br.rcmto.external.BCBClient.*
import br.rcmto.external.ItauClient
import br.rcmto.model.Chave
import br.rcmto.model.ContaBanco
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset

//fun CadastraChavePixRequest.retornaChaveAleatoria(): Chave {
//    return Chave(
//        chave = UUID.randomUUID().toString(),
//        identificador = this.identificador,
//        tipo = TipoConta.valueOf(this.conta.name),
//        tipoChave = TipoChave.valueOf(this.tipo.name)
//    )
//}
//
//fun CadastraChavePixRequest.paraChave(): Chave {
//    return Chave(
//        chave = this.chave,
//        identificador = this.identificador,
//        tipo = TipoConta.valueOf(this.conta.name),
//        tipoChave = TipoChave.valueOf(this.tipo.name)
//    )
//}

fun CadastraChavePixRequest.paraChave(contaBanco: ContaBanco): Chave {

    return Chave(
        chave = this.chave,
        tipo = TipoConta.valueOf(this.conta.name),
        tipoChave = TipoChave.valueOf(this.tipo.name),
        conta = contaBanco
    )
}

fun ItauClient.ResponseItau.paraContaBanco(): ContaBanco {
    return ContaBanco(
        this.titular.nome,
        this.titular.cpf,
        this.agencia,
        this.numero,
        this.instituicao.nome,
        this.instituicao.ispb,
        this.titular.id
    )
}

fun Chave.paraCadastroRequestBcb(): CriarChaveRequest{

    val tipoConta = when (this.tipo) {
        TipoConta.CONTA_CORRENTE -> AccountType.CACC
        TipoConta.CONTA_POUPANCA -> AccountType.SVGS
        else -> throw IllegalStateException("Tipo da conta inválido")
    }
    val bankAccount = BankAccount(
        this.conta.ispbInstituicao, this.conta.agencia, this.conta.numero, tipoConta
    )

    val owner = Owner(TypeOnner.NATURAL_PERSON, this.conta.nome, this.conta.cpf)

    val tipoChave = when (this.tipoChave) {
        TipoChave.CPF -> KeyType.CPF
        TipoChave.EMAIL -> KeyType.EMAIL
        TipoChave.TELEFONE_CELULAR -> KeyType.PHONE
        TipoChave.CHAVE_ALEATORIA -> KeyType.RANDOM
        else -> throw IllegalStateException("Tipo da chave inválido")
    }
    return CriarChaveRequest(this.chave,tipoChave, bankAccount, owner)
}

fun RemoveChavePixRequest.hasBlankOrEmptyAtributes(): Boolean {
    if (this.identificador.isBlank() || this.identificador.isEmpty()
        || this.pixID.isBlank() || this.pixID.isEmpty()
    ) {
        return true
    }
    return false
}

fun Chave.paraDeleteRequestBcb(): DeletePixKeyRequest{
    return DeletePixKeyRequest(this.chave,this.conta.ispbInstituicao)
}

fun Chave.paraConsultaKeyManagerResponse(): ConsultaKeyManagerResponse {
    val conta = Conta.newBuilder()
        .setNumero(this.conta.numero)
        .setTipoConta(br.rcmto.TipoConta.valueOf(this.tipo.name))
        .setInstituicao(this.conta.nomeInstituicao).build()
    return ConsultaKeyManagerResponse.newBuilder()
        .setTipoChave(br.rcmto.TipoChave.valueOf(this.tipoChave.name))
        .setChave(this.chave)
        .setNomePortador(this.conta.nome)
        .setCpfPortador(this.conta.cpf)
        .setConta(conta)
        .setCriadoEm(Timestamp.newBuilder().setSeconds(this.criadoEm.toEpochSecond(ZoneOffset.UTC)).build())
        .setIdPix(this.id)
        .setIdentificador(this.conta.idTitular)
        .build()
}

fun Chave.paraConsultaChaveExternoResponse(): ConsultaChaveExternoResponse{
    val conta = Conta.newBuilder()
        .setNumero(this.conta.numero)
        .setTipoConta(br.rcmto.TipoConta.valueOf(this.tipo.name))
        .setInstituicao(this.conta.nomeInstituicao).build()
    return ConsultaChaveExternoResponse.newBuilder()
        .setTipoChave(br.rcmto.TipoChave.valueOf(this.tipoChave.name))
        .setChave(this.chave)
        .setNomePortador(this.conta.nome)
        .setCpfPortador(this.conta.cpf)
        .setConta(conta)
        .setCriadoEm(Timestamp.newBuilder().setSeconds(this.criadoEm.toEpochSecond(ZoneOffset.UTC)).build())
        .build()
}

fun BCBResponse.paraConsultaChaveExternoResponse(): ConsultaChaveExternoResponse{
    val tipoChave = when (this.keyType) {
        KeyType.CPF -> br.rcmto.TipoChave.CPF
        KeyType.PHONE -> br.rcmto.TipoChave.TELEFONE_CELULAR
        KeyType.EMAIL -> br.rcmto.TipoChave.EMAIL
        KeyType.RANDOM -> br.rcmto.TipoChave.CHAVE_ALEATORIA
        KeyType.CNPJ -> br.rcmto.TipoChave.UNRECOGNIZED
    }
    val tipoConta = when (this.bankAccount.accountType) {
        AccountType.SVGS -> br.rcmto.TipoConta.CONTA_POUPANCA
        AccountType.CACC -> br.rcmto.TipoConta.CONTA_CORRENTE
    }
    val conta = Conta.newBuilder()
        .setNumero(this.bankAccount.accountNumber)
        .setTipoConta(tipoConta)
        .setInstituicao(this.bankAccount.branch).build()
    return ConsultaChaveExternoResponse.newBuilder()
        .setTipoChave(tipoChave)
        .setChave(this.key)
        .setNomePortador(this.owner.name)
        .setCpfPortador(this.owner.taxIdNumber)
        .setConta(conta)
        .setCriadoEm(Timestamp.newBuilder().setSeconds(LocalDateTime.parse(this.createdAt).toEpochSecond(ZoneOffset.UTC)).build())
        .build()
}