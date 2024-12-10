package llc.virtue.kopairwise

class Constraint(
    private val condition: (Map<String, Enum<*>>) -> Boolean
) {
    operator fun invoke(combination: Map<String, Enum<*>>): Boolean = condition(combination)

    infix fun and(other: Constraint): Constraint =
        Constraint { combination -> this(combination) && other(combination) }

    infix fun or(other: Constraint): Constraint =
        Constraint { combination -> this(combination) || other(combination) }

    infix fun implies(other: Constraint): Constraint =
        Constraint { combination -> !this(combination) || other(combination) }

    operator fun not(): Constraint =
        Constraint { combination -> !this(combination) }
}

infix fun String.eq(value: Enum<*>): Constraint =
    Constraint { combination -> if (combination.containsKey(this)) combination[this] == value else true}

infix fun String.neq(value: Enum<*>): Constraint =
    Constraint { combination -> if (combination.containsKey(this)) combination[this] != value else true}

infix fun String.inSet(values: List<Enum<*>>): Constraint =
    Constraint { combination -> if (combination.containsKey(this)) combination[this] in values else true}

infix fun String.notInSet(values: List<Enum<*>>): Constraint =
    Constraint { combination -> if (combination.containsKey(this)) combination[this] !in values else true}
