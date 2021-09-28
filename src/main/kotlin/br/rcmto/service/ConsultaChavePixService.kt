package br.rcmto.service

import br.rcmto.ConsultaChaveExternoRequest
import br.rcmto.ConsultaChaveExternoResponse
import br.rcmto.ConsultaKeyManagerRequest
import br.rcmto.exceptions.ChaveNotFoundException
import br.rcmto.exceptions.ConstraintValidationPixException
import br.rcmto.external.BCBClient
import br.rcmto.model.Chave
import br.rcmto.repository.ChaveRepository
import br.rcmto.util.paraConsultaChaveExternoResponse
import br.rcmto.util.validaFormatoChave
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsultaChavePixService(
    @Inject val repository: ChaveRepository,
    @Inject val bcbClient: BCBClient
) {
    private val LOGGER = LoggerFactory.getLogger(ConsultaChavePixService::class.java)

    fun buscarChaveKeyManager(request: ConsultaKeyManagerRequest): Chave {
        if (request!!.pixId.isNullOrBlank() || request.idPortador.isNullOrBlank()) {
            throw ConstraintValidationPixException("PixID ou Identificador não devem ser nulos.")
        }
        val possivelChave = repository.findById(request.pixId)
        if (possivelChave.isPresent) {
            val chave = possivelChave.get()
            if (chave.conta.idTitular == request.idPortador) {
                return chave
            } else {
                throw ConstraintValidationPixException("A chave e o identificador não conferem.")
            }
        }
        throw ChaveNotFoundException("Chave não encontrada.")
    }

    fun consultaChaveExterno(request2: ConsultaChaveExternoRequest?): ConsultaChaveExternoResponse {
        val request = request2!!
        if (request.chave.isNullOrBlank()) {
            throw ConstraintValidationPixException("A chave não pode ser nula ou em branco.")
        } else if (!validaFormatoChave(request.chave)) {
            throw ConstraintValidationPixException("Formato da chave inválido.")
        }

        val possivelChave = repository.findByChave(request.chave)

        if (possivelChave.isPresent) {
            val chave = possivelChave.get()
            return chave.paraConsultaChaveExternoResponse()
        }

        val responseBcb = bcbClient.busca(request.chave)

        if (responseBcb.status.equals(HttpStatus.OK)) {
            return responseBcb.body()!!.paraConsultaChaveExternoResponse()
        }else{
            throw ChaveNotFoundException("Chave Pix não encontrada.")
        }
    }
}