/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
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
import org.yocto.crops.internal.sdk.core.Activator;

public class YoctoBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = Activator.PLUGIN_ID + ".yoctoBuildConfigurationProvider";
	
	private ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
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
				for (IToolChain tc : toolChainManager.getToolChainsMatching(new HashMap<String,String>())) {
					toolChain = tc;
					break;
				}
			}

			if (toolChain != null) {
				return new YoctoBuildConfiguration(config, name, toolChain);
			} else {
				// No valid combinations
				return null;
			}
		} else {
			return new YoctoBuildConfiguration(config, name);
		}
	}

	@Override
	public ICBuildConfiguration createBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		// See if there is one already
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				YoctoBuildConfiguration cmakeConfig = cconfig.getAdapter(YoctoBuildConfiguration.class);
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
		ICMakeToolChainFile file = null;
		Collection<ICMakeToolChainFile> files = manager.getToolChainFilesMatching(properties);
		if (!files.isEmpty()) {
			file = files.iterator().next();
		}

		// create config
		String configName = "cmake." + launchMode + '.' + toolChain.getId(); //$NON-NLS-1$
		IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName, monitor);
		YoctoBuildConfiguration cmakeConfig = new YoctoBuildConfiguration(config, configName, toolChain, file,
				launchMode);
		configManager.addBuildConfiguration(config, cmakeConfig);
		return cmakeConfig;
	}

}
