package net.akehurst.application.framework.technology.interfaceFilesystem;

import java.io.Reader;

public interface IFile {

	boolean exists();

	Reader reader() throws FilesystemException;
	
}
