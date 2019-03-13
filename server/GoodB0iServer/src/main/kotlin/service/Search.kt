package service

import java.text.Normalizer
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object Search {

    private const val DEFAULT_SCALING_FACTOR = 0.1
    private const val SCORE_THRESHOLD = 0.6

    private inline fun <T, R> Iterable<T>.mapThenFilter(transform: (T) -> R, predicate: (R) -> Boolean): List<R> {
        val out = ArrayList<R>()
        for (element in this) {
            val transformed = transform(element)
            if (predicate(transformed)) out.add(transformed)
        }
        return out
    }


//    fun search(data: Collection<String>, query: String, threshold: Double = SCORE_THRESHOLD): Collection<String> = data.map { item ->
//        Pair(item, getAdjustedJaroWinklerSimilarity(item, query))
//    }.filter { it.second > threshold }.sortedBy { it.second }.map { it. }


    fun <T> search(data: Collection<T>, query: String, fields: (T) -> Collection<String>, threshold: Double = SCORE_THRESHOLD): Collection<T> =
            data.filter { item -> fields(item).any { field -> field.toLowerCase().contains(query) } }
            //data.map { item -> Pair(item, computeSimilarity(item, fields, query)) }.filter { it.second > threshold }.sortedBy { it.second }.map { it.first }


    private inline fun <T> computeSimilarity(item: T, fields: (T) -> Collection<String>, query: String): Double =
            fields(item).foldIndexed(0.0) { index: Int, score: Double, field: String ->
                max(score, getAdjustedJaroWinklerSimilarity(field, query) - (index * 0.001))
            }


    /*
    Jaro-Winkler similarity is a similarity measure weighted by the number of matching characters and
    transpositions, weighted by the prefix length (strings matching from the beginning)
    https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance#Jaro%E2%80%93Winkler_Similarity

     */

    private fun getAdjustedJaroWinklerSimilarity(first: String, second: String): Double {
        if (first.isEmpty() || second.isEmpty()) return 0.0
        val split = first.split("\\s") // whitespace split
        val nonAdjusted = getJaroWinklerSimilarity(first, second)
        return if (split.size == 1)
            nonAdjusted
        else
            split.fold(nonAdjusted) { max, substr ->
                max(max, getJaroWinklerSimilarity(substr, second))
            }
    }

    private fun getJaroWinklerSimilarity(first: String, second: String): Double {
        val fl = Normalizer.normalize(first.toLowerCase(), Normalizer.Form.NFD)
        val sl = Normalizer.normalize(second.toLowerCase(), Normalizer.Form.NFD)
        val mtp = matches(fl, sl)
        val matches = mtp[0]
        if (matches == 0) return 0.0
        val j = ((matches / fl.length + matches / sl.length + (matches - mtp[1]) / matches)) / 3.0
        val jw = if (j < 0.7) j else j + min(DEFAULT_SCALING_FACTOR, 1.0 / mtp[3]) * mtp[2] * (1 - j)
        return round(jw * 100.0) / 100.0
    }

//    private fun matches(first: CharSequence, second: CharSequence): IntArray {
//        val max: CharSequence
//        val min: CharSequence
//        if (first.length > second.length) {
//            max = first
//            min = second
//        } else {
//            min = first
//            max = second
//        }
//        val range = max(max.length / 2 - 1, 0)
//        val matchIndexes = IntArray(min.length) { -1 }
//        val matchFlags = BooleanArray(max.length)
//        var matches = 0
//        for (minIndex in 0 until min.length) {
//            val from = max(minIndex - range, 0)
//            val to = min(minIndex + range + 1, max.length)
//            for (maxIndex in from until to) {
//                if (!matchFlags[maxIndex] && min[minIndex] == max[maxIndex]) {
//                    matchIndexes[minIndex] = maxIndex
//                    matchFlags[maxIndex] = true
//                    matches++
//                    break
//                }
//            }
//        }
//        val ms1 = CharArray(matches)
//        val ms2 = CharArray(matches)
//        var si = 0
//        min.forEachIndexed { minIndex, c ->
//            if (matchIndexes[minIndex] != -1) {
//                ms1[si] = c
//                si++
//            }
//        }
//        si = 0
//        max.forEachIndexed { maxIndex, c ->
//            if (matchFlags[maxIndex]) {
//                ms2[si] = c
//                si++
//            }
//        }
//        // How much slower is this than a for loop?
//        val transpositions = ms1.zip(ms2).count { it.first != it.second }
//
//        var prefix = 0
//        for (index in 0 until min.length) {
//            if (first[index] == second[index]) {
//                prefix++
//            } else {
//                break
//            }
//        }
//
//        return intArrayOf(matches, transpositions / 2, prefix, max.length)
//    }

    private fun matches(first: CharSequence, second: CharSequence): IntArray {
        val max: CharSequence
        val min: CharSequence
        if (first.length > second.length) {
            max = first
            min = second
        } else {
            max = second
            min = first
        }
        val range = Math.max(max.length / 2 - 1, 0)
        val matchIndexes = IntArray(min.length)
        Arrays.fill(matchIndexes, -1)
        val matchFlags = BooleanArray(max.length)
        var matches = 0
        for (mi in 0 until min.length) {
            val c1 = min[mi]
            var xi = Math.max(mi - range, 0)
            val xn = Math.min(mi + range + 1, max.length)
            while (xi < xn) {
                if (!matchFlags[xi] && c1 == max[xi]) {
                    matchIndexes[mi] = xi
                    matchFlags[xi] = true
                    matches++
                    break
                }
                xi++
            }
        }
        val ms1 = CharArray(matches)
        val ms2 = CharArray(matches)
        run {
            var i = 0
            var si = 0
            while (i < min.length) {
                if (matchIndexes[i] != -1) {
                    ms1[si] = min[i]
                    si++
                }
                i++
            }
        }
        var i = 0
        var si = 0
        while (i < max.length) {
            if (matchFlags[i]) {
                ms2[si] = max[i]
                si++
            }
            i++
        }
        var transpositions = 0
        for (mi in ms1.indices) {
            if (ms1[mi] != ms2[mi]) {
                transpositions++
            }
        }
        var prefix = 0
        for (mi in 0 until min.length) {
            if (first[mi] == second[mi]) {
                prefix++
            } else {
                break
            }
        }
        return intArrayOf(matches, transpositions / 2, prefix, max.length)
    }

}