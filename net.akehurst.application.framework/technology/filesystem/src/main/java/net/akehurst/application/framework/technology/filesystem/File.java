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

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

public class File extends DirectoryEntry implements IFile {

	public File(final IFilesystem fs, final Path path) {
		super(fs, path);
	}

	@Override
	public Reader reader() throws FilesystemException {
		try {
			return Files.newBufferedReader(this.path);
		} catch (final Exception ex) {
			throw new FilesystemException("Failed to create Reader", ex);
		}
	}

}
