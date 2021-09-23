package br.rcmto.service

import br.rcmto.RemoveChavePixRequest
import br.rcmto.exceptions.ChaveNotFoundException
import br.rcmto.exceptions.ClientRequestErrorException
import br.rcmto.exceptions.ConstraintValidationPixException
import br.rcmto.external.BCBClient
import br.rcmto.external.ItauClient
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.hasBlankOrEmptyAtributes
import br.rcmto.util.paraDeleteRequestBcb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveChavePixService(
    @Inject val repository: ChaveRepository,
    @Inject val bcbClient: BCBClient,
    @Inject val itauClient: ItauClient
) {
    fun removeChave(request: RemoveChavePixRequest): Boolean {

        if (request.hasBlankOrEmptyAtributes()) {
            throw ConstraintValidationPixException("A Chave ou identificador não podem ser vazios.")
        }
        val possivelChave = repository.findById(request.pixID)
        if (possivelChave.isPresent) {
            val chave = possivelChave.get()
            if (chave.conta.idTitular == request.identificador) {
                val responseBcb = bcbClient.deleta(chave.chave, chave.paraDeleteRequestBcb())
                if(responseBcb.status.code != 200){
                    throw ClientRequestErrorException("Falha ao tentar remover chave no BCB.")
                }
                repository.delete(chave)
                return true
            }

            throw ConstraintValidationPixException("A chave (${request.pixID}) não pertence ao identificador (${request.identificador}) ")
        }

        throw ChaveNotFoundException("Chave não encontrada no sistema.")
    }
}