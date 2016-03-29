/*******************************************************************************
 * Copyright (c) 2016 Intel Corporation
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - CROPS implementation
 *******************************************************************************/
package org.yocto.crops.dsf.gdb.service.command.commands;

import java.io.File;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 *      -environment-cd PATHDIR
 *
 *   Set GDB's working directory.
 * 
 * override DSF default, we need to receive 
 * a user browse-able path to application/binary/executable
 * and translate that path to one inside the container
 * 
 */
public class MIEnvironmentCD extends MICommand <MIInfo> 
{
	/* translate the native directory which the user browsed for 
	 * into one appropriate for the container */
	/* ideally add a call into container to test if file exists */
	/* for now test for ${workspace_loc} strip and replace with /crops
	 * and then append the remaining segments
	 */
	public MIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		super(ctx, "-environment-cd", new String[]{toContainerPath(path)}); //$NON-NLS-1$
	}
	
	private static String toContainerPath(String hostPath) {
		// First verify we are dealing with a proper project.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final String workspaceName = workspace.getRoot().getName();
		// Now verify we know the program to debug.
		// We can verify the Host local path
		// but then we must translate that into the container path
		IPath path = new Path(hostPath);
		String[] segments = path.segments();
		boolean foundworkspace = false;
		/* Assume hostPath is <some_path>/<workspace_loc>/<project>
		 * Assume containerPath is /crops/<project>
		 * TODO: add checks to validate both hostPath and containerPath
		 */
		Path containerPath = new Path("/crops"); //$NON-NLS-1$
		for (String segment : segments) {
			if (segment.equals(workspaceName)) {
					foundworkspace = true;
					continue;
			}
			if (foundworkspace)
				containerPath = new Path(containerPath.toPortableString() + Path.SEPARATOR + segment);
		}
		return containerPath.toPortableString();
	}
	
	public String getWorkingDirectory(File cwd) {
		return "--cd=" + cwd.getAbsolutePath(); //$NON-NLS-1$
	}
	
	public String getContainerWorkingDirectory(File cwd) {
		return "--cd=" + cwd.getAbsolutePath(); //$NON-NLS-1$
	}
}
