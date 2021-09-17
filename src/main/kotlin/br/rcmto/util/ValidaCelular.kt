package br.rcmto.util

fun validaCelular(celular: String): Boolean{
    val matches = celular.matches(Regex("^\\+[1-9][0-9]\\d{1,14}\$"))
    return matches
}