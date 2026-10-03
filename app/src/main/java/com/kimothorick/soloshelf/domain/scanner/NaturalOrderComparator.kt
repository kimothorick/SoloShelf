package com.kimothorick.soloshelf.domain.scanner

class NaturalOrderComparator : Comparator<String> {

    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == null && s2 == null) return 0
        if (s1 == null) return -1
        if (s2 == null) return 1

        var i1 = 0
        var i2 = 0

        while (i1 < s1.length && i2 < s2.length) {
            val c1 = s1[i1]
            val c2 = s2[i2]

            if (c1.isDigit() && c2.isDigit()) {
                val num1 = extractNumber(s1, i1)
                val num2 = extractNumber(s2, i2)

                val comp = num1.value.compareTo(num2.value)
                if (comp != 0) return comp

                i1 = num1.nextIndex
                i2 = num2.nextIndex
            } else {
                val comp = c1.lowercaseChar().compareTo(c2.lowercaseChar())
                if (comp != 0) return comp
                i1++
                i2++
            }
        }

        return s1.length.compareTo(s2.length)
    }

    private data class ExtractedNumber(val value: Long, val nextIndex: Int)

    private fun extractNumber(s: String, startIndex: Int): ExtractedNumber {
        var idx = startIndex
        var num = 0L
        while (idx < s.length && s[idx].isDigit()) {
            num = num * 10 + (s[idx] - '0')
            idx++
        }
        return ExtractedNumber(num, idx)
    }
}
