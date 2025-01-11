package llc.virtue.kopairwise

import kotlin.reflect.KClass

/**
 * Represents a factor in a pairwise comparison.
 *
 * @param enumClass The enum class representing the factor levels.
 * @param name The name of the factor. If not provided, it will be derived from the decapitalized enum class name
 *             (if 2nd char of the name also capital, stay capital).
 */
data class Factor(
    val enumClass: KClass<out Enum<*>>,
    val name: String = enumClass.simpleName?.let {
        if (it.length >= 2 && it[0].isUpperCase() && it[1].isLowerCase()) {
            enumClass.simpleName?.replaceFirstChar { it.lowercase() }
        } else {
            enumClass.simpleName
        }
    } ?: "",
) {
    val levels: List<Enum<*>> get() = enumClass.java.enumConstants.toList()
}
