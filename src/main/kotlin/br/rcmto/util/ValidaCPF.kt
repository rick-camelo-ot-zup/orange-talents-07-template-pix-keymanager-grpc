package br.rcmto.util

fun validaCpf(cpf: String): Boolean {
    val matches = cpf.matches(Regex("^[0-9]{11}\$"))
    return matches
}