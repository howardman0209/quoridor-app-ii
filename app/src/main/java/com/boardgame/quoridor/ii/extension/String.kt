package com.boardgame.quoridor.ii.extension

import android.util.Base64
import com.boardgame.quoridor.ii.game.qf.CustomBase64
import java.util.BitSet

fun String.decodeBase64ToBitSet(): BitSet {
    return CustomBase64.decode(this) // Base64.decode(this.addBase64Padding(), Base64.NO_PADDING).toBitSet()
}

fun String.binaryToBitSet(): BitSet {
    val bitSet = BitSet(this.length)
    this.forEachIndexed { idx, char ->
        if (char == '1') {
            bitSet.set(idx)
        }
    }
    return bitSet
}