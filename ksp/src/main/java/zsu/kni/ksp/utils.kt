package zsu.kni.ksp

import zsu.kni.JniApi
import zsu.kni.JniImpl
import zsu.kni.JniShared

val jniSharedFqn: String = JniShared::class.java.name
val jniApiFqn: String = JniApi::class.java.name
val jniImplFqn: String = JniImpl::class.java.name