package kr.foundcake.super_legend_punch.extension

import java.util.Collections
import java.util.regex.Pattern

class RegexLimitExceededException : RuntimeException(null, null, false, false)

private class MatchStepCounter {
    var value: Long = 0
}

private class GuardedCharSequence(
    private val source: CharSequence,
    private val maxSteps: Long,
    private val counter: MatchStepCounter = MatchStepCounter()
) : CharSequence {

    override val length: Int
        get() = source.length

    override fun get(index: Int): Char {
        if (++counter.value > maxSteps) {
            throw RegexLimitExceededException()
        }
        return source[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return GuardedCharSequence(
            source = source.subSequence(startIndex, endIndex),
            maxSteps = maxSteps,
            counter = counter
        )
    }
}

fun Pattern.safeMatches(
    input: String,
    maxSteps: Long = 1_000_000L
): Boolean? {
    require(maxSteps > 0) { "maxSteps must be positive" }

    return try {
        val counter = MatchStepCounter()
        matcher(GuardedCharSequence(input, maxSteps, counter)).matches()
    } catch (e: RegexLimitExceededException) {
        null
    }
}

private const val MIN_CACHE_LENGTH = 8
private const val MAX_CACHE_SIZE = 300

private data class RegexCacheKey(
    val pattern: String,
    val flags: Int,
    val input: String,
    val maxSteps: Long
)

private val matchCache = Collections.synchronizedMap(
    object : LinkedHashMap<RegexCacheKey, Boolean?>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<RegexCacheKey, Boolean?>?
        ): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }
)

fun Pattern.cachedSafeMatches(
    input: String,
    maxSteps: Long = 1_000_000L
): Boolean? {
    val key = RegexCacheKey(
        pattern = pattern(),
        flags = flags(),
        input = input,
        maxSteps = maxSteps
    )

    synchronized(matchCache) {
        if (matchCache.containsKey(key)) {
            return matchCache[key]
        }
    }

    val result = safeMatches(input, maxSteps)

    if (input.length >= MIN_CACHE_LENGTH) {
        synchronized(matchCache) {
            matchCache[key] = result
        }
    }

    return result
}
