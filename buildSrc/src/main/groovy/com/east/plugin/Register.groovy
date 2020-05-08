package com.east.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by hp on 2016/4/8.
 */
class Register implements Plugin<Project> {
    @Override
    void apply(Project project) {
//        def android = project.extensions.findByType(AppExtension)
//        android.registerTransform(new PreDexTransform(project))
        project.logger.error "================自定义插件成功！=========="
    }
}