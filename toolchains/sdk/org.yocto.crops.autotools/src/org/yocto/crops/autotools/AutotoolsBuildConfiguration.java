/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.yocto.crops.autotools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.yocto.crops.sdk.core.build.ContainerCBuildConfiguration;

public class AutotoolsBuildConfiguration extends ContainerCBuildConfiguration {

	public static final String AUTOTOOLS_GENERATOR = "autotools.generator"; //$NON-NLS-1$
	public static final String AUTOTOOLS_ARGUMENTS = "autotools.arguments"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "autotools.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "autotools.command.clean"; //$NON-NLS-1$

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public AutotoolsBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		super(config, name, toolChain, "run"); // TODO: why "run"
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		
		IProject project = getProject();
		
		execute(Arrays.asList(new String[] { "autoreconf", "--install" }), project.getLocation(), console, monitor);
		execute(Arrays.asList(new String[] { "./configure" }), project.getLocation(), console, monitor);
		execute(Arrays.asList(new String[] { "make" }), project.getLocation(), console, monitor);
		
		return new IProject[] { project };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		execute(Arrays.asList(new String[] { "make", "clean" }), getProject().getLocation(), console, monitor);
	}

	protected void execute(List<String> command, IPath dir, IConsole console, IProgressMonitor monitor) throws CoreException {
//		try {
//			launchAndWait(String.join(" ", command), getProject(), dir.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw new CoreException(Activator.errorStatus("FIXME", e));
//		}
		
		launchAndWait(command, dir, console, monitor);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

	}

}
