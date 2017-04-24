package org.yocto.crops.sdk.ui.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.yocto.crops.internal.sdk.ui.Activator;
import org.yocto.crops.sdk.core.model.IYoctoInstancePreferences;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class YoctoWorkspaceInstancePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public YoctoWorkspaceInstancePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A demonstration of a preference page implementation");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(IYoctoInstancePreferences.INSTPREFS_URIPREFIX_NODE_NAME, "Connection Prefix"
				, getFieldEditorParent()));
		addField(new StringFieldEditor(IYoctoInstancePreferences.INSTPREFS_IMAGE_FILTER_NAME, "Tag Filter",
				getFieldEditorParent()));
		addField(new StringFieldEditor(IYoctoInstancePreferences.INSTPREFS_PORT_NAME, "Port", getFieldEditorParent()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setDescription("Yocto Docker Preferences");
	}
	
}