/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.internal.sdk.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.yocto.crops.sdk.core.docker.IYoctoDockerConnectionManager;
import org.yocto.crops.sdk.core.model.IYoctoInstancePreferences;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.yocto.crops.sdk.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private List<IDockerConnection> connections;

	private ServiceRegistration<IYoctoDockerConnectionManager> reg;

	private ScopedPreferenceStore defaultPreferenceStore;
	
	public Filter createFilter(String filter) throws InvalidSyntaxException {
		return plugin.getBundle().getBundleContext().createFilter(filter);
	}

	class YoctoDockerConnectionManager implements IYoctoDockerConnectionManager {
		@Override
		public List<IDockerConnection> getConnections() {
			synchronized (connections) {
				return new ArrayList<IDockerConnection>(connections);
			}
		}

		@Override
		public List<IDockerImage> getImagesForConnection(IDockerConnection connection, Filter repoTagsFilter) {
			List<IDockerImage> images = connection.getImages(true);
			List<IDockerImage> results = new ArrayList<IDockerImage>();
			if (repoTagsFilter != null) {
				Map<String, Object> imageProps = new HashMap<String, Object>();
				for (IDockerImage image : images) {
					imageProps.clear();
					java.util.List<String> tags = image.repoTags();
					if (tags != null) {
						for (String tag : tags) {
							if (!tag.equals("<none>:<none>")) {
								String[] splitStr = tag.split(":");
								if (splitStr.length == 2) {
									imageProps.put(splitStr[0], splitStr[1]);
								}
							}
						}
					}
					imageProps.put("id", image.id());
					imageProps.put("created", image.created());
					imageProps.put("createdDate", image.createdDate());
					imageProps.put("parentId", image.parentId());
					imageProps.put("repoTags", image.repoTags());
					imageProps.put("repo", image.repo());
					imageProps.put("tags", image.tags());
					imageProps.put("size", image.size());
					imageProps.put("virtualSize", image.virtualSize());
					imageProps.put("isIntermediateImage", image.isIntermediateImage());
					imageProps.put("isDangling", image.isDangling());
					if (repoTagsFilter.matches(imageProps))
						results.add(image);
				}
			} else
				if (images != null)
					results.addAll(images);
			return results;
		}

	}

	private IDockerConnectionManagerListener listener = new IDockerConnectionManagerListener() {
		@Override
		public void changeEvent(IDockerConnection connection, int type) {
			synchronized (connections) {
				switch (type) {
				case ADD_EVENT:
					connections.add(connection);
					break;
				case REMOVE_EVENT:
					connections.remove(connection);
				}
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		DockerConnectionManager dcm = DockerConnectionManager.getInstance();
		connections = dcm.getAllConnections();
		// Then setup our listener
		dcm.addConnectionManagerListener(listener);

		YoctoDockerConnectionManager cm = new YoctoDockerConnectionManager();
		synchronized (connections) {
			for (IDockerConnection c : connections)
				cm.getImagesForConnection(c, null);
		}
		// register yocto connection manager service
		reg = context.registerService(IYoctoDockerConnectionManager.class, cm, null);

		// setup default preferences at workspace instance scope if they are not already set
		defaultPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,context.getBundle().getSymbolicName());
		String val = defaultPreferenceStore.getDefaultString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		if (val == null || "".equals(val)) 
			defaultPreferenceStore.setDefault(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME, IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_DEFAULT);
		val = defaultPreferenceStore.getString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		if (val == null || "".equals(val))
			defaultPreferenceStore.setValue(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME, IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_DEFAULT);
		
		val = defaultPreferenceStore.getDefaultString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);
		if (val == null || "".equals(val))
			defaultPreferenceStore.setDefault(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME, IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_DEFAULT);
		val = defaultPreferenceStore.getString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);
		if (val == null || "".equals(val))
			defaultPreferenceStore.setValue(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME, IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_DEFAULT);
		
		val = defaultPreferenceStore.getDefaultString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);
		if (val == null || "".equals(val))
			defaultPreferenceStore.setDefault(IYoctoInstancePreferences.INSTPREFS_PORT_NAME, IYoctoInstancePreferences.INSTPREFS_PORT_DEFAULT);
		val = defaultPreferenceStore.getString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);
		if (val == null || "".equals(val))
			defaultPreferenceStore.setValue(IYoctoInstancePreferences.INSTPREFS_PORT_NAME, IYoctoInstancePreferences.INSTPREFS_PORT_DEFAULT);
	
	}

	public ScopedPreferenceStore getDefaultPreferences() {
		return defaultPreferenceStore;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		DockerConnectionManager dcm = DockerConnectionManager.getInstance();
		if (dcm != null) {
			dcm.removeConnectionManagerListener(listener);
		}
		if (reg != null) {
			reg.unregister();
			reg = null;
		}
		plugin = null;
	}
	
	

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = getDefault().getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(errorStatus(e.getLocalizedMessage(), e));
		}
	}

	public static void error(String message, Throwable cause) {
		plugin.getLog().log(errorStatus(message, cause));
	}

	public static IStatus errorStatus(String message, Throwable cause) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message, cause);
	}

}
