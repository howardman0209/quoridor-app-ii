package com.boardgame.quoridor.ii.extension

import java.util.BitSet

fun ByteArray.toBinaryString(): String {
    val binaryString = StringBuilder()

    for (byte in this) {
        binaryString.append(String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0'))
    }

    return binaryString.toString()
}

fun ByteArray.toBitSet(): BitSet {
    val bitSet = BitSet(this.size * 8)

    // Iterate through each byte in the ByteArray
    for (i in this.indices) {
        val byte = this[i]
        // Set bits in the BitSet for the current byte
        for (j in 0 until 8) {
            if ((byte.toInt() and (1 shl (7 - j))) != 0) {
                bitSet.set(i * 8 + j)
            }
        }
    }

    return bitSet
}