package org.yocto.crops.core.builder;

import org.eclipse.cdt.managedbuilder.core.ExternalBuildRunner;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;

public class CropsExternalToolBuilder extends ExternalBuildRunner
		implements IBuildEnvironmentVariable, IBuildConfiguration {

	public CropsExternalToolBuilder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOperation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDelimiter() {
		// TODO Auto-generated method stub
		return null;
	}

}
