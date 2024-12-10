package llc.virtue.kopairwise

/**
 * Generate cases using Pairwise method.
 */
fun generatePairwise(
    factors: List<Factor>,
    constraints: List<Constraint> = emptyList()
): List<Map<String, Enum<*>>> {
    if (factors.isEmpty()) return emptyList()

    val uncoveredPairs = initializeUncoveredPairs(factors)
    val combinations = mutableListOf<Map<String, Enum<*>>>()
    combination(factors) { testCase ->
        if (constraints.all { it(testCase) }) {
            if (removeCoveredPairs(uncoveredPairs, testCase)) {
                combinations.add(testCase)
            }
        }
        uncoveredPairs.isNotEmpty()
    }
    return combinations
}

fun removeCoveredPairs(uncoveredPairs: MutableMap<FactorPair, MutableSet<LevelPair>>, testCase: Map<String, Enum<*>>): Boolean {
    val removeElements = mutableListOf<Pair<FactorPair, LevelPair>>()
    for ((factorPair, levelPairs) in uncoveredPairs) {
        val l1 = testCase[factorPair.factor1.name]
        val l2 = testCase[factorPair.factor2.name]
        for (level in levelPairs) {
            val (level1, level2) = level
            if (l1 == level1 && l2 == level2) {
                removeElements.add(factorPair to level)
            }
        }
    }
    if (removeElements.isEmpty()) {
        return false
    }
    removeElements.forEach { r ->
        uncoveredPairs[r.first]?.remove(r.second)
        if (uncoveredPairs[r.first]?.isEmpty() == true) {
            uncoveredPairs.remove(r.first)
        }
    }
    return true
}

fun combination(factors: List<Factor>, action: (Map<String, Enum<*>>) -> Boolean) {
    if (factors.isEmpty()) {
        return
    }

    val indices = IntArray(factors.size)

    while (true) {
        val current = factors.indices.associate { i -> factors[i].name to factors[i].levels[indices[i]] }
        if (!action(current)) {
            break
        }

        // carry
        var carry = true
        for (i in factors.indices.reversed()) {
            if (carry) {
                indices[i] ++
                carry = false
            }
            if (indices[i] == factors[i].levels.size) {
                if (i == 0) {
                    return
                }
                indices[i] = 0
                carry = true
            }
        }
    }
}

// 未カバーのペアを初期化する関数
fun initializeUncoveredPairs(factors: List<Factor>): MutableMap<FactorPair, MutableSet<LevelPair>> {
    val uncoveredPairs = mutableMapOf<FactorPair, MutableSet<LevelPair>>()

    for (i in factors.indices) {
        for (j in (i + 1) until factors.size) {
            val factorPair = FactorPair(factors[i], factors[j])
            val pairSet = mutableSetOf<LevelPair>()

            for (level1 in factors[i].levels) {
                for (level2 in factors[j].levels) {
                    pairSet.add(LevelPair(level1, level2))
                }
            }
            uncoveredPairs[factorPair] = pairSet
        }
    }

    return uncoveredPairs
}

// 因子間のペアを表すクラス
data class FactorPair(val factor1: Factor, val factor2: Factor)

// 因子の水準間のペアを表すクラス
data class LevelPair(val level1: Enum<*>, val level2: Enum<*>)
