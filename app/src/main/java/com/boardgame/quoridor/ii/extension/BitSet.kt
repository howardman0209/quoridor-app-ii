package com.boardgame.quoridor.ii.extension

import android.util.Base64
import com.boardgame.quoridor.ii.game.qf.CustomBase64
import java.util.BitSet
import kotlin.experimental.or

fun BitSet.encodeToBase64String(): String? {
    return CustomBase64.encode(this) // Base64.encodeToString(this.toByteArrayExplicit(), Base64.NO_PADDING)
}

fun BitSet.toByteArrayExplicit(): ByteArray {
    val byteArray = ByteArray(this.size() / 8)

    // Iterate through the bits in the BitSet
    for (i in 0 until this.length()) {
        if (this[i]) {
            // Set the corresponding bit in the byte array
            byteArray[i / 8] = byteArray[i / 8] or (1 shl (7 - (i % 8))).toByte()
        }
    }
    return byteArray
}

fun BitSet.toBinaryString(size: Int? = null): String {
    val binaryString = StringBuilder()
    for (i in 0 until (size ?: this.length())) {
        binaryString.append(if (this[i]) '1' else '0')
    }
    return binaryString.toString()
}