package llc.virtue.kopairwise

/**
 * Generate cases using Pairwise method.
 */
fun generatePairwise(
    factors: List<Factor>,
    constraints: List<Constraint> = emptyList()
): List<Map<String, Enum<*>>> {
    if (factors.isEmpty()) return emptyList()

    val uncoveredPairs = initializeUncoveredPairs(factors, constraints)
    val combinations = mutableListOf<Map<String, Enum<*>>>()
    val allCases = mutableListOf<Map<String, Enum<*>>>()
    combination(factors) { allCases.add(it) }
    while (true) {
        var candidateCase: Map<String, Enum<*>>? = null
        var maxCoverage = listOf<Pair<FactorPair, LevelPair>>()
        var foundAt = -1
        for ((i, testCase) in allCases.withIndex()) {
            if (constraints.all { it(testCase) }) {
                val coverage = searchCoverage(uncoveredPairs, testCase)
                if (coverage.size > maxCoverage.size) {
                    candidateCase = testCase
                    maxCoverage = coverage
                    foundAt = i
                }
            }
        }
        if (foundAt == -1) {
            // Failed to increase coverage
            break
        }
        combinations.add(candidateCase!!)
        removeCoverage(maxCoverage, uncoveredPairs)
        allCases.removeAt(foundAt)
        if (uncoveredPairs.isEmpty()) {
            // Successfully covered
            break
        }
    }

    return combinations
}

private fun removeCoverage(
    maxCoverage: List<Pair<FactorPair, LevelPair>>,
    uncoveredPairs: MutableMap<FactorPair, MutableSet<LevelPair>>
) {
    maxCoverage.forEach { r ->
        uncoveredPairs[r.first]?.remove(r.second)
        if (uncoveredPairs[r.first]?.isEmpty() == true) {
            uncoveredPairs.remove(r.first)
        }
    }
}

/**
 * Search testCase with the most coverage and remove covered pairs from uncoveredPairs.
 */
private fun searchCoverage(
    uncoveredPairs: MutableMap<FactorPair, MutableSet<LevelPair>>,
    testCase: Map<String, Enum<*>>
): List<Pair<FactorPair, LevelPair>> {
    val coverage = mutableListOf<Pair<FactorPair, LevelPair>>()
    for ((factorPair, levelPairs) in uncoveredPairs) {
        val l1 = testCase[factorPair.factor1.name]
        val l2 = testCase[factorPair.factor2.name]
        for (level in levelPairs) {
            val (level1, level2) = level
            if (l1 == level1 && l2 == level2) {
                coverage.add(factorPair to level)
            }
        }
    }
    return coverage
}

private fun combination(factors: List<Factor>, action: (Map<String, Enum<*>>) -> Unit) {
    if (factors.isEmpty()) {
        return
    }

    val indices = IntArray(factors.size)

    while (true) {
        val current = factors.indices.associate { i -> factors[i].name to factors[i].levels[indices[i]] }
        action(current)

        // carry
        var carry = true
        for (i in factors.indices.reversed()) {
            if (carry) {
                indices[i]++
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
fun initializeUncoveredPairs(
    factors: List<Factor>,
    constraints: List<Constraint>
): MutableMap<FactorPair, MutableSet<LevelPair>> {
    val uncoveredPairs = mutableMapOf<FactorPair, MutableSet<LevelPair>>()

    for (i in factors.indices) {
        for (j in (i + 1) until factors.size) {
            val factorPair = FactorPair(factors[i], factors[j])
            val pairSet = mutableSetOf<LevelPair>()

            for (level1 in factors[i].levels) {
                for (level2 in factors[j].levels) {
                    val testCase = mapOf(factors[i].name to level1, factors[j].name to level2)
                    if (constraints.all { it(testCase) }) {
                        pairSet.add(LevelPair(level1, level2))
                    }
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
