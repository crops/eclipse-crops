package org.yocto.crops.zephyr.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.yocto.crops.core.CropsCorePlugin;

public class MakefileBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = CropsCorePlugin.getId() + ".zephyrMakefileBuilder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
			throws CoreException {
		
		IProject project = getProject();
		try {
			//IConsoleService console = CropsCDTCorePlugin.getService(IConsoleService.class);
			// MakefileBuildConfiguration makeConfig = project.getActiveBuildConfig()
			// .getAdapter(MakefileBuildConfiguration.class);
			// Path buildDir = makeConfig.getBuildDirectory();
			
			//if (!Files.exists(buildDirectory()., arg1))
		} finally {
		}
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
	}
}
