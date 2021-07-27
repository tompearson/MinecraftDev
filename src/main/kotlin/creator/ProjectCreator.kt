/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.creator

import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.intellij.openapi.module.Module
import java.nio.file.Path

/**
 * This class represents a specific configuration for a project to be created. Typically these configurations represent
 * ([PlatformType][com.demonwav.mcdev.platform.PlatformType], [BuildSystem][com.demonwav.mcdev.creator.buildsystem.BuildSystem])
 * pairs, but this API does not apply any such restriction. Each `BuildSystem` defines how to create a [ProjectCreator]
 * instance for the given configuration using the [buildCreator][com.demonwav.mcdev.creator.buildsystem.BuildSystem.buildCreator]
 * method.
 *
 * &nbsp;
 *
 * In the current implementation the creation of [ProjectCreator] instances are delegated to project configurations via
 * implementing the [GradleCreator][com.demonwav.mcdev.creator.buildsystem.gradle.GradleCreator] and/or
 * [MavenCreator][com.demonwav.mcdev.creator.buildsystem.maven.MavenCreator] interfaces. This allows some configurations
 * to produce different [ProjectCreator] instances based on their specific configuration, such as
 * [ForgeProjectConfig][com.demonwav.mcdev.platform.forge.creator.ForgeProjectConfig].
 */
interface ProjectCreator {

    /**
     * Returns the [CreatorStep]s which should be executed in order to create the project configuration represented by
     * this [ProjectCreator].
     */
    fun getSingleModuleSteps(): Iterable<CreatorStep>

    /**
     * Returns the [CreatorStep]s which should be executed in order to create the submodule project configuration
     * represented by this [ProjectCreator].
     */
    fun getMultiModuleSteps(projectBaseDir: Path): Iterable<CreatorStep>
}

typealias JavaClassTextMapper = (packageName: String, className: String) -> String

abstract class BaseProjectCreator(
    private val rootModule: Module,
    private val buildSystem: BuildSystem
) : ProjectCreator {
    protected val project
        get() = rootModule.project

    protected fun createJavaClassStep(
        qualifiedClassName: String,
        mapper: JavaClassTextMapper
    ): BasicJavaClassStep {
        val (packageName, className) = splitPackage(qualifiedClassName)
        val classText = mapper(packageName, className)
        return BasicJavaClassStep(project, buildSystem, qualifiedClassName, classText)
    }

    protected fun splitPackage(text: String): Pair<String, String> {
        val index = text.lastIndexOf('.')
        val className = text.substring(index + 1)
        val packageName = text.substring(0, index)
        return packageName to className
    }
}

/**
 * Implement this interface on your [ProjectCreator] if you need to do extra setup work after all modules are built
 * and the project is imported, e.g. if some of the creation needs to be done in smart mode. Only gets used in
 * multi-project builds; single-module builds are expected to run these tasks themselves in the correct order, such
 * that they happen after the project is imported.
 *
 * Note: just because this interface can be used to utilize smart mode, doesn't mean that the steps will be called in
 * smart mode. If a step needs smart mode, it should wait for it itself.
 */
interface PostMultiModuleAware {
    fun getPostMultiModuleSteps(projectBaseDir: Path): Iterable<CreatorStep>
}
