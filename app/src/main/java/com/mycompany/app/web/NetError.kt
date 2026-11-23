package com.mycompany.app.web

/** The few Chromium network error codes the app compares against (values from Chromium's net_error_list.h). */
object NetError {
    const val ERR_TIMED_OUT = -7
    const val ERR_FILE_TOO_BIG = -8
    const val ERR_UNEXPECTED = -9
    const val ERR_ACCESS_DENIED = -10
    const val ERR_NOT_IMPLEMENTED = -11
    const val ERR_INSUFFICIENT_RESOURCES = -12
    const val ERR_OUT_OF_MEMORY = -13
    const val ERR_UPLOAD_FILE_CHANGED = -14
    const val ERR_SOCKET_NOT_CONNECTED = -15
}
