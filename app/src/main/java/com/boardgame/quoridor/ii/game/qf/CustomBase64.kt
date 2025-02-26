package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.extension.binaryToBitSet
import com.boardgame.quoridor.ii.extension.toBitSet
import java.util.BitSet

object CustomBase64 {
    private val ENCODING_TABLE = mutableMapOf<BitSet, Char>()
    private val DECODING_TABLE = mutableMapOf<Char, BitSet>()

    private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    init {
        BASE64_CHARS.forEachIndexed { idx, b64Char ->
            ENCODING_TABLE.put(idx.toString(2).padStart(6, '0').binaryToBitSet(), b64Char)
        }

        ENCODING_TABLE.entries.forEach { (key, value) ->
            DECODING_TABLE.put(value, key)
        }
    }

    fun encode(bitSet: BitSet, size: Int): String {
        return (0 until size).joinToString("") { i ->
            val segment = bitSet.get(i * 6, (i + 1) * 6)
            ENCODING_TABLE[segment].toString()
        }
    }

    fun encode(booleanArray: BooleanArray): String {
        val bitSet = booleanArray.toBitSet()
        return encode(bitSet, ((booleanArray.size + 6) / 6).coerceAtLeast(1))
    }

    fun decode(base64: String): BitSet {
        val bitSet = BitSet(base64.length * 6)

        base64.forEachIndexed { index, char ->
            val segment = DECODING_TABLE[char]
            segment?.let {
                for (j in 0 until 6) {
                    bitSet.set(index * 6 + j, it[j])
                }
            }
        }

        return bitSet
    }


}