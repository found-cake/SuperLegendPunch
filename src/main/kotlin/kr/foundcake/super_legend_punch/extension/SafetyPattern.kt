package kr.foundcake.super_legend_punch.extension

import java.util.regex.Pattern

class RegexTimeoutException : RuntimeException() {
    override fun fillInStackTrace(): Throwable = this
}

private class GuardedCharSequence(
    private val source: CharSequence,
    private val deadlineNanos: Long
) : CharSequence {
    override val length: Int get() = source.length
    override fun get(index: Int): Char {
        if (System.nanoTime() > deadlineNanos) throw RegexTimeoutException()
        return source[index]
    }
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        GuardedCharSequence(source.subSequence(startIndex, endIndex), deadlineNanos)
}

private const val MAX_CACHE_SIZE = 500

private val matchCache = object : LinkedHashMap<String, Boolean?>(MAX_CACHE_SIZE, 0.75f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<String, Boolean?>?): Boolean =
        size > MAX_CACHE_SIZE
}

fun Pattern.safeMatches(input: String, timeoutMs: Long): Boolean? {
    val deadline = System.nanoTime() + timeoutMs * 1_000_000
    return try {
        matcher(GuardedCharSequence(input, deadline)).matches()
    } catch (_: RegexTimeoutException) {
        null
    }
}

fun Pattern.cachedSafeMatches(input: String, timeoutMs: Long = 15): Boolean? {
    matchCache[input]?.let { return it }

    val result = safeMatches(input, timeoutMs)
    matchCache[input] = result
    return result
}