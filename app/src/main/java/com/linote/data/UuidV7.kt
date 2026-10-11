package com.linote.data

import java.security.SecureRandom
import java.util.UUID

object UuidV7 {
    private val random = SecureRandom()

    fun generate(timestampMillis: Long = System.currentTimeMillis()): UUID {
        val entropy = ByteArray(10)
        random.nextBytes(entropy)
        val randA = ((entropy[0].toInt() and 0xff) shl 4) or ((entropy[1].toInt() and 0xf0) ushr 4)
        val mostSigBits = ((timestampMillis and 0xffff_ffff_ffffL) shl 16) or
            (0x7L shl 12) or
            (randA.toLong() and 0xfffL)
        var randB = 0L
        for (i in 2 until entropy.size) {
            randB = (randB shl 8) or (entropy[i].toLong() and 0xff)
        }
        val leastSigBits = (randB and 0x3fff_ffff_ffff_ffffL) or (1L shl 63)
        return UUID(mostSigBits, leastSigBits)
    }
}
