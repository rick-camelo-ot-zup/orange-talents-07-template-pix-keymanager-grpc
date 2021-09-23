package br.rcmto.util

import br.rcmto.CadastraChavePixRequest
import br.rcmto.TipoChave
import br.rcmto.exceptions.ConstraintValidationPixException


class CadastraChavePixValidator{
    fun validateCadastraChavePixRequest(request: CadastraChavePixRequest){
        if(request.chave.isBlank() || request.chave.isEmpty()){
            throw ConstraintValidationPixException("Chave vazia é inválida para tipo não aleatória.")
        }
        if(!isChaveValida(request.chave, request.tipo)){
            throw ConstraintValidationPixException("A chave (${request.chave}) não corresponde ao tipo (${request.tipo.name})")
        }
    }
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
}

