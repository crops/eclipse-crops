/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.build;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerLoggingStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.ui.launch.IContainerLaunchListener;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
import org.eclipse.linuxtools.internal.docker.ui.consoles.ConsoleOutputStream;
import org.eclipse.linuxtools.internal.docker.ui.consoles.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.launch.ContainerCommandProcess;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DataVolumeModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.yocto.crops.internal.sdk.core.Activator;

public class ContainerLauncher {

	private static final String ERROR_CREATING_CONTAINER = "Error creating container from image<{0}>"; //$NON-NLS-1$
	private static final String ERROR_LAUNCHING_CONTAINER = "Error launching container"; //$NON-NLS-1$
	private static final String ERROR_NO_CONNECTIONS = "There are no open connections to a Docker daemon.  Open one from the Docker Explorer View."; //$NON-NLS-1$
	private static final String ERROR_NO_CONNECTION_WITH_URI = "There is no open connection with specified URI: {0}.  Use the Docker Explorer View to open it."; //$NON-NLS-1$

	private static final String YOCTO_WORKDIR_PARAM = "--workdir";
	private static final String YOCTO_CMD_PARAM = "--cmd";

	private static RunConsole console;
	
	private class ID {
		private Integer uid;
		private Integer gid;

		public ID(Integer uid, Integer gid) {
			this.uid = uid;
			this.gid = gid;
		}

		public Integer getuid() {
			return uid;
		}

		public Integer getgid() {
			return gid;
		}
	}
	
	private static Map<IProject, ID> fidMap = new HashMap<>();
	
	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}

	public static String getString(String key) {
		try {
			return key;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return '#' + key + '#';
		}
	}

	private class CopyVolumesJob extends Job {

		private static final String COPY_VOLUMES_JOB_TITLE = "Copying Volumes"; //$NON-NLS-1$
		private static final String COPY_VOLUMES_DESC = "Copying volumes from host to <{0}>"; //$NON-NLS-1$
		private static final String COPY_VOLUMES_TASK = "Copying [{0}]"; //$NON-NLS-1$
		private static final String ERROR_COPYING_VOLUME = "Error copying volume [{0}] to <{1}>"; //$NON-NLS-1$

		private final Set<String> volumes;
		private final IDockerConnection connection;
		private final String containerId;

		public CopyVolumesJob(Set<String> volumes, IDockerConnection connection, String containerId) {
			super(getString(COPY_VOLUMES_JOB_TITLE));
			this.volumes = volumes;
			this.connection = connection;
			this.containerId = containerId;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(getFormattedString(COPY_VOLUMES_DESC, containerId), volumes.size());
			Iterator<String> iterator = volumes.iterator();
			IStatus status = Status.OK_STATUS;
			// for each remote volume, copy from host to Container volume
			while (iterator.hasNext()) {
				if (monitor.isCanceled()) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				String directory = iterator.next();
				if (!directory.endsWith("/")) { //$NON-NLS-1$
					directory = directory + "/"; //$NON-NLS-1$
				}
				monitor.setTaskName(getFormattedString(COPY_VOLUMES_TASK, directory));
				try {
					((DockerConnection) connection).copyToContainer(directory, containerId, directory);
					monitor.worked(1);
				} catch (DockerException | InterruptedException | IOException e) {
					monitor.done();
					final String dir = directory;
					Display.getDefault()
							.syncExec(() -> MessageDialog.openError(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), getFormattedString(
											ERROR_COPYING_VOLUME, new String[] { dir, containerId }),
									e.getMessage()));
					status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
				} finally {
					monitor.done();
				}
			}
			return status;
		}

	}

	/**
	 * Perform a launch of a command in a container.
	 * 
	 * @param id
	 *            - id of caller to use to distinguish console owner
	 * @param listener
	 *            - optional listener of the run console
	 * @param connectionUri
	 *            - the specified connection to use
	 * @param image
	 *            - the image to use
	 * @param command
	 *            - command to run
	 * @param commandDir
	 *            - directory command requires or null
	 * @param workingDir
	 *            - working directory or null
	 * @param additionalDirs
	 *            - additional directories to mount or null
	 * @param origEnv
	 *            - original environment if we are appending to our existing
	 *            environment
	 * @param envMap
	 *            - map of environment variable settings
	 * @param ports
	 *            - ports to expose
	 * @param keep
	 *            - keep container after running
	 * @param stdinSupport
	 *            - true if stdin support is required, false otherwise
	 */
	public void launch(String id, IContainerLaunchListener listener, final String connectionUri, String image,
			String command, String commandDir, String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap, List<String> ports, boolean keep,
			boolean stdinSupport) {
		launch(id, listener, connectionUri, image, command, commandDir, workingDir, additionalDirs, origEnv, envMap,
				ports, keep, stdinSupport, false);
	}

	/**
	 * Perform a launch of a command in a container.
	 * 
	 * @param id
	 *            - id of caller to use to distinguish console owner
	 * @param listener
	 *            - optional listener of the run console
	 * @param connectionUri
	 *            - the specified connection to use
	 * @param image
	 *            - the image to use
	 * @param command
	 *            - command to run
	 * @param commandDir
	 *            - directory command requires or null
	 * @param workingDir
	 *            - working directory or null
	 * @param additionalDirs
	 *            - additional directories to mount or null
	 * @param origEnv
	 *            - original environment if we are appending to our existing
	 *            environment
	 * @param envMap
	 *            - map of environment variable settings
	 * @param ports
	 *            - ports to exposeGenerating
	 * @param keep
	 *            - keep container after running
	 * @param stdinSupport
	 *            - true if stdin support is required, false otherwise
	 * @param privilegedMode
	 *            - true if privileged mode is required, false otherwise
	 * @since 2.1
	 */
	public void launch(String id, IContainerLaunchListener listener, final String connectionUri, String image,
			String command, String commandDir, String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap, List<String> ports, boolean keep,
			boolean stdinSupport, boolean privilegedMode) {
		launch(id, listener, connectionUri, image, command, commandDir, workingDir, additionalDirs, origEnv, envMap,
				ports, keep, stdinSupport, privilegedMode, null);
	}

	/**
	 * Perform a launch of a command in a container.
	 * 
	 * @param id
	 *            - id of caller to use to distinguish console owner
	 * @param listener
	 *            - optional listener of the run console
	 * @param connectionUri
	 *            - the specified connection to use
	 * @param image
	 *            - the image to use
	 * @param command
	 *            - command to run
	 * @param commandDir
	 *            - directory command requires or null
	 * @param workingDir
	 *            - working directory or null
	 * @param additionalDirs
	 *            - additional directories to mount or null
	 * @param origEnv
	 *            - original environment if we are appending to our existing
	 *            environment
	 * @param envMap
	 *            - map of environment variable settings
	 * @param ports
	 *            - ports to expose
	 * @param keep
	 *            - keep container after running
	 * @param stdinSupport
	 *            - true if stdin support is required, false otherwise
	 * @param privilegedMode
	 *            - true if privileged mode is required, false otherwise
	 * @param labels
	 *            - Map of labels for the container
	 * @since 2.2
	 */
	public void launch(String id, IContainerLaunchListener listener, final String connectionUri, String image,
			String command, String commandDir, String workingDir, List<String> additionalDirs,
			Map<String, String> origEnv, Map<String, String> envMap, List<String> ports, boolean keep,
			boolean stdinSupport, boolean privilegedMode, Map<String, String> labels) {

		final String LAUNCH_TITLE = "ContainerLaunch.title"; //$NON-NLS-1$
		final String LAUNCH_EXITED_TITLE = "ContainerLaunchExited.title"; //$NON-NLS-1$

		final List<String> env = new ArrayList<>();
		env.addAll(toList(origEnv));
		env.addAll(toList(envMap));

		final List<String> cmdList = getYoctoCmd(command, workingDir);

		final Set<String> exposedPorts = new HashSet<>();
		final Map<String, List<IDockerPortBinding>> portBindingsMap = new HashMap<>();

		if (ports != null) {
			for (String port : ports) {
				port = port.trim();
				if (port.length() > 0) {
					String[] segments = port.split(":"); //$NON-NLS-1$
					if (segments.length == 1) { // containerPort
						exposedPorts.add(segments[0]);
						portBindingsMap.put(segments[0],
								Arrays.asList((IDockerPortBinding) new DockerPortBinding("", ""))); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (segments.length == 2) { // hostPort:containerPort
						exposedPorts.add(segments[1]);
						portBindingsMap.put(segments[1],
								Arrays.asList((IDockerPortBinding) new DockerPortBinding("", segments[0]))); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (segments.length == 3) { // either
						// ip:hostPort:containerPort
						// or ip::containerPort
						exposedPorts.add(segments[1]);
						if (segments[1].isEmpty()) {
							portBindingsMap.put(segments[2],
									Arrays.asList((IDockerPortBinding) new DockerPortBinding("", segments[0]))); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							portBindingsMap.put(segments[2], Arrays
									.asList((IDockerPortBinding) new DockerPortBinding(segments[0], segments[1]))); // $NON-NLS-1$
																													// //$NON-NLS-2$
						}
					}
				}
			}

		}

		// Note we only pass volumes to the config if we have a
		// remote daemon. Local mounted volumes are passed
		// via the HostConfig binds setting

		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder().openStdin(stdinSupport).cmd(cmdList)
				.image(image).workingDir(workingDir);
		// add any exposed ports as needed
		if (exposedPorts.size() > 0)
			builder = builder.exposedPorts(exposedPorts);

		// add any labels if specified
		if (labels != null)
			builder = builder.labels(labels);

		if (!DockerConnectionManager.getInstance().hasConnections()) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							getString(ERROR_LAUNCHING_CONTAINER), getString(ERROR_NO_CONNECTIONS)));
			return;
		}

		// Try and use the specified connection that was used before,
		// otherwise, open an error
		final IDockerConnection connection = DockerConnectionManager.getInstance().getConnectionByUri(connectionUri);
		if (connection == null) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							getString(ERROR_LAUNCHING_CONTAINER),
							getFormattedString(ERROR_NO_CONNECTION_WITH_URI, connectionUri)));
			return;
		}

		DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder().privileged(privilegedMode);

		final Set<String> remoteVolumes = new TreeSet<>();
		if (!((DockerConnection) connection).isLocal()) {
			// if using remote daemon, we have to
			// handle volume mounting differently.
			// Instead we mount empty volumes and copy
			// the host data over before starting.
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					remoteVolumes.add(dir); // $NON-NLS-1$
				}
			}
			if (workingDir != null)
				remoteVolumes.add(workingDir); // $NON-NLS-1$
			if (commandDir != null)
				remoteVolumes.add(commandDir); // $NON-NLS-1$
			builder = builder.volumes(remoteVolumes);
		} else {
			// Running daemon on local host.
			// Add mounts for any directories we need to run the executable.
			// When we add mount points, we need entries of the form:
			// hostname:mountname:Z.
			// In our case, we want all directories mounted as-is so the
			// executable will run as the user expects.
			// NOTE: At some point we may want to allow user to config using :z
			// rather than :Z
			// See
			// https://docs.docker.com/engine/reference/commandline/run/#mount-volumes-from-container---volumes-from
			final List<String> volumes = new ArrayList<>();
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					volumes.add(dir + ":" + dir + ":Z"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (workingDir != null) {
				volumes.add(workingDir + ":" + workingDir + ":Z"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (commandDir != null) {
				volumes.add(commandDir + ":" + commandDir + ":Z"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			hostBuilder = hostBuilder.binds(volumes);
		}

		final DockerContainerConfig config = builder.build();

		// add any port bindings if specified
		if (portBindingsMap.size() > 0)
			hostBuilder = hostBuilder.portBindings(portBindingsMap);

		final IDockerHostConfig hostConfig = hostBuilder.build();

		final String imageName = image;
		final boolean keepContainer = keep;
		final String consoleId = id;
		final IContainerLaunchListener containerListener = listener;

		Thread t = new Thread(() -> {
			// create the container
			String containerId = null;
			try {
				containerId = ((DockerConnection) connection).createContainer(config, hostConfig, null);
				if (!((DockerConnection) connection).isLocal()) {
					// if daemon is remote, we need to copy
					// data over from the host.
					if (!remoteVolumes.isEmpty()) {
						CopyVolumesJob job = new CopyVolumesJob(remoteVolumes, connection, containerId);
						job.schedule();
						job.join();
						if (job.getResult() != Status.OK_STATUS)
							return;
					}
				}
				OutputStream stream = null;
				RunConsole oldConsole = getConsole();
				final RunConsole rc = RunConsole.findConsole(containerId, consoleId);
				setConsole(rc);
				rc.clearConsole();
				if (oldConsole != null)
					RunConsole.removeConsole(oldConsole);
				Display.getDefault().syncExec(() -> rc
						.setTitle(getFormattedString(LAUNCH_TITLE, new String[] { cmdList.get(0), imageName })));
				// if (!rc.isAttached()) {
				rc.attachToConsole(connection, containerId);
				// }
				if (rc != null) {
					stream = rc.getOutputStream();
					if (containerListener != null) {
						((ConsoleOutputStream) stream).addConsoleListener(containerListener);
					}
				}
				// Create a unique logging thread id which has container id
				// and console id
				String loggingId = containerId + "." + consoleId;
				((DockerConnection) connection).startContainer(containerId, loggingId, stream);
				if (rc != null)
					rc.showConsole();
				if (containerListener != null) {
					IDockerContainerInfo info = ((DockerConnection) connection).getContainerInfo(containerId);
					containerListener.containerInfo(info);
				}

				// Wait for the container to finish
				final IDockerContainerExit status = ((DockerConnection) connection).waitForContainer(containerId);
				Display.getDefault().syncExec(() -> {
					rc.setTitle(getFormattedString(LAUNCH_EXITED_TITLE,
							new String[] { status.statusCode().toString(), cmdList.get(0), imageName }));
					rc.showConsole();
				});

				// Let any container listener know that the container is
				// finished
				if (containerListener != null)
					containerListener.done();

				if (!keepContainer) {
					// Drain the logging thread before we remove the
					// container (we need to use the logging id)
					((DockerConnection) connection).stopLoggingThread(loggingId);
					while (((DockerConnection) connection)
							.loggingStatus(loggingId) == EnumDockerLoggingStatus.LOGGING_ACTIVE) {
						Thread.sleep(1000);
					}
					// Look for any Display Log console that the user may
					// have opened which would be
					// separate and make sure it is removed as well
					RunConsole rc2 = RunConsole.findConsole(((DockerConnection) connection).getContainer(containerId));
					if (rc2 != null)
						RunConsole.removeConsole(rc2);
					((DockerConnection) connection).removeContainer(containerId);
				}

			} catch (final DockerException e2) {
				// error in creation, try and remove Container if possible
				if (!keepContainer && containerId != null) {
					try {
						((DockerConnection) connection).removeContainer(containerId);
					} catch (DockerException | InterruptedException e1) {
						// ignore exception
					}
				}
				Display.getDefault()
						.syncExec(() -> MessageDialog.openError(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								getFormattedString(ERROR_CREATING_CONTAINER, imageName), e2.getMessage()));
			} catch (InterruptedException e3) {
				// for now
				// do nothing
			}
			((DockerConnection) connection).getContainers(true);
		});
		t.start();
	}

	/**
	 * Clean up the container used for launching
	 * 
	 * @param connectionUri
	 *            the URI of the connection used
	 * @param info
	 *            the container info
	 */
	public void cleanup(String connectionUri, IDockerContainerInfo info) {
		if (!DockerConnectionManager.getInstance().hasConnections()) {
			return;
		}

		// Try and find the specified connection
		final IDockerConnection connection = DockerConnectionManager.getInstance().getConnectionByUri(connectionUri);
		if (connection == null) {
			return;
		}
		try {
			connection.killContainer(info.id());
		} catch (DockerException | InterruptedException e) {
			// do nothing
		}
	}

	/**
	 * Get the reusable run console for running C/C++ executables in containers.
	 * 
	 * @return
	 */
	private RunConsole getConsole() {
		// if (console == null) {
		// console = RunConsole.getContainerLessConsole();
		// }
		return console;
	}

	private void setConsole(RunConsole cons) {
		console = cons;
	}

	/**
	 * Take the command string and parse it into a list of strings.
	 * 
	 * @param s
	 * @param workingDir
	 * @return list of strings
	 */
	private List<String> getYoctoCmd(String s, String workingDir) {
		ArrayList<String> list = new ArrayList<>();
		list.add(YOCTO_WORKDIR_PARAM);
		list.add(workingDir);
		list.add(YOCTO_CMD_PARAM);
		list.add(s);
		return list;
	}

	/**
	 * Convert map of environment variables to a {@link List} of KEY=VALUE
	 * String
	 * 
	 * @param variables
	 *            the entries to manipulate
	 * @return the concatenated key/values for each given variable entry
	 */
	private List<String> toList(@SuppressWarnings("rawtypes") final Map variables) {
		final List<String> result = new ArrayList<>();
		if (variables != null) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Set<Entry> entries = variables.entrySet();
			for (@SuppressWarnings("rawtypes") Entry entry : entries) {
				final String key = (String) entry.getKey();
				final String value = (String) entry.getValue();

				final String envEntry = key + "=" + value; //$NON-NLS-1$
				result.add(envEntry);
			}
		}
		return result;

	}

	public Process runCommand(String connectionName, String imageName, IProject project, 
			IErrorMessageHolder errMsgHolder, List<String> cmdList,
			String commandDir,
			String workingDir,
			List<String> additionalDirs, Map<String, String> origEnv,
			Properties envMap, boolean supportStdin,
			boolean privilegedMode, HashMap<String, String> labels,
			boolean keepContainer) {

		Integer uid = null;
		Integer gid = null;
		// For Unix, make sure that the user id is passed with the run
		// so any output files are accessible by this end-user
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os.indexOf("nux") > 0) { //$NON-NLS-1$
			// first try and see if we have already run a command on this
			// project
			ID ugid = fidMap.get(project);
			if (ugid == null) {
				try {
					uid = (Integer) Files.getAttribute(
							project.getLocation().toFile().toPath(),
							"unix:uid"); //$NON-NLS-1$
					gid = (Integer) Files.getAttribute(
							project.getLocation().toFile().toPath(),
							"unix:gid"); //$NON-NLS-1$
					ugid = new ID(uid, gid);
					// store the uid for possible later usage
					fidMap.put(project, ugid);
				} catch (IOException e) {
					// do nothing...leave as null
				} // $NON-NLS-1$
			} else {
				uid = ugid.getuid();
				gid = ugid.getgid();
			}
		}

		final List<String> env = new ArrayList<>();
		env.addAll(toList(origEnv));
		env.addAll(toList(envMap));

//		final List<String> cmdList = getCmdList(command);

		final Map<String, List<IDockerPortBinding>> portBindingsMap = new HashMap<>();


		IDockerConnection[] connections = DockerConnectionManager
				.getInstance().getConnections();
		if (connections == null || connections.length == 0) {
			errMsgHolder.setErrorMessage(
					ContainerLauncherMessages.getString("ContainerLaunch.noConnections.error")); //$NON-NLS-1$
			return null;
		}

		IDockerConnection connection = null;
		for (IDockerConnection c : connections) {
			if (c.getUri().equals(connectionName)) {
				connection = c;
				break;
			}
		}

		if (connection == null) {
			errMsgHolder.setErrorMessage(ContainerLauncherMessages.getFormattedString(
					"ContainerLaunch.connectionNotFound.error", //$NON-NLS-1$
					connectionName));
			return null;
		}

		List<IDockerImage> images = connection.getImages();
		if (images.isEmpty()) {
			errMsgHolder.setErrorMessage(
					ContainerLauncherMessages.getString("ContainerLaunch.noImages.error")); //$NON-NLS-1$
			return null;
		}

		IDockerImageInfo info = connection.getImageInfo(imageName);
		if (info == null) {
			errMsgHolder.setErrorMessage(ContainerLauncherMessages.getFormattedString(
					"ContainerLaunch.imageNotFound.error", imageName)); //$NON-NLS-1$
			return null;
		}

		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder()
				.openStdin(supportStdin).cmd(cmdList).image(imageName)
				.workingDir(workingDir);

//		// switch to user id for Linux so output is accessible
//		if (uid != null) {
//			builder = builder.user(uid.toString());
//		}
		
		// TODO: add group id here when supported by DockerHostConfig.Builder

		// add any labels if specified
		if (labels != null)
			builder = builder.labels(labels);

		DockerHostConfig.Builder hostBuilder = new DockerHostConfig.Builder()
				.privileged(privilegedMode);

		// Note we only pass volumes to the config if we have a
		// remote daemon. Local mounted volumes are passed
		// via the HostConfig binds setting
		final Set<String> remoteVolumes = new TreeSet<>();
//		final Map<String, String> remoteDataVolumes = new HashMap<>();
		final Set<String> readOnlyVolumes = new TreeSet<>();
		if (!((DockerConnection) connection).isLocal()) {
			// if using remote daemon, we have to
			// handle volume mounting differently.
			// Instead we mount empty volumes and copy
			// the host data over before starting.
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					if (dir.contains(":")) { //$NON-NLS-1$
						DataVolumeModel dvm = DataVolumeModel.parseString(dir);
						switch (dvm.getMountType()) {
						case HOST_FILE_SYSTEM:
							dir = dvm.getHostPathMount();
//							remoteDataVolumes.put(dir, dvm.getContainerMount());
							// keep track of read-only volumes so we don't copy
							// these
							// back after command completion
							if (dvm.isReadOnly()) {
								readOnlyVolumes.add(dir);
							}
							break;
						default:
							continue;
						}
					}
					IPath p = new Path(dir).removeTrailingSeparator();
					remoteVolumes.add(p.toPortableString());
				}
			}
			if (workingDir != null) {
				IPath p = new Path(workingDir).removeTrailingSeparator();
				remoteVolumes.add(p.toPortableString());
			}
			if (commandDir != null) {
				IPath p = new Path(commandDir).removeTrailingSeparator();
				remoteVolumes.add(p.toPortableString());
			}
			builder = builder.volumes(remoteVolumes);
		} else {
			// Running daemon on local host.
			// Add mounts for any directories we need to run the executable.
			// When we add mount points, we need entries of the form:
			// hostname:mountname:Z.
			// In our case, we want all directories mounted as-is so the
			// executable will run as the user expects.
			final Set<String> volumes = new TreeSet<>();
			final List<String> volumesFrom = new ArrayList<>();
			if (additionalDirs != null) {
				for (String dir : additionalDirs) {
					IPath p = new Path(dir).removeTrailingSeparator();
					if (dir.contains(":")) { //$NON-NLS-1$
						DataVolumeModel dvm = DataVolumeModel.parseString(dir);
						switch (dvm.getMountType()) {
						case HOST_FILE_SYSTEM:
							String bind = LaunchConfigurationUtils
									.convertToUnixPath(dvm.getHostPathMount())
									+ ':' + dvm.getContainerPath() + ":Z"; //$NON-NLS-1$ //$NON-NLS-2$
							if (dvm.isReadOnly()) {
								bind += ",ro"; //$NON-NLS-1$
							}
							volumes.add(bind);
							break;
						case CONTAINER:
							volumesFrom.add(dvm.getContainerMount());
							break;
						default:
							break;

						}
					} else {
						volumes.add(p.toPortableString() + ":" //$NON-NLS-1$
								+ p.toPortableString() + ":Z"); //$NON-NLS-1$
					}
				}
			}
			if (workingDir != null) {
				IPath p = new Path(workingDir).removeTrailingSeparator();
				volumes.add(p.toPortableString() + ":" + p.toPortableString() //$NON-NLS-1$
						+ ":Z"); //$NON-NLS-1$
			}
			if (commandDir != null) {
				IPath p = new Path(commandDir).removeTrailingSeparator();
				volumes.add(p.toPortableString() + ":" + p.toPortableString() //$NON-NLS-1$
						+ ":Z"); //$NON-NLS-1$
			}
			List<String> volumeList = new ArrayList<>(volumes);
			hostBuilder = hostBuilder.binds(volumeList);
			if (!volumesFrom.isEmpty()) {
				hostBuilder = hostBuilder.volumesFrom(volumesFrom);
			}
		}

		final DockerContainerConfig config = builder.build();

		// add any port bindings if specified
		if (portBindingsMap.size() > 0)
			hostBuilder = hostBuilder.portBindings(portBindingsMap);

		final IDockerHostConfig hostConfig = hostBuilder.build();

		// create the container
		String containerId = null;
		try {
			containerId = ((DockerConnection) connection)
					.createContainer(config, hostConfig, null);
		} catch (DockerException | InterruptedException e) {
			errMsgHolder.setErrorMessage(e.getMessage());
			return null;
		}
		
		final String id = containerId;
		final IDockerConnection conn = connection;

		try {
			((DockerConnection) conn).startContainer(id, null);
		} catch (DockerException | InterruptedException e) {
			Activator.log(e);
		}

		return new ContainerCommandProcess(connection, imageName, containerId,
				remoteVolumes,
				keepContainer);
	}
}
