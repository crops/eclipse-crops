package org.yocto.crops.sdk.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.yocto.crops.internal.sdk.ui.Activator;
import org.yocto.crops.sdk.core.model.IYoctoInstancePreferences;

/**
 * Class used to initialize default preference values.
 */
public class YoctoWorkspaceInstancePreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String val = store.getDefaultString(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME);
		if (val == null)
			store.setDefault(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME, IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_DEFAULT);
		val = store.getDefaultString(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME);
		if (val == null)
			store.setDefault(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME, IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_DEFAULT);
		val = store.getDefaultString(IYoctoInstancePreferences.INSTPREFS_PORT_NAME);
		if (val == null)
			store.setDefault(IYoctoInstancePreferences.INSTPREFS_PORT_NAME, IYoctoInstancePreferences.INSTPREFS_PORT_DEFAULT);
	}

}
