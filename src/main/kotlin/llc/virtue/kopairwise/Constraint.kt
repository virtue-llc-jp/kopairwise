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
    Constraint { combination -> combination[this] == value }

infix fun String.neq(value: Enum<*>): Constraint =
    Constraint { combination -> combination[this] != value }

infix fun String.inSet(values: List<Enum<*>>): Constraint =
    Constraint { combination -> combination[this] in values }

infix fun String.notInSet(values: List<Enum<*>>): Constraint =
    Constraint { combination -> combination[this] !in values }
