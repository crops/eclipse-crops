package org.yocto.crops.zephyr.preferences;

import java.lang.reflect.Array;
import java.util.Set;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.core.Messages;
//import org.yocto.crops.zephyr.model.Toolchain;
//import org.yocto.crops.zephyr.model.Toolchains;
import org.yocto.crops.zephyr.ZephyrPlugin;

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

public class ZephyrPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public ZephyrPreferencePage() {
		super(GRID);
		setPreferenceStore(ZephyrPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CropsPreferencePage_description);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		/* TODO: figure out the generic CROPS way to expand "toolchains" from JSON */
		String[] toolchainNames = {"crops-zephyr-0-7-2-src"}; //CropsUtils.getToolchainContainers();
		String[][] toolchainNamesArray = new String[toolchainNames.length][2];
		int i = 0;
		for (String toolchainName : toolchainNames) {
			toolchainNamesArray[i][0] = toolchainName;
			toolchainNamesArray[i][1] = toolchainName;
		}
		addField(
			new ComboFieldEditor(PreferenceConstants.P_TOOLCHAIN_CONTAINER_ID,
				"Toolchain container I&d:",
				toolchainNamesArray,
				getFieldEditorParent()));
		/* TODO: figure out the generic CROPS way to expand "boards" from JSON */
//		Set<String> boardNames = Toolchain.getBoardNames();
//		String[][] boardNamesArray = new String[boardNames.size()][2];
		String[][] boardNames = new String[][]{
								{"arduino_101","arduino_101"},
								{"arduino_101_sss","arduino_101_sss"}};
		String[][] archNames = new String[][] {
								{"arc","arc"},
								{"arm","arm"},
								{"x86","x86"}};
//		int i = 0;
//		for(String boardName : boardNames) {
//			boardNamesArray[i][0] = boardName;
//			boardNamesArray[i][1] = boardName;
//			i++;
//		}
		addField(
			new ComboFieldEditor(PreferenceConstants.P_ZEPHYR_BOARD,
								 "Zephyr &Board:",
								 boardNames,
								 getFieldEditorParent()));
		addField(
			new ComboFieldEditor(PreferenceConstants.P_ZEPHYR_ARCH,
								 "Zephyr &Arch:",
								 archNames,
								 getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_ZEPHYR_GCC_VARIANT,
								  "Zephyr &GCC Variant:",
								  getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_ZEPHYR_INSTALL_DIR,
								  "&Zephyr SDK Install Directory:",
								  getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_ZEPHYR_BASE,
								  "Zephyr B&ase Directory:",
								  getFieldEditorParent()));
//		addField(
//			new BooleanFieldEditor(PreferenceConstants.P_EXPERT_MODE,
//				                   "&Expert mode by default",
//				                   getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}