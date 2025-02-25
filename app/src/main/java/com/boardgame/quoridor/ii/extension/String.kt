package com.boardgame.quoridor.ii.extension

import android.util.Base64
import java.util.BitSet

fun String.decodeBase64ToBitSet(): BitSet {
    return Base64.decode(this, Base64.URL_SAFE).toBitSet()
}