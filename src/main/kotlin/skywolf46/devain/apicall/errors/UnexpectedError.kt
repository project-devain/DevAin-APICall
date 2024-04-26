package skywolf46.devain.apicall.errors

import skywolf46.devain.apicall.APIError

open class UnexpectedError(val exception: Throwable) : APIError {
    override fun getErrorMessage(): String {
        return "${exception.javaClass.name} : ${exception.message}"
    }
}