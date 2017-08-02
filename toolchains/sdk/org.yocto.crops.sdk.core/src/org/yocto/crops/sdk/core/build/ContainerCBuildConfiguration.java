/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.yocto.crops.sdk.core.build;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.ui.launch.IContainerLaunchListener;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.osgi.framework.InvalidSyntaxException;
import org.yocto.crops.internal.sdk.core.Activator;
import org.yocto.crops.sdk.core.docker.IYoctoDockerConnectionManager;
import org.yocto.crops.sdk.core.model.YoctoProjectPreferences;

import com.google.gson.Gson;

public abstract class ContainerCBuildConfiguration extends CBuildConfiguration {

	private ContainerLauncher launcher;

	private class ContainerJob extends Job implements IContainerLaunchListener {

		private boolean started;
		private boolean done;
		private IDockerContainerInfo info;

		public ContainerJob(String name) {
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
			System.out.println("Docker Output: " + output);
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

	public ContainerCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
		launcher = new ContainerLauncher();
	}

	public ContainerCBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain, String launchMode) {
		super(config, name, toolChain, launchMode);
		launcher = new ContainerLauncher();
	}
	
	@SuppressWarnings("unchecked")
	private List<IDockerImage> getDockerImages(String connectionUriStartsWith, String imageFilter) {
		// XXX this should be different...i.e. so that the connection is looked
		// up
		// given some preference
		IYoctoDockerConnectionManager dcm = Activator.getService(IYoctoDockerConnectionManager.class);
		List<IDockerConnection> conns = dcm.getConnections();
		// Take the first one until we have some way to select via prefs
		if (conns.isEmpty())
			return null;
		else {
			try {
				IDockerConnection conn = null;
				for (IDockerConnection c : conns)
					if (c.getUri().startsWith(connectionUriStartsWith))
						conn = c;
				if (conn == null)
					return (List<IDockerImage>) Collections.EMPTY_LIST;
				return dcm.getImagesForConnection(conn, Activator.getDefault().createFilter(imageFilter));
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private IDockerImage selectDockerImage(List<IDockerImage> images) {
		if (images.isEmpty())
			return null;
		return images.get(0);
	}

	private IDockerImage getDockerImage(String connectionUriStartsWith, String imageFilter) {
		return selectDockerImage(getDockerImages(connectionUriStartsWith, imageFilter));
	}

	protected void launchAndWait(String cmd, IProject project, String buildDir) throws IOException {

		YoctoProjectPreferences pref = new YoctoProjectPreferences(getProject());
		pref.readPreferences();
		String dockerConnectionUrlPrefix = pref.getConnectionCriteria();
		String dockerImageFilter = pref.getImageFilter();
		String dockerPort = pref.getContainerPort();

		IDockerImage image = getDockerImage(dockerConnectionUrlPrefix, dockerImageFilter);
		if (image == null)
			throw new IOException("Could not build because image not found");

		ContainerJob job = new ContainerJob("Container job");
		job.schedule();

		launcher.launch(Activator.PLUGIN_ID, job, image.getConnection().getUri(), image.repoTags().get(0),
				cmd.toString(), null, buildDir,
				Arrays.asList(new String[] { new File(project.getLocationURI()).getAbsolutePath() }), null, null,
				Arrays.asList(new String[] { dockerPort }), true, false, true, new HashMap<String, String>());

		try {
			job.join();
		} catch (InterruptedException e) {
			// ignore
		}
	}

	protected void launchAndWait(List<String> command, IPath dir, IConsole console, IProgressMonitor monitor) throws CoreException {
		
		ArrayList<String> pokyEntryCmd = new ArrayList<String>();
		pokyEntryCmd.add("--workdir");
		pokyEntryCmd.add(dir.toPortableString());
		pokyEntryCmd.add("--id");
		pokyEntryCmd.add("1000:1000");
		pokyEntryCmd.add("--cmd");
		pokyEntryCmd.add(String.join(" ", command));
		
		ConsoleOutputStream outStream = console.getOutputStream();
		
		try {

			
			Process process = launcher.runCommand(
					"unix:///var/run/docker.sock", 
					"bavery/scott:cross", 
					getProject(), 
					new IErrorMessageHolder() {
						
						@Override
						public void setErrorMessage(String error) {
							// TODO Auto-generated method stub
							
						}
					}, 
					pokyEntryCmd, 
					getProject().getLocation().toPortableString(), 
					getProject().getLocation().toPortableString(), 
					new ArrayList<String>(), 
					new HashMap<String, String>(), 
					new Properties(), 
					false, 
					false, 
					new HashMap<String, String>(), 
					false);
			
			outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
			watchProcess(process, new IConsoleParser[0], console);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CoreException(Activator.errorStatus("FIXME", e));
		}		
	}

	protected void processCompileCommandsFile(IProgressMonitor monitor) throws CoreException {
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
