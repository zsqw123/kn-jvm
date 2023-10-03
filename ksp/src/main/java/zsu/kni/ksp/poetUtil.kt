package zsu.kni.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName

// copy from kotlin poet, it is internal in poet sadly :(
internal fun TypeName.rawType(): ClassName {
    return findRawType() ?: throw IllegalArgumentException("Cannot get raw type from $this")
}

internal fun TypeName.findRawType(): ClassName? {
    return when (this) {
        is ClassName -> this
        is ParameterizedTypeName -> rawType
        else -> null
    }
}

fun KSFunctionDeclaration.asMemberName(): MemberName {
    val packageName = packageName.asString()
    val parent = parentDeclaration as? KSClassDeclaration
    return if (parent == null) {
        MemberName(packageName, simpleName.asString())
    }else{
        MemberName(parent.toClassName(), simpleName.asString())
    }
}
