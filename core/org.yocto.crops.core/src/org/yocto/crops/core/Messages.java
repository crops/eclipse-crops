package org.yocto.crops.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String CropsUtils_platform_not_supported;
	public static String CropsPreferencePage_description;
	public static String CropsPreferencePage_ceed_label;
	public static String CropsPreferencePage_crops_root_label;
	
	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}

}
