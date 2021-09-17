package br.rcmto.util

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator

fun validaEmail(email: String): Boolean{

    val emailValidator = EmailValidator()

    emailValidator.initialize(null)

    return emailValidator.isValid(email, null)

}