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

public class CropsRootOptionValueHandler implements IManagedOptionValueHandler {

	String defaultValue = null;
	
	public CropsRootOptionValueHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
			String artifactName = null;
			IPreferenceStore store = null;
			String system_crops_root = null;
			
			if (configuration instanceof IConfiguration) {
				artifactName =((IConfiguration) configuration).getArtifactName();
			} else {
				artifactName = "crops_root";
			}
			try {
				store = CropsCorePlugin.getDefault().getPreferenceStore();
			} catch (NullPointerException e) {
				// ignore it
				// e.printStackTrace();
			}
			if (store != null) {
				system_crops_root = store.getDefaultString(PreferenceConstants.P_CROPS_ROOT);
			}
		
			defaultValue = system_crops_root;
			
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
