package com.boardgame.quoridor.ii.util

import android.util.Log
import kotlin.text.format

object DebugUtil {
    const val TAG = "DEBUG"

    inline fun <T> measureExecutionTime(block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block.invoke()
        val endTime = System.nanoTime()
        val executionTime = (endTime - startTime) / 1_000_000_000.0
        val message = "Execution time: %.6f seconds".format(executionTime)
        Log.d(TAG, message)
        return result
    }
}