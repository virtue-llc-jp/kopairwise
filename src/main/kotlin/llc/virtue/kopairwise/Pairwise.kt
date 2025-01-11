package llc.virtue.kopairwise

import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberProperties

open class Pairwise(builder: PairwiseBuilder.() -> Unit) {
    private val factors: List<Factor>
    private val constraints: List<Constraint>

    init {
        val pairwiseBuilder = PairwiseBuilder().apply(builder)
        factors = pairwiseBuilder.factors
        constraints = pairwiseBuilder.constraints
    }

    fun cases(): List<Map<String, Enum<*>>> {
        return generatePairwise(factors, constraints)
    }

    fun invertCases(): List<Map<String, Enum<*>>> {
        return generatePairwise(factors, constraints, invert = true)
    }

    fun <T : Any> cases(kClass: KClass<T>): List<T> {
        return typing(kClass, cases())
    }

    fun <T : Any> invertCases(kClass: KClass<T>): List<T> {
        return typing(kClass, invertCases())
    }

    private fun <T : Any> typing(kClass: KClass<T>, cases: List<Map<String, Enum<*>>>): List<T> {
        return cases.map { case ->
            // find constructor
            println(kClass.constructors)
            val constructor = kClass.constructors.find { constructor ->
                case.keys.all { factorName ->
                    factorName in constructor.parameters.map { it.name }
                }
            } ?: throw IllegalArgumentException("Constructor not found for $kClass")
            // create args
            val args = constructor.parameters.associateWith { param ->
                case[param.name]
            }
            constructor.callBy(args)
        }
    }
}

class PairwiseBuilder(
    internal val factors: MutableList<Factor> = mutableListOf(),
    internal val constraints: MutableList<Constraint> = mutableListOf()
) {
    fun factor(enumClass: KClass<out Enum<*>>) {
        factors.add(Factor(enumClass))
    }

    fun factors(vararg enumClasses: KClass<out Enum<*>>) {
        factors.addAll(enumClasses.map { Factor(it) })
    }

    fun constraint(constraint: Constraint) {
        constraints.add(constraint)
    }

    fun constraints(vararg constraints: Constraint) {
        this.constraints.addAll(constraints)
    }
}

/**
 * Generate cases using Pairwise method.
 */
internal fun generatePairwise(
    factors: List<Factor>,
    constraints: List<Constraint> = emptyList(),
    invert: Boolean = false,
): List<Map<String, Enum<*>>> {
    // no factor, no test
    if (factors.isEmpty()) return emptyList()
    // single factor
    if (factors.size == 1) {
        return factors.first().let { factor ->
            factor.levels.map {
                mapOf(factors.first().name to it)
            }
        }
    }

    val coverageLimit = getCoverageLimit(factors)
    val uncoveredPairs = initializeUncoveredPairs(factors, constraints, invert)
    val combinations = mutableListOf<Map<String, Enum<*>>>()
    val allCases = mutableListOf<Map<String, Enum<*>>>()
    combination(factors) { allCases.add(it) }
    while (true) {
        var candidateCase: Map<String, Enum<*>>? = null
        var maxCoverage = listOf<Pair<FactorPair, LevelPair>>()
        var foundAt = -1
        for ((i, testCase) in allCases.withIndex()) {
            if (constraints.all { it(testCase) } != invert) {
                val coverage = searchCoveredPairs(uncoveredPairs, testCase)
                if (coverage.size > maxCoverage.size) {
                    candidateCase = testCase
                    maxCoverage = coverage
                    foundAt = i
                    if (coverage.size >= coverageLimit) {
                        break
                    }
                }
            }
        }
        if (candidateCase == null) {
            // Failed to increase coverage
            break
        }
        combinations.add(candidateCase)
        removeCoveredPairs(maxCoverage, uncoveredPairs)
        allCases.removeAt(foundAt)
        if (uncoveredPairs.isEmpty()) {
            // Successfully covered
            break
        }
    }

    return combinations
}

private fun getCoverageLimit(factors: List<Factor>): Int {
    return factors.size.let { size ->
        if (size == 1) {
            1
        } else {
            (size * (size - 1)) / 2
        }
    }
}

private fun removeCoveredPairs(
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
private fun searchCoveredPairs(
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

internal fun initializeUncoveredPairs(
    factors: List<Factor>,
    constraints: List<Constraint>,
    invert: Boolean = false,
): MutableMap<FactorPair, MutableSet<LevelPair>> {
    val uncoveredPairs = mutableMapOf<FactorPair, MutableSet<LevelPair>>()

    for (i in factors.indices) {
        for (j in (i + 1) until factors.size) {
            val factorPair = FactorPair(factors[i], factors[j])
            val pairSet = mutableSetOf<LevelPair>()

            for (level1 in factors[i].levels) {
                for (level2 in factors[j].levels) {
                    val testCase = mapOf(factors[i].name to level1, factors[j].name to level2)
                    if (constraints.all { it(testCase) } != invert) {
                        pairSet.add(LevelPair(level1, level2))
                    }
                }
            }
            uncoveredPairs[factorPair] = pairSet
        }
    }
    return uncoveredPairs
}

data class FactorPair(val factor1: Factor, val factor2: Factor)

data class LevelPair(val level1: Enum<*>, val level2: Enum<*>)
