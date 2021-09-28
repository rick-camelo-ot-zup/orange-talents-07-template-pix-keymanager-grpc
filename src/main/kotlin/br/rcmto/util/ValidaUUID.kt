package br.rcmto.util

fun validaUUID(chave: String): Boolean{
    val matches = chave.matches(Regex("^[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}\$"))
    return matches
}