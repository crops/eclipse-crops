/*******************************************************************************
 * Copyright (c) 2015, 2016, 2017 Red Hat Inc., Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *     Intel - Launch using CROPS containers
 *******************************************************************************/

package org.yocto.crops.sdk.core.build;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ContainerLauncherMessages {

	private static final String BUNDLE_NAME = ContainerLauncherMessages.class.getName();

	public static String getString(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return '#' + key + '#';
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), arg);
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), (Object[]) args);
	}
}
