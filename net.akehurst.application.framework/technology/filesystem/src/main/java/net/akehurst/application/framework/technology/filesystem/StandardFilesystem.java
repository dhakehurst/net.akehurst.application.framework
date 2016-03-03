/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.akehurst.application.framework.components.AbstractComponent;
import net.akehurst.application.framework.components.Port;
import net.akehurst.application.framework.os.annotations.PortInstance;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

public class StandardFilesystem extends AbstractComponent implements IFilesystem {

	public StandardFilesystem(String id) {
		super(id);
	}
	
	@Override
	public void afRun() {
		// TODO Auto-generated method stub
		
	}
	
	// ---------- IFilesystem ---------
	@Override
	public IFile file(String pathName) {
		Path path = Paths.get(pathName);
		return new File(path);
	}
	
	@Override
	public IDirectory directory(String pathNme) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// ---------- Ports ---------
	@PortInstance(provides={IFilesystem.class},requires={})
	Port portFs;
	public Port portFs() {
		return this.portFs;
	}
	
}
