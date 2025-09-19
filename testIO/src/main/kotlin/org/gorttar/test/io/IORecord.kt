package org.gorttar.test.io

sealed class IORecord {
    val typeName: String = "${this::class.simpleName}"
}

sealed class IOContentRecord : IORecord() {
    abstract val content: String
}

data class IN(override val content: String) : IOContentRecord()
data class OUT(override val content: String) : IOContentRecord()
data class ERR(override val content: String) : IOContentRecord()
data class TIMEOUT(val t: Throwable) : IORecord()
