package com.boardgame.quoridor.ii.extension

import android.util.Base64
import com.boardgame.quoridor.ii.game.qf.CustomBase64
import java.util.BitSet

fun String.decodeBase64ToBitSet(): BitSet {
    // CustomBase64.decode(this)
    // Base64.decode(this.base64AddPadding(), Base64.NO_PADDING).toBitSet()
    return CustomBase64.decode(this)
}

fun String.base64AddPadding(): String {
    return when (this.length % 4) {
        1 -> "${this}A=="
        2 -> "$this=="
        3 -> "$this="
        else -> this
    }
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