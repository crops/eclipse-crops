package org.yocto.crops.internal.container.docker.launch.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.docker.ui.launch.RunDockerImageLaunchConfigurationDelegate;

public class YoctoRunDockerImageLaunchConfigurationDelegate extends RunDockerImageLaunchConfigurationDelegate {
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
		super.launch(configuration, mode, launch, monitor);
	}
}
