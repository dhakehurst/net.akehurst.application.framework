package net.akehurst.application.framework.technology.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.akehurst.application.framework.common.property.Property;
import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectoryEntry;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

abstract public class DirectoryEntry implements IDirectoryEntry {

	public DirectoryEntry(final IFilesystem fs, final Path path) {
		this.fs = fs;
		this.path = path;
		this.name().set(path.getFileName().toString());
		this.fullName().set(path.toString().replaceAll("\\\\", "/"));
	}

	IFilesystem fs;

	Path path;

	Property<String> nameProperty;

	@Override
	public Property<String> name() {
		if (null == this.nameProperty) {
			this.nameProperty = new Property<>();
		}
		return this.nameProperty;
	}

	Property<String> fullNameProperty;

	@Override
	public Property<String> fullName() {
		if (null == this.fullNameProperty) {
			this.fullNameProperty = new Property<>();
		}
		return this.fullNameProperty;
	}

	Property<IDirectory> parentProperty;

	@Override
	public Property<IDirectory> parent() {
		if (null == this.parentProperty) {
			this.parentProperty = new Property<>();
			this.parentProperty.set(new Directory(this.fs, this.path.getParent()));
		}
		return this.parentProperty;
	}

	@Override
	public boolean exists() {
		return Files.exists(this.path);
	}

	@Override
	public void delete() throws FilesystemException {
		try {
			Files.delete(this.path);
		} catch (final IOException e) {
			throw new FilesystemException(e.getMessage(), e);
		}
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof DirectoryEntry) {
			return this.path.equals(((DirectoryEntry) obj).path);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.name().get();
	}

}
