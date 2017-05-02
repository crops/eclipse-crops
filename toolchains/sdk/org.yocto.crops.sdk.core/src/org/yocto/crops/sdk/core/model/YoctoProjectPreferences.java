/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.yocto.crops.internal.sdk.core.Activator;

public class YoctoProjectPreferences {

	public static final String URI_STARTSWITH_CRITERIA_NODE_NAME = "uriStartswithCriteria";
	public static final String IMAGE_FILTER_NODE_NAME = "imageFilter";
	public static final String CONTAINER_PORT_NODE_NAME = "containerPort";

	private final ProjectScope projectScope;
	private String uriStartswithCriteria;
	private String imageFilter;
	private String containerPort;

	public YoctoProjectPreferences(IProject project) {
		this.projectScope = new ProjectScope(project);
	}

	public YoctoProjectPreferences(IProject project, String uriStartswithCriteria, String imageFilter,
			String containerPort) {
		this(project);
		this.uriStartswithCriteria = uriStartswithCriteria;
		this.imageFilter = imageFilter;
		this.containerPort = containerPort;
	}

	public String getConnectionCriteria() {
		return uriStartswithCriteria;
	}

	public String getImageFilter() {
		return imageFilter;
	}

	public String getContainerPort() {
		return containerPort;
	}

	IEclipsePreferences getProjectPreferences() {
		return this.projectScope.getNode(Activator.PLUGIN_ID);
	}

	public void readPreferences() {
		IEclipsePreferences prefs = getProjectPreferences();
		this.uriStartswithCriteria = prefs.get(URI_STARTSWITH_CRITERIA_NODE_NAME, null);
		this.imageFilter = prefs.get(IMAGE_FILTER_NODE_NAME, null);
		this.containerPort = prefs.get(CONTAINER_PORT_NODE_NAME, null);
	}

	public void writePreferences() throws BackingStoreException {
		IEclipsePreferences prefs = getProjectPreferences();
		prefs.put(URI_STARTSWITH_CRITERIA_NODE_NAME, this.uriStartswithCriteria);
		prefs.put(IMAGE_FILTER_NODE_NAME, this.imageFilter);
		prefs.put(CONTAINER_PORT_NODE_NAME, this.containerPort);
		prefs.flush();
	}
	
	public static IPreferenceStore getDefaultPreferenceStore() {
		return Activator.getDefault().getDefaultPreferences();
	}
}
