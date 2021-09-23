package br.rcmto.model

import javax.persistence.Embeddable

@Embeddable
data class ContaBanco(
    val nome: String,
    val cpf: String,
    val agencia: String,
    val numero: String,
    val nomeInstituicao:String,
    val ispbInstituicao:String,
    val idTitular: String
) {

}