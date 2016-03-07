package org.yocto.crops.zephyr;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;

public class MakefileGenerator implements IManagedBuilderMakefileGenerator {

	private static final String makefile = "Makefile";
	@Override
	public void generateDependencies() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public MultiStatus generateMakefiles(IResourceDelta delta) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getBuildWorkingDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMakefileName() {
		return makefile;
	}

	@Override
	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isGeneratedResource(IResource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void regenerateDependencies(boolean force) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public MultiStatus regenerateMakefiles() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
