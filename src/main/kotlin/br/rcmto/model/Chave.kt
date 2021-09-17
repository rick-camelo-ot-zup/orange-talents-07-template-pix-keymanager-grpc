package br.rcmto.model

import br.rcmto.enums.TipoChave
import br.rcmto.enums.TipoConta

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
data class Chave(
    @field:Column(nullable = false)  @field:NotBlank val identificador: String,
    @field:Column(unique = true, length =77)  @field:Size(max=77) @field:NotBlank val chave: String,
    @field:Enumerated(EnumType.STRING) @field:NotBlank val tipoConta: TipoConta,
    @field:Enumerated(EnumType.STRING) @field:NotBlank val tipoChave: TipoChave
) {
    @Id
    val id:String=UUID.randomUUID().toString()
}