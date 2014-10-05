package target.result

/**
 * Created by soywiz on 5/10/14.
 */
case class ClassResult(clazz: SootClass, methods: List[MethodResult], declaration: String, definition: String, referencedClasses: List[SootClass], nativeFramework: String, nativeLibrary: String, cflags: String, staticConstructor:StaticConstructorResult)
