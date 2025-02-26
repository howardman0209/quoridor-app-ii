package com.boardgame.quoridor.ii.game.qf

import android.util.Log
import com.boardgame.quoridor.ii.extension.binaryToBitSet
import com.boardgame.quoridor.ii.extension.toBitSet
import java.util.BitSet

object CustomBase64 {
    private val ENCODING_TABLE = hashMapOf<BitSet, Char>(
        "000000".binaryToBitSet() to 'A', "010000".binaryToBitSet() to 'Q', "100000".binaryToBitSet() to 'g', "110000".binaryToBitSet() to 'w',
        "000001".binaryToBitSet() to 'B', "010001".binaryToBitSet() to 'R', "100001".binaryToBitSet() to 'h', "110001".binaryToBitSet() to 'x',
        "000010".binaryToBitSet() to 'C', "010010".binaryToBitSet() to 'S', "100010".binaryToBitSet() to 'i', "110010".binaryToBitSet() to 'y',
        "000011".binaryToBitSet() to 'D', "010011".binaryToBitSet() to 'T', "100011".binaryToBitSet() to 'j', "110011".binaryToBitSet() to 'z',
        "000100".binaryToBitSet() to 'E', "010100".binaryToBitSet() to 'U', "100100".binaryToBitSet() to 'k', "110100".binaryToBitSet() to '0',
        "000101".binaryToBitSet() to 'F', "010101".binaryToBitSet() to 'V', "100101".binaryToBitSet() to 'l', "110101".binaryToBitSet() to '1',
        "000110".binaryToBitSet() to 'G', "010110".binaryToBitSet() to 'W', "100110".binaryToBitSet() to 'm', "110110".binaryToBitSet() to '2',
        "000111".binaryToBitSet() to 'H', "010111".binaryToBitSet() to 'X', "100111".binaryToBitSet() to 'n', "110111".binaryToBitSet() to '3',
        "001000".binaryToBitSet() to 'I', "011000".binaryToBitSet() to 'Y', "101000".binaryToBitSet() to 'o', "111000".binaryToBitSet() to '4',
        "001001".binaryToBitSet() to 'J', "011001".binaryToBitSet() to 'Z', "101001".binaryToBitSet() to 'p', "111001".binaryToBitSet() to '5',
        "001010".binaryToBitSet() to 'K', "011010".binaryToBitSet() to 'a', "101010".binaryToBitSet() to 'q', "111010".binaryToBitSet() to '6',
        "001011".binaryToBitSet() to 'L', "011011".binaryToBitSet() to 'b', "101011".binaryToBitSet() to 'r', "111011".binaryToBitSet() to '7',
        "001100".binaryToBitSet() to 'M', "011100".binaryToBitSet() to 'c', "101100".binaryToBitSet() to 's', "111100".binaryToBitSet() to '8',
        "001101".binaryToBitSet() to 'N', "011101".binaryToBitSet() to 'd', "101101".binaryToBitSet() to 't', "111101".binaryToBitSet() to '9',
        "001110".binaryToBitSet() to 'O', "011110".binaryToBitSet() to 'e', "101110".binaryToBitSet() to 'u', "111110".binaryToBitSet() to '+',
        "001111".binaryToBitSet() to 'P', "011111".binaryToBitSet() to 'f', "101111".binaryToBitSet() to 'v', "111111".binaryToBitSet() to '/',
    )
    private val DECODING_TABLE = hashMapOf<Char, BitSet>()

    init {
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
        return encode(bitSet, (booleanArray.size / 6).coerceAtLeast(1))
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