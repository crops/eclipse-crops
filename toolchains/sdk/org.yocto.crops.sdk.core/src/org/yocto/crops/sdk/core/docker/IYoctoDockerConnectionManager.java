/*******************************************************************************
 * Copyright (c) 2017, Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.yocto.crops.sdk.core.docker;

import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.osgi.framework.Filter;

public interface IYoctoDockerConnectionManager {

	List<IDockerConnection> getConnections();

	List<IDockerImage> getImagesForConnection(IDockerConnection connection, Filter repoTagsFilter);

}
