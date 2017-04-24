package org.yocto.crops.internal.sdk.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yocto.crops.sdk.core.model.IYoctoInstancePreferences;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.yocto.crops.internal.sdk.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		initializeDefaultPluginPreferences();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		String val = store.getDefaultString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		if (val == null || "".equals(val)) 
			store.setDefault(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME, IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_DEFAULT);
		val = store.getString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		if (val == null || "".equals(val))
			store.setValue(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME, IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_DEFAULT);
		
		val = store.getDefaultString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);
		if (val == null || "".equals(val))
			store.setDefault(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME, IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_DEFAULT);
		val = store.getString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);
		if (val == null || "".equals(val))
			store.setValue(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME, IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_DEFAULT);
		
		val = store.getDefaultString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);
		if (val == null || "".equals(val))
			store.setDefault(IYoctoInstancePreferences.INSTPREFS_PORT_NAME, IYoctoInstancePreferences.INSTPREFS_PORT_DEFAULT);
		val = store.getString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);
		if (val == null || "".equals(val))
			store.setValue(IYoctoInstancePreferences.INSTPREFS_PORT_NAME, IYoctoInstancePreferences.INSTPREFS_PORT_DEFAULT);
	}
}
