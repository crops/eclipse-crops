package org.yocto.crops.sdk.core.model;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.yocto.crops.internal.sdk.core.Activator;

public abstract class YoctoProjectGenerator extends FMProjectGenerator implements IGenerator {

	public YoctoProjectGenerator(String string) {
		super(string);
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		
		super.generate(model, monitor);
		
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
