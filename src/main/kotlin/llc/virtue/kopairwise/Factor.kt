package llc.virtue.kopairwise

import kotlin.reflect.KClass

data class Factor(
    val enumClass: KClass<out Enum<*>>,
    val name: String = enumClass.simpleName ?: "",
) {
    val levels: List<Enum<*>> get() = enumClass.java.enumConstants.toList()
}
