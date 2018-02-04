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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectoryEntry;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

abstract public class DirectoryEntry implements IDirectoryEntry {

    public DirectoryEntry(final IFilesystem fs, final Path path) {
        this.fs = fs;
        this.path = path;
    }

    private final IFilesystem fs;

    private final Path path;

    public IFilesystem getFilesystem() {
        return this.fs;
    }

    public Path asPath() {
        return this.path;
    }

    public File asFile() {
        return this.asPath().toFile();
    }

    @Override
    public String getName() {
        return this.asPath().getFileName().toString();
    }

    @Override
    public String getFullName() {
        return this.asPath().toString().replaceAll("\\\\", "/");
    }

    @Override
    public URL toURL() throws FilesystemException {
        try {
            return this.asPath().toUri().toURL();
        } catch (final MalformedURLException e) {
            throw new FilesystemException("Cannot convert to URL", e);
        }
    }

    @Override
    public IDirectory getParent() {
        return new DirectoryImpl(this.getFilesystem(), this.asPath().getParent());
    }

    @Override
    public boolean exists() {
        return Files.exists(this.asPath());
    }

    @Override
    public void delete() throws FilesystemException {
        try {
            Files.delete(this.asPath());
        } catch (final IOException e) {
            throw new FilesystemException(e.getMessage(), e);
        }
    }

    // --- Object ---
    @Override
    public int hashCode() {
        return this.asPath().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof DirectoryEntry) {
            return this.asPath().equals(((DirectoryEntry) obj).asPath());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getName();
    }

}
