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

public class CeedOptionValueHandler implements IManagedOptionValueHandler {

	String defaultValue = null;
	
	public CeedOptionValueHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
			String artifactName = null;
			IPreferenceStore store = null;
			String system_ceed = null;
			
			if (configuration instanceof IConfiguration) {
				artifactName =((IConfiguration) configuration).getArtifactName();
			} else {
				artifactName = "ceed";
			}
			/* FIXME: apparently the PreferenceStore does not always already exist ... */
			try {
				store = CropsCorePlugin.getDefault().getPreferenceStore();
			} catch (NullPointerException e) {
				// ignore it
				// e.printStackTrace();
			}
			if (store != null) {
				system_ceed = store.getDefaultString(PreferenceConstants.P_CEED_PATH);
		
				defaultValue = system_ceed;
			} else {
				defaultValue = null;
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
