package org.yocto.crops.zephyr;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	public static String WizardPage_browse;
	public static String WizardPage_description;
	public static String WizardPage_name;
	public static String WizardPage_path;
	//public static String WizardPage_prefix;
	public static String WizardPage_title;
	
	public static String WizardPage_board;
	public static String WizardPage_arch;
	public static String WizardPage_zephyr_install_dir;
	public static String WizardPage_zephyr_gcc_variant;
	public static String WizardPage_zephyr_base;

	public static String ZephyrConsole_0;
	
	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
 

}
