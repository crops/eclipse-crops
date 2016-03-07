/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.yocto.crops.internal.docker.launcher.ui.preferences;

import org.yocto.crops.docker.launcher.DockerLaunchUIPlugin;
import org.yocto.crops.internal.docker.launcher.PreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = DockerLaunchUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.DEFAULT_IMAGE, "");
		store.setDefault(PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
	}

}
