/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.internal.sdk.core;

import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class YoctoProjectNature implements IProjectNature {

	public static final String ID = Activator.PLUGIN_ID + ".yoctoProjectNature";
	
	private IProject project;

	public static void setupBuilder(IProjectDescription projDesc) throws CoreException {
		ICommand command = projDesc.newCommand();
		CBuilder.setupBuilder(command);
		projDesc.setBuildSpec(new ICommand[] { command });
	}
	
	@Override
	public void configure() throws CoreException {
		IProjectDescription projDesc = project.getDescription();
		setupBuilder(projDesc);
		project.setDescription(projDesc, new NullProgressMonitor());
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}


}
