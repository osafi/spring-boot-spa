package ms.safi.spring.spa.util.files.junit

import ms.safi.spring.spa.util.files.FileBuilder
import org.junit.jupiter.api.extension.*
import java.lang.reflect.Constructor

internal class TemporaryFileBuilder : ParameterResolver, AfterTestExecutionCallback {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val annotated = parameterContext.isAnnotated(TempFileBuilder::class.java)
        if (annotated && parameterContext.declaringExecutable is Constructor<*>) {
            throw ParameterResolutionException(
                "@TempFileBuilder is not supported on constructor parameters. Please use field injection instead."
            )
        }
        return annotated
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        assertSupportedType(parameterContext.parameter.type)
        val annotation = parameterContext.findAnnotation(TempFileBuilder::class.java).get()
        return extensionContext.getStore(NAMESPACE)
            .getOrComputeIfAbsent(KEY) { FileBuilder(rootOnClasspath = annotation.rootOnClasspath) }
    }

    private fun assertSupportedType(type: Class<*>) {
        if (type != FileBuilder::class.java) {
            throw ExtensionConfigurationException(
                "Can only resolve @TempFileBuilder parameter of type ${FileBuilder::class.java.name} but was: ${type.name}"
            )
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        context.getStore(NAMESPACE).get(KEY, FileBuilder::class.java)?.deleteCreatedFiles()
    }

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(FileBuilder::class.java)
        private const val KEY = "temp.fileBuilder"
    }
}