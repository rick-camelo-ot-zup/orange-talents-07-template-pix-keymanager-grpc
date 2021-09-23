package br.rcmto.util

import br.rcmto.CadastraChavePixRequest
import br.rcmto.RemoveChavePixRequest
import br.rcmto.enums.TipoChave
import br.rcmto.enums.TipoConta
import br.rcmto.external.BCBClient.*
import br.rcmto.external.ItauClient
import br.rcmto.model.Chave
import br.rcmto.model.ContaBanco

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