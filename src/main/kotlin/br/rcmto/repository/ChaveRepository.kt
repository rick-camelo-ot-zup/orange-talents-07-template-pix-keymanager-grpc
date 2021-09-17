package br.rcmto.repository

import br.rcmto.model.Chave
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChaveRepository:JpaRepository<Chave,String> {
    fun existsByChave(chave: String):Boolean
}