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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

public class FileImpl extends DirectoryEntry implements IFile {

    public FileImpl(final IFilesystem fs, final Path path) {
        super(fs, path);
    }

    @Override
    public byte[] readBytes() throws FilesystemException {
        try {
            return Files.readAllBytes(this.asPath());
        } catch (final IOException e) {
            throw new FilesystemException(e.getMessage(), e);
        }
    }

    @Override
    public Reader reader() throws FilesystemException {
        try {
            return Files.newBufferedReader(this.asPath());
        } catch (final Exception ex) {
            throw new FilesystemException("Failed to create Reader", ex);
        }
    }

    @Override
    public BufferedReader bufferedReader() throws FilesystemException {
        try {
            final Reader r = Files.newBufferedReader(this.asPath());
            final BufferedReader br = new BufferedReader(r);
            return br;
        } catch (final Exception ex) {
            throw new FilesystemException("Failed to create Reader", ex);
        }
    }

    @Override
    public IFile relativeTo(final IDirectory directory) {
        final Path path = directory.asPath().relativize(this.asPath());
        return new FileImpl(this.getFilesystem(), path);
    }

}
