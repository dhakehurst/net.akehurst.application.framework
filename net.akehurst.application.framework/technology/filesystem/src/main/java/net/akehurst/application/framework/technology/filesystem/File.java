package net.akehurst.application.framework.technology.filesystem;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;

public class File implements IFile {

	public File(Path path) {
		this.path = path;
	}
	
	Path path;

	public boolean exists() {
		return path.toFile().exists();
	}
	
	@Override
	public Reader reader() throws FilesystemException {
		try {
			return Files.newBufferedReader(this.path);
		} catch (Exception ex) {
			throw new FilesystemException("Failed to create Reader",ex);
		}
	}
	
}
