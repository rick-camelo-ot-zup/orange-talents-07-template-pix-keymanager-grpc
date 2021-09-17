package br.rcmto.util

import br.rcmto.TipoChave

fun isChaveValida(chave: String, tipo: TipoChave): Boolean{
    when(tipo){
        TipoChave.CPF -> {
            return validaCpf(chave)
        }
        TipoChave.TELEFONE_CELULAR -> {
            return validaCelular(chave)
        }
        TipoChave.EMAIL -> {
            return validaEmail(chave)
        }
        else -> return false
    }
}
