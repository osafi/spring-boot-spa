package ms.safi.spa.devserver

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpaDevServerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register(RunSpaDevServerTask.NAME, RunSpaDevServerTask::class.java)
    }
}