class StringOfName(inline val transform: (String) -> String = { it }) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>) = transform(property.name)
}