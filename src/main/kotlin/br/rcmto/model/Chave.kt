package br.rcmto.model

import br.rcmto.enums.TipoChave
import br.rcmto.enums.TipoConta
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
data class Chave(
    @field:Enumerated(EnumType.STRING) @field:NotBlank val tipo: TipoConta,
//    @field:Column(nullable = false)  @field:NotBlank val identificador: String,
    @field:Column(unique = true, length =77)  @field:Size(max=77) @field:NotBlank var chave: String,
    @field:Enumerated(EnumType.STRING) @field:NotBlank val tipoChave: TipoChave,
    @field:Embedded val conta: ContaBanco,
    @field:CreationTimestamp var criadoEm: LocalDateTime = LocalDateTime.now()
) {
    @Id
    val id:String=UUID.randomUUID().toString()

    fun atualiza(chave: String, dateTime: LocalDateTime){
        this.chave = chave
        this.criadoEm = dateTime
    }
}