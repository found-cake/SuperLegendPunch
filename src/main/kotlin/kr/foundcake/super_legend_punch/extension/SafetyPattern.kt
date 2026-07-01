package kr.foundcake.super_legend_punch.extension

import java.util.Collections
import java.util.regex.Pattern

class RegexLimitExceededException : RuntimeException(null, null, false, false)

private class MatchStepCounter {
    var value: Long = 0
}

private inline fun <T> limitRegexSteps(block: () -> T): T? {
    return try {
        block()
    } catch (_: RegexLimitExceededException) {
        null
    }
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

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        source.subSequence(startIndex, endIndex).let {
            GuardedCharSequence(
                source = it,
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

    return limitRegexSteps {
        val counter = MatchStepCounter()
        matcher(GuardedCharSequence(input, maxSteps, counter)).matches()
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

private data class CachedMatch(
    val value: Boolean?
)

private val matchCache = Collections.synchronizedMap(
    object : LinkedHashMap<RegexCacheKey, Boolean?>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<RegexCacheKey, Boolean?>?
        ): Boolean = size > MAX_CACHE_SIZE
    }
)

private inline fun <T> withMatchCache(block: MutableMap<RegexCacheKey, Boolean?>.() -> T): T =
    synchronized(matchCache) {
        matchCache.block()
    }

private fun RegexCacheKey.cachedMatch(): CachedMatch? =
    withMatchCache {
        if (containsKey(this@cachedMatch)) {
            CachedMatch(get(this@cachedMatch))
        } else {
            null
        }
    }

private fun RegexCacheKey.cacheMatch(result: Boolean?) {
    withMatchCache {
        put(this@cacheMatch, result)
    }
}

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

    key.cachedMatch()?.let {
        return it.value
    }

    return safeMatches(input, maxSteps).also { result ->
        input
            .takeIf { it.length >= MIN_CACHE_LENGTH }
            ?.let { key.cacheMatch(result) }
    }
}
