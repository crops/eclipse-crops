package org.yocto.crops.zephyr.templateengine.processes;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.yocto.crops.zephyr.templateengine.processes.Messages;

public class Messages {
	
	private static final ResourceBundle RESOURCE_BUNDLE =
			ResourceBundle.getBundle(Messages.class.getName());

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
