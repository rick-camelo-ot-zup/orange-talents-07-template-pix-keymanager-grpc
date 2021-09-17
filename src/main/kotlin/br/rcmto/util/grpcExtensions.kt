package br.rcmto.util

import br.rcmto.KeymanagerRequest
import br.rcmto.enums.TipoChave
import br.rcmto.enums.TipoConta
import br.rcmto.model.Chave
import java.util.*

fun KeymanagerRequest.retornaChaveAleatoria(): Chave {
    return Chave(
        chave = UUID.randomUUID().toString(),
        identificador = this.identificador,
        tipoConta = TipoConta.valueOf(this.conta.name),
        tipoChave = TipoChave.valueOf(this.tipo.name)
    )
}

fun KeymanagerRequest.paraChave(): Chave {
    return Chave(
        chave = this.chave,
        identificador = this.identificador,
        tipoConta = TipoConta.valueOf(this.conta.name),
        tipoChave = TipoChave.valueOf(this.tipo.name)
    )
}