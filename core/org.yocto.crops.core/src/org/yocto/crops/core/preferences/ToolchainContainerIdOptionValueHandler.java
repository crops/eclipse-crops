package org.yocto.crops.core.preferences;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.jface.preference.IPreferenceStore;
import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.core.preferences.PreferenceConstants;

public class ToolchainContainerIdOptionValueHandler implements IManagedOptionValueHandler {

	String defaultValue = null;
	
	public ToolchainContainerIdOptionValueHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
			String artifactName = null;
			IPreferenceStore store = null;
			String system_toolchain_container_id = null;
			
			if (configuration instanceof IConfiguration) {
				artifactName =((IConfiguration) configuration).getArtifactName();
			} else {
				artifactName = "toolchain_container_id";
			}
			try {
				store = CropsCorePlugin.getDefault().getPreferenceStore();
			} catch (NullPointerException e) {
				// ignore it
				// e.printStackTrace();
			}
			if (store != null) {
				system_toolchain_container_id = store.getDefaultString(PreferenceConstants.P_TOOLCHAIN_CONTAINER_ID);
			}
		
			defaultValue = system_toolchain_container_id;
			
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
