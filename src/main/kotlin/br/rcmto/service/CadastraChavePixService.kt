package br.rcmto.service

import br.rcmto.CadastraChavePixRequest
import br.rcmto.TipoChave
import br.rcmto.exceptions.ChaveDuplicadaException
import br.rcmto.exceptions.ClientRequestErrorException
import br.rcmto.exceptions.ClienteNotFoundException
import br.rcmto.external.BCBClient
import br.rcmto.external.ItauClient
import br.rcmto.model.Chave
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.CadastraChavePixValidator
import br.rcmto.util.paraChave
import br.rcmto.util.paraContaBanco
import br.rcmto.util.paraCadastroRequestBcb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CadastraChavePixService(
    @Inject val repository: ChaveRepository,
    @Inject val bcbClient: BCBClient,
    @Inject val itauClient: ItauClient
) {
    fun cadastraChave(request: CadastraChavePixRequest): Chave {
        if (request.tipo.equals(TipoChave.CHAVE_ALEATORIA)) {
                return realizaCadastroClients(request)
        } else {
            CadastraChavePixValidator().validateCadastraChavePixRequest(request)

            val isChaveCadastrada = repository.existsByChave(request.chave)
            if (isChaveCadastrada) {
                throw ChaveDuplicadaException("A chave  informada (${request.chave}) já está cadastrada.")
            }

            return realizaCadastroClients(request)
        }
    }

    fun realizaCadastroClients(request: CadastraChavePixRequest): Chave{
        val consultaItau = itauClient.consultaCliente(request.identificador, request.conta.name)
        if (consultaItau.status.code != 200) {
            throw ClienteNotFoundException("Cliente não encontrado.")
        }
        val dadosConta = consultaItau.body()!!.paraContaBanco()
        val chave = request.paraChave(dadosConta)

        val requestBcb = chave.paraCadastroRequestBcb()

        val respostaBcb = bcbClient.cadastra(requestBcb)

        if (respostaBcb.status.code != 201) {
            throw ClientRequestErrorException("Falha na requisicação para o sistema do BCB.")
        }
        val retornoBcb = respostaBcb.body()
        chave.atualiza(retornoBcb.key, retornoBcb.createdAt)
        repository.save(chave)
        return chave
    }

}