package ms.safi.spring.spa.util.files.junit

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TempFileBuilder(
    val rootOnClasspath: Boolean = false
)
