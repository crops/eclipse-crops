/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.yocto.crops.autotools.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.ui.TemplateWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.yocto.crops.autotools.AutotoolsProjectGenerator;

public class NewAutotoolsProjectWizard extends TemplateWizard {

	private WizardNewProjectCreationPage mainPage;

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(),
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle("New Autotools Project"); //$NON-NLS-1$
		mainPage.setDescription("Specify properties of new Autotools project."); //$NON-NLS-1$
		this.addPage(mainPage);
	}

	@Override
	protected IGenerator getGenerator() {
		AutotoolsProjectGenerator generator = new AutotoolsProjectGenerator("templates/autotools/manifest.xml"); //$NON-NLS-1$
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) {
			generator.setLocationURI(mainPage.getLocationURI());
		}
		return generator;
	}

}
