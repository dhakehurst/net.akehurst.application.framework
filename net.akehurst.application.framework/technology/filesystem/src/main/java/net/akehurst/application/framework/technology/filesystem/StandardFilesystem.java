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
