/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.yocto.crops.internal.docker.launcher;

import org.yocto.crops.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.debug.core.DebugPlugin;

public interface ILaunchConstants {

	public final static String LAUNCH_ID = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".launchConfigurationType"; //$NON-NLS-1$

	public final static String ATTR_CROPS_DIRECTORY = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".crops_directory";  //$NON-NLS-1$

	public final static String ATTR_CROPS_DIRECTORY_DEFAULT = "/crops"; //$NON-NLS-1$
	
	public final static String ATTR_ADDITIONAL_DIRS = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".additional_dirs"; //$NON-NLS-1$

	public final static String ATTR_CONTAINER = DockerLaunchUIPlugin.getUniqueIdentifier()
			+ ".container"; //$NON-NLS-1$
	
	public final static String ATTR_IMAGE = DockerLaunchUIPlugin.getUniqueIdentifier()
			+ ".image"; //$NON-NLS-1$

	public final static String ATTR_CONNECTION_URI = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".connection_uri"; //$NON-NLS-1$

	public final static String ATTR_KEEP_AFTER_LAUNCH = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".keep_after_launch"; //$NON-NLS-1$

	public final static String ATTR_STDIN_SUPPORT = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".support_std_input"; //$NON-NLS-1$
	
	public final static String ATTR_SYSROOTS_PATH = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".sysroots_path"; //$NON-NLS-1$
	public final static String ATTR_USR_BIN_PATH = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".usr_bin_path"; //$NON-NLS-1$
	public final static String ATTR_ARCH_PREFIX = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".arch_prefix"; //$NON-NLS-1$
	public final static String ATTR_GDB_COMMAND = DockerLaunchUIPlugin
			.getUniqueIdentifier() + ".gdb_command"; //$NON-NLS-1$
	
	public static final String ATTR_SYSROOTS_PATH_DEFAULT = "/opt/zephyr-sdk/sysroots/i686-pokysdk-linux"; //$NON-NLS-1$
	public static final String ATTR_USR_BIN_PATH_DEFAULT = "/usr/bin"; //$NON-NLS-1$
	public static final String ATTR_ARCH_PREFIX_DEFAULT = "/arc-poky-elf"; //$NON-NLS-1$
	public static final String ATTR_GDB_COMMAND_DEFAULT = ATTR_ARCH_PREFIX_DEFAULT + "-gdb"; //$NON-NLS-1$
	
	// Attributes that need to match CDT attribute names
	public static final String ATTR_GDBSERVER_PORT = DebugPlugin
			.getUniqueIdentifier() + ".ATTR_GDBSERVER_PORT"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND = DebugPlugin
			.getUniqueIdentifier() + ".ATTR_GDBSERVER_COMMAND"; //$NON-NLS-1$

	public static final String ATTR_GDBSERVER_PORT_DEFAULT = "2345"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND_DEFAULT = "gdbserver"; //$NON-NLS-1$

}
