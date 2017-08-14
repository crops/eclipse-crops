/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.yocto.crops.internal.sdk.core.Activator;

public class YoctoBuildConfiguration extends ContainerCBuildConfiguration {

	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "cmake.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cmake.command.clean"; //$NON-NLS-1$

	private static final String TOOLCHAIN_FILE = "cdt.cmake.toolchainfile"; //$NON-NLS-1$

	private ICMakeToolChainFile toolChainFile;

	public YoctoBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		Preferences settings = getSettings();
		String pathStr = settings.get(TOOLCHAIN_FILE, ""); //$NON-NLS-1$
		if (!pathStr.isEmpty()) {
			Path path = Paths.get(pathStr);
			toolChainFile = manager.getToolChainFile(path);
		}
	}

	public YoctoBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public YoctoBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode) {
		super(config, name, toolChain, launchMode);
		this.toolChainFile = toolChainFile;

		if (toolChainFile != null) {
			Preferences settings = getSettings();
			settings.put(TOOLCHAIN_FILE, toolChainFile.getPath().toString());
			try {
				settings.flush();
			} catch (BackingStoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			Map<String, String> properties = getProperties();
			String generator = properties.get(CMAKE_GENERATOR);

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();
			org.eclipse.core.runtime.Path buildDir2 = new org.eclipse.core.runtime.Path(buildDir.toString());

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				outStream.write("CMakeFiles not found. Assuming clean.");
				return;
			}

			String cleanCommand = properties.get(CLEAN_COMMAND);
			if (cleanCommand == null) {
				if (generator != null && generator.equals("Ninja")) { //$NON-NLS-1$
					cleanCommand = "ninja clean"; //$NON-NLS-1$
				} else {
					cleanCommand = "make clean"; //$NON-NLS-1$
				}
			}

			launchAndWait(Arrays.asList(cleanCommand.split(" ")), buildDir2, console, monitor);
			
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format("Cleaning %s", project.getName()), e));
		}
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			Map<String, String> properties = getProperties();
			String generator = properties.get(CMAKE_GENERATOR);
			if (generator == null) {
				generator = "\"Unix Makefiles\""; //$NON-NLS-1$
			}

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();
			org.eclipse.core.runtime.Path buildDir2 = new org.eclipse.core.runtime.Path(buildDir.toString());

			outStream.write(String.format("Building in: %s\n", buildDir.toString()));

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				List<String> command = new ArrayList<>();

				// TODO location of CMake out of preferences if not found here
				command.add("cmake");

				command.add("-G"); //$NON-NLS-1$
				command.add(generator);

				if (toolChainFile != null) {
					command.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
				}

				command.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$
				command.add(new File(project.getLocationURI()).getAbsolutePath());

				launchAndWait(command, buildDir2, console, monitor);

			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				String buildCommand = properties.get(BUILD_COMMAND);
				if (buildCommand == null) {
					if (generator.equals("Ninja")) { //$NON-NLS-1$
						buildCommand = "ninja"; //$NON-NLS-1$
					} else {
						buildCommand = "make"; //$NON-NLS-1$
					}
				}

				launchAndWait(Arrays.asList(buildCommand.split(" ")), buildDir2, console, monitor);
				
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Load compile_commands.json file
			processCompileCommandsFile(monitor);

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format("Building %s", project.getName()), e));
		}
	}


}
