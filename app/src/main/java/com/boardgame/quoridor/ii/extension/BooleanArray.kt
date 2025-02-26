package com.boardgame.quoridor.ii.extension

import java.util.BitSet

fun BooleanArray.toBitSet(): BitSet {
    val bitSet = BitSet(this.size)
    this.forEachIndexed { idx, bool ->
        if (bool) bitSet.set(idx)
    }
    return bitSet
}