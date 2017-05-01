/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.eclipse.tools.templates.freemarker.TemplateManifest;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;
import org.yocto.crops.internal.sdk.core.Activator;
import org.yocto.crops.internal.sdk.core.YoctoProjectNature;

public class YoctoProjectGenerator extends FMProjectGenerator implements IGenerator {

	public YoctoProjectGenerator(String manifestFile) {
		super(manifestFile);
	}

	public Bundle getSourceBundle() {
		return Activator.getDefault().getBundle();
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, YoctoProjectNature.ID });
		ICommand command = description.newCommand();
		CBuilder.setupBuilder(command);
		description.setBuildSpec(new ICommand[] { command });
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		super.generate(model, monitor);

		List<IPathEntry> entries = new ArrayList<>();
		IProject project = getProject();

		// Create the source folders
		TemplateManifest manifest = getManifest();
		if (manifest != null) {
			List<SourceRoot> srcRoots = getManifest().getSrcRoots();
			if (srcRoots != null && !srcRoots.isEmpty()) {
				for (SourceRoot srcRoot : srcRoots) {
					IFolder sourceFolder = project.getFolder(srcRoot.getDir());
					if (!sourceFolder.exists()) {
						sourceFolder.create(true, true, monitor);
					}

					entries.add(CoreModel.newSourceEntry(sourceFolder.getFullPath()));
				}
			} else {
				entries.add(CoreModel.newSourceEntry(getProject().getFullPath()));
			}
		}
		// XXX these entries should be unique to yocto projects
		entries.add(CoreModel.newOutputEntry(getProject().getFolder("build").getFullPath(), //$NON-NLS-1$
				new IPath[] { new Path("**/CMakeFiles/**") })); //$NON-NLS-1$
		CoreModel.getDefault().create(project).setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
				monitor);

		// Add the default preferences XXX
		// We will load default project preferences from instance preferences
		ScopedPreferenceStore scopedPreferenceStore = Activator.getDefault().getDefaultPreferences();

		String defaultURIPrefix = scopedPreferenceStore.getString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		if (defaultURIPrefix == null)
			defaultURIPrefix = scopedPreferenceStore.getDefaultString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		
		String defaultImageFilter = scopedPreferenceStore.getString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);
		if (defaultImageFilter == null)
			defaultImageFilter = scopedPreferenceStore.getDefaultString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);

		String defaultPort = scopedPreferenceStore.getString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);
		if (defaultPort == null)
			defaultPort = scopedPreferenceStore.getDefaultString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);

		YoctoProjectPreferences yoctoPref = new YoctoProjectPreferences(getProject(), defaultURIPrefix,
				defaultImageFilter, defaultPort);
		try {
			yoctoPref.writePreferences();
		} catch (BackingStoreException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not write project preferences"));
		}
	}

}
