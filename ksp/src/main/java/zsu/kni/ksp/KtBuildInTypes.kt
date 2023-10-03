package zsu.kni.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType

class KtBuildInTypes(private val resolver: Resolver) : KSBuiltIns by resolver.builtIns {
    val throwableType = type<Throwable>()

    val byteArray = type<ByteArray>()
    val charArray = type<CharArray>()
    val shortArray = type<ShortArray>()
    val intArray = type<IntArray>()
    val longArray = type<LongArray>()
    val floatArray = type<FloatArray>()
    val doubleArray = type<DoubleArray>()
    val booleanArray = type<BooleanArray>()

    private inline fun <reified T> type(): KSType {
        val requiredName = resolver.getClassDeclarationByName<T>()!!
        return requiredName.asType(emptyList())
    }
}
