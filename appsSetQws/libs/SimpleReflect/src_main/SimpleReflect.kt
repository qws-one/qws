object SimpleReflect {

    class Delegate {
        abstract class Base(val jObj: Any)

        class R<DR>(val wrap: (Any) -> DR) {
            operator fun getValue(thisRef: Base, property: kotlin.reflect.KProperty<*>): DR =
                Value.nullable<DR>(thisRef.jObj, property.name)?.let { wrap(it) } ?: TODO()
        }

        class C<R>(val wrap: (Any?) -> R) {
            operator fun getValue(thisRef: Base, property: kotlin.reflect.KProperty<*>): R =
                wrap(Value.nullable(thisRef.jObj, property.name))

            companion object {
                inline operator fun <reified T> invoke(): C<T> = C { if (it is T) it else TODO() }
            }
        }
    }

    class Value {
        class Wrap(val any: Any) {
            operator fun get(name: String) = Wrap(value(name))

            @Suppress("UNCHECKED_CAST")
            fun <T> value(): T = any as T

            fun <T> value(name: String): T = invoke(any, name)

            fun <T> value(name: String, defaultValue: T): T = invoke(any, name, defaultValue)
        }

        companion object {
            private fun Class<Any>.lookupField(fieldName: String): java.lang.reflect.Field? = try {
                getDeclaredField(fieldName)
            } catch (e: Exception) {
                null
            } ?: superclass?.lookupField(fieldName)

            fun <T> nullable(any: Any, fieldName: String): T? = try {
                any.javaClass.lookupField(fieldName)?.run {
                    trySetAccessible()
                    @Suppress("UNCHECKED_CAST")
                    get(any) as? T
                }
            } catch (e: Exception) {
                null
            }

            operator fun <T> invoke(any: Any, name: String): T {
                val names = name.split('.')
                var value = any
                for (nm in names) {
                    val fieldName = nm.trim()
                    value = nullable(value, fieldName)!!
                }
                @Suppress("UNCHECKED_CAST")
                return value as T
            }

            operator fun <T> invoke(any: Any, name: String, defaultValue: T): T {
                val names = name.split('.')
                var value = any
                for (nm in names) {
                    val fieldName = nm.trim()
                    value = nullable(value, fieldName) ?: return defaultValue
                }
                @Suppress("UNCHECKED_CAST")
                return value as T
            }

            operator fun invoke(any: Any) = Wrap(any)
        }
    }
}
