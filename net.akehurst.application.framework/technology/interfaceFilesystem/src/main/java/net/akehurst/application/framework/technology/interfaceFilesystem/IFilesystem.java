package net.akehurst.application.framework.technology.interfaceFilesystem;

public interface IFilesystem {

	IFile file(String pathName);
	
	IDirectory directory(String pathNme);
}
