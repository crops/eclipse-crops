/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.yocto.crops.make;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

public class MakeBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.yocto.crops.autotools.provider"; //$NON-NLS-1$

	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		IToolChain toolChain = null;

		// try the toolchain for the local target
		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, Platform.getOS());
		properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
		IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
		for (IToolChain tc : toolChainManager.getToolChainsMatching(properties)) {
			toolChain = tc;
			break;
		}

		// local didn't work, try and find one that does
		if (toolChain == null) {
			for (IToolChain tc : toolChainManager.getToolChainsMatching(new HashMap<>())) {
				toolChain = tc;
				break;
			}
		}

		if (toolChain != null) {
			return new MakeBuildConfiguration(config, name, toolChain);
		} else {
			return null;
		}
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		// See if there is one already
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				CBuildConfiguration cmakeConfig = cconfig.getAdapter(MakeBuildConfiguration.class);
				if (cmakeConfig != null && cmakeConfig.getToolChain().equals(toolChain)
						&& launchMode.equals(cmakeConfig.getLaunchMode())) {
					return cconfig;
				}
			}
		}

		// get matching toolchain file if any
		Map<String, String> properties = new HashMap<>();
		String os = toolChain.getProperty(IToolChain.ATTR_OS);
		if (os != null && !os.isEmpty()) {
			properties.put(IToolChain.ATTR_OS, os);
		}
		String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
		if (!arch.isEmpty()) {
			properties.put(IToolChain.ATTR_ARCH, arch);
		}

		// create config
		String configName = "autotools." + launchMode + '.' + toolChain.getId(); //$NON-NLS-1$
		IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName, monitor);
		CBuildConfiguration autotoolsConfig = new MakeBuildConfiguration(config, configName);
		configManager.addBuildConfiguration(config, autotoolsConfig);
		return autotoolsConfig;
	}

}
