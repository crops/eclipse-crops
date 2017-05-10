package org.yocto.crops.internal.sdk.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.yocto.crops.internal.sdk.ui.Activator;
import org.yocto.crops.sdk.core.model.CMakeProjectGenerator;

public class CMakeNewProjectWizard extends TemplateWizard {

	private static final String WIZARD_IMAGE_FILE = "icons/yocto_dot-64x64.png";
	
	private WizardNewProjectCreationPage mainPage;

	public CMakeNewProjectWizard() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), null,
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
				ImageDescriptor id = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, WIZARD_IMAGE_FILE);
				setImageDescriptor(id);
			}
		};
		mainPage.setTitle("New Yocto CMake Project"); //$NON-NLS-1$
		mainPage.setDescription("Specify properties for Yocto CMake project"); //$NON-NLS-1$
		this.addPage(mainPage);
	}

	@Override
	protected IGenerator getGenerator() {
		CMakeProjectGenerator generator = new CMakeProjectGenerator(); //$NON-NLS-1$
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) 
			generator.setLocationURI(mainPage.getLocationURI());
		return generator;
	}

}
