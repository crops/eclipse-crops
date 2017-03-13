/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.internal.container.docker.build.cmake;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IContainerLaunchListener;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

public class DockerCMakeBuildConfiguration extends CBuildConfiguration {

	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "cmake.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cmake.command.clean"; //$NON-NLS-1$

	private static final String TOOLCHAIN_FILE = "cdt.cmake.toolchainfile"; //$NON-NLS-1$

	private ICMakeToolChainFile toolChainFile;
	private ContainerLauncher launcher;

	private class StartCMakeServerJob extends Job implements IContainerLaunchListener {

		private boolean started;
		private boolean done;
		private IDockerContainerInfo info;

		public StartCMakeServerJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

			while (!done) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				if (started && getIpAddress() != null)
					done = true;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		@Override
		public void newOutput(String output) {
			// XXX this is for debugging/temporary
			System.out.println("Docker CMake Output: "+output);
			started = true;
		}

		public String getIpAddress() {
			if (info != null) {
				IDockerNetworkSettings networkSettings = info.networkSettings();
				return networkSettings.ipAddress();
			}
			return null;
		}

		@Override
		public void done() {
			done = true;
		}

		@Override
		public void containerInfo(IDockerContainerInfo info) {
			this.info = info;
		}
	}

	public DockerCMakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		Preferences settings = getSettings();
		String pathStr = settings.get(TOOLCHAIN_FILE, ""); //$NON-NLS-1$
		if (!pathStr.isEmpty()) {
			Path path = Paths.get(pathStr);
			toolChainFile = manager.getToolChainFile(path);
		}
		launcher = new ContainerLauncher();
	}

	public DockerCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public DockerCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
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
		launcher = new ContainerLauncher();
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
			String[] command = cleanCommand.split(" "); //$NON-NLS-1$

			Path cmdPath = findCommand(command[0]);
			if (cmdPath != null) {
				command[0] = cmdPath.toString();
			}

			StringBuffer cmd = new StringBuffer("");
			for (String c : command) 
				cmd.append(c).append(" ");
		
			launchAndWait("unix:///var/run/docker.sock","bavery/scott:ubuntu16.04","2345",cmd.toString(),project,buildDir.toString());

//			ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
//			Process process = processBuilder.start();
//			outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
//			watchProcess(process, new IConsoleParser[0], console);
//
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
				generator = "Unix Makefiles"; //$NON-NLS-1$
			}

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream outStream = console.getOutputStream();

			Path buildDir = getBuildDirectory();

			outStream.write(String.format("Building in: %s\n", buildDir.toString()));

			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				List<String> command = new ArrayList<>();

				// TODO location of CMake out of preferences if not found here
				Path cmakePath = findCommand("cmake"); //$NON-NLS-1$
				if (cmakePath != null) {
					command.add(cmakePath.toString());
				} else {
					command.add("cmake"); //$NON-NLS-1$
				}

				command.add("-G"); //$NON-NLS-1$
				command.add(generator);

				if (toolChainFile != null) {
					command.add("-DCMAKE_TOOLCHAIN_FILE=" + toolChainFile.getPath().toString()); //$NON-NLS-1$
				}

				command.add("-DCMAKE_EXPORT_COMPILE_COMMANDS=ON"); //$NON-NLS-1$
				command.add(new File(project.getLocationURI()).getAbsolutePath());

				StringBuffer cmd = new StringBuffer("");
				for (String c : command) 
					cmd.append(c).append(" ");
			
				launchAndWait("unix:///var/run/docker.sock","bavery/scott:ubuntu16.04","2345",cmd.toString(),project,buildDir.toString());

//				 ProcessBuilder processBuilder = new
//				 ProcessBuilder(command).directory(buildDir.toFile());
//				 setBuildEnvironment(processBuilder.environment());
//				 Process process = processBuilder.start();
//				 outStream.write(String.join(" ", command) + '\n');
//				 //$NON-NLS-1$
//				 watchProcess(process, new IConsoleParser[0], console);
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
				String[] command = buildCommand.split(" "); //$NON-NLS-1$

				Path cmdPath = findCommand(command[0]);
				if (cmdPath != null) {
					command[0] = cmdPath.toString();
				}

				StringBuffer cmd = new StringBuffer("");
				for (String c : command) 
					cmd.append(c).append(" ");

				launchAndWait("unix:///var/run/docker.sock","bavery/scott:ubuntu16.04","2345",cmd.toString(),project,buildDir.toString());
				
//				 ProcessBuilder processBuilder = new
//				 ProcessBuilder(command).directory(buildDir.toFile());
//				 setBuildEnvironment(processBuilder.environment());
//				 Process process = processBuilder.start();
//				 outStream.write(String.join(" ", command) + '\n');
//				 //$NON-NLS-1$
//				 watchProcess(process, new IConsoleParser[] { epm }, console);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// Load compile_commands.json file
			processCompileCommandsFile(monitor);

			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator.errorStatus(String.format("Building %s", project.getName()), e));
		}
	}

	private void launchAndWait(String connectionUri, String image, String dockerPort, String cmd, IProject project, String buildDir) {
		StartCMakeServerJob job = new StartCMakeServerJob(
				"Docker CMake server start");
		job.schedule();

		launcher.launch(Activator.PLUGIN_ID, job, connectionUri, image,
				cmd.toString(), null, buildDir, Arrays.asList(new String[] { new File(project.getLocationURI()).getAbsolutePath() }), null, null,
				Arrays.asList(new String[] { dockerPort }), true, true, true, new HashMap<String, String>());

		try {
			job.join();
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	private void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		Path commandsFile = getBuildDirectory().resolve("compile_commands.json"); //$NON-NLS-1$
		if (Files.exists(commandsFile)) {
			monitor.setTaskName("Processing compile_commands.json");
			try (FileReader reader = new FileReader(commandsFile.toFile())) {
				Gson gson = new Gson();
				CompileCommand[] commands = gson.fromJson(reader, CompileCommand[].class);
				for (CompileCommand command : commands) {
					processLine(command.getCommand());
				}
				shutdown();
			} catch (IOException e) {
				throw new CoreException(
						Activator.errorStatus(String.format("Processing compile commands %s", project.getName()), e));
			}
		}
	}

}
