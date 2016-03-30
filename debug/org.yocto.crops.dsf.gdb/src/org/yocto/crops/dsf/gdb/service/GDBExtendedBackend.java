package org.yocto.crops.dsf.gdb.service;

import org.eclipse.cdt.dsf.gdb.service.extensions.GDBBackend_HEAD;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Extension of Top-level class in the version hierarchy of implementations of {@link IMIBackend}.
 * <br> 
 * This subclass is for our special needs, which will allow
 * us to always extend the most recent version of the service.
 * For example, if GDB<Service>_7_9 is added, this GDB<Service>_HEAD class
 * will be changed to extend it instead of the previous version, therefore
 * automatically allowing extenders to be extending the new class.
 * 
 * NOTE: Older versions of GDB that were already using an extending class,
 *       will automatically start using the new service version, which may
 *       not be desirable.  Extenders should update how they extend
 *       GdbDebugServicesFactory to properly choose the version of the
 *       service that should be used for older GDBs.
 *       
 *       On the contrary, not using GDB<Service>_HEAD requires the
 *       extender to update how they extend GdbDebugServicesFactory
 *       whenever a new GDB<Service> version is added.
 *       
 *       Extenders that prefer to focus on the latest GDB version are
 *       encouraged to extend GDB<Service>_HEAD.
 * 
 * @since 4.8
 */
public class GDBExtendedBackend extends GDBBackend_HEAD {

	public GDBExtendedBackend(DsfSession session, ILaunchConfiguration lc) {
		super(session, lc);
	}
	
	/* TODO: if we are asking in host context run super
	 * but if we are asking in docker container context use this
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.service.GDBBackend#getProgramPath()
	 */
	@Override
	public IPath getProgramPath() {
		IPath hostPath = super.getProgramPath();
		return toContainerPath(hostPath);
	}
	
	private static IPath toContainerPath(IPath hostPath) {
		// First verify we are dealing with a proper project.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String workspaceName = workspace.getRoot().getLocation().lastSegment();
		// Now verify we know the program to debug.
		// We can verify the Host local path
		// but then we must translate that into the container path
		String[] segments = hostPath.segments();
		boolean foundworkspace = false;
		/* Assume hostPath is <some_path>/<workspace_loc>/<project>
		 * Assume containerPath is /crops/<project>
		 * TODO: add checks to validate both hostPath and containerPath
		 */
		Path containerPath = new Path("/crops"); //$NON-NLS-1$
		for (String segment : segments) {
			if (segment.equals(workspaceName)) {
					foundworkspace = true;
					continue;
			}
			if (foundworkspace)
				containerPath = new Path(containerPath.toPortableString() + Path.SEPARATOR + segment);
		}
		return containerPath;
	}
}
