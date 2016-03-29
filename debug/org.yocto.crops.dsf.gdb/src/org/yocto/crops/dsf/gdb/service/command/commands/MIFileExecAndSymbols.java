/*******************************************************************************
 * Copyright (c) 2016 Intel Corporation and others.
 * 
 * Copyright (c) 2008, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Intel Corporation - CROPS implementation
 *******************************************************************************/
package org.yocto.crops.dsf.gdb.service.command.commands;

import java.io.File;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * -file-exec-and-symbols [FILE]
 * 
 * Specify the executable file to be debugged. This file is the one from which
 * the symbol table is also read. If no file is specified, the command clears
 * the executable and symbol information. If breakpoints are set when using this
 * command with no arguments, gdb will produce error messages. Otherwise, no
 * output is produced, except a completion notification.
 */
public class MIFileExecAndSymbols extends MICommand<MIInfo> {

	/** @since 4.0 */
    public MIFileExecAndSymbols(IMIContainerDMContext dmc) {
    	this(dmc, null);
    }

    /** @since 4.0 */
    public MIFileExecAndSymbols(IMIContainerDMContext dmc, String file) {
    	/* The file will be valid on the host
    	 * but we need to translate it to be valid in the container
    	 */
        super(dmc, "-file-exec-and-symbols", null, file == null ? null : new String[] {toContainerFile(file)}); //$NON-NLS-1$
    }
    
	private static String toContainerFile(String hostFile) {
		// First verify we are dealing with a proper project.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final String workspaceName = workspace.getRoot().getName();
		// Now verify we know the program to debug.
		// We can verify the Host local path
		// but then we must translate that into the docker container path
		IFile file = (IFile) new File(hostFile);
		IPath path = file.getFullPath();
		String[] segments = path.segments();
		boolean foundworkspace = false;
		/* gdb wants the full path to the executable/application/binary
		 * 
		 * Assume hostPath is <some_path>/<workspace_loc>/<project>/<config>/<executable>
		 * Assume containerPath is /crops/<project>/<config>/<executable>
		 * TODO: add checks to validate both hostFile and containerFile
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
}
