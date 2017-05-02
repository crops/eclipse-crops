/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.internal.sdk.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.yocto.crops.sdk.core.model.YoctoProjectPreferences;

public class YoctoProjectPropertyPage extends PropertyPage {

	public static final String NODE_NAME = "yoctoBuildContainerConfig";
	
	private Text connectionPrefix;
	private Text imageFilter;
	private Text port;
	private IProject project;
	
	public Control createContents(Composite parent) {
		
		YoctoProjectPreferences prefs = new YoctoProjectPreferences(project);
		
		prefs.readPreferences();
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		Label connectionPrefixLabel = new Label(mainComposite, SWT.NULL);
		connectionPrefixLabel.setText("Connection Prefix:");
		
		connectionPrefix = new Text(mainComposite,SWT.SINGLE);
		connectionPrefix.setText(prefs.getConnectionCriteria());
		connectionPrefix.setLayoutData(gridData);
		
		Label imageLabel = new Label(mainComposite, SWT.NULL);
		imageLabel.setText("Image Filter:");

		imageFilter = new Text(mainComposite,SWT.SINGLE);
		imageFilter.setText(prefs.getImageFilter());
		imageFilter.setLayoutData(gridData);

		Label portLabel = new Label(mainComposite, SWT.NULL);
		portLabel.setText("Port:");

		port = new Text(mainComposite,SWT.SINGLE);
		port.setText(prefs.getContainerPort());
		port.setLayoutData(gridData);

		return mainComposite;
	}
	@Override
	public void setElement(IAdaptable element) {
		super.setElement(element);
		this.project = (IProject) element;
	}
	public boolean performOk() {
		System.out.println("Ok");
		return true;
	}
	@Override
	protected void performDefaults() {
		super.performDefaults();
		
	}
}