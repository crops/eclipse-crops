/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.model;

public interface IYoctoInstancePreferences {

	public static final String NODE_ID = "org.yocto.crops.sdk.core";

	public static final String INSTPREFS_URIPREFIX_NODE_NAME = "uriPrefixDefault";
	public static final String INSTPREFS_URIPREFIX_NODE_DEFAULT = "unix";

	public static final String INSTPREFS_IMAGE_FILTER_NAME = "imageFilter";
	public static final String INSTPREFS_IMAGE_FILTER_DEFAULT = "(&(repo=bavery/scott)(tags=cross))";

	public static final String INSTPREFS_PORT_NAME = "port";
	public static final String INSTPREFS_PORT_DEFAULT = "2345";

}
