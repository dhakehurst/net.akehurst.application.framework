package net.akehurst.application.framework.technology.interfaceFilesystem;

import net.akehurst.application.framework.common.property.Property;

public interface IDirectoryEntry {

	Property<String> name();

	Property<String> fullName();

	Property<IDirectory> parent();

	boolean exists();

	void delete() throws FilesystemException;
}
