package org.yocto.crops.zephyr;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.zephyr.preferences.PreferenceConstants;

public class ZephyrBaseOptionValueHandler implements IManagedOptionValueHandler {

	String defaultValue = null;
	
	public ZephyrBaseOptionValueHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
			String artifactName = null;
			IPreferenceStore store = null;
			String system_zephyr_base = null;

			
			if (configuration instanceof IConfiguration) {
				artifactName =((IConfiguration) configuration).getArtifactName();
			} else {
				artifactName = "zephyr_base";
			}
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			String workspaceName = workspace.getRoot().getLocation().lastSegment();
			/* FIXME: project doesn't exist yet when we want to access it */
			IProject project = workspace.getRoot().getProject();
			String projectName = null;
//			ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
			/* TODO: how to get the IConfigurationDescription and under which scope is it available? */
//			IConfigurationDescription iconfigdesc = CCorePlugin.getDefault().
//			projectName = varManager.resolveValue("ProjName", null, ";", configuration.getBaseId());
			try {
				if (project != null) {
					projectName = project.getLocation().lastSegment();
					if (projectName != null) {
						defaultValue = CropsUtils.getCropsRoot() + "/" + projectName + "/zephyr-project";
					}
				} else {
					defaultValue = CropsUtils.getCropsRoot() + "/${ProjName}/zephyr-project";
				}
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				store = ZephyrPlugin.getDefault().getPreferenceStore();
			} catch (NullPointerException e) {
				// ignore it
			//	e.printStackTrace();
			}
			if (store != null) {
				system_zephyr_base = store.getDefaultString(PreferenceConstants.P_ZEPHYR_BASE);
			}

			option.setDefaultValue( defaultValue );
			
			if( option.getValue() == null ){
				try {
					option.setValue(defaultValue);
				} catch (BuildException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			return false;
	}

	@Override
	public boolean isDefaultValue(IBuildObject configuration, IHoldsOptions holder, IOption option,
			String extraArgument) {
		if (defaultValue != null)
		{
			try {
				return option.getStringValue().equals(defaultValue);
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean isEnumValueAppropriate(IBuildObject configuration, IHoldsOptions holder, IOption option,
			String extraArgument, String enumValue) {
		// TODO Auto-generated method stub
		return false;
	}

}
