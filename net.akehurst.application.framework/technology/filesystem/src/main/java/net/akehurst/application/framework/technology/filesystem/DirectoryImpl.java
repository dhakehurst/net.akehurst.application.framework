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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectoryEntry;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

public class DirectoryImpl extends DirectoryEntry implements IDirectory {

    public DirectoryImpl(final IFilesystem fs, final Path path) {
        super(fs, path);
    }

    private List<PathMatcher> createPathMatcher(final List<String> globs) {
        final List<PathMatcher> result = new ArrayList<>();
        for (final String glob : globs) {
            final PathMatcher pm = this.asPath().getFileSystem().getPathMatcher("glob:" + glob);
            result.add(pm);
        }
        return result;
    }

    private boolean matchesOneOf(final Path path, final List<PathMatcher> matchers) {
        for (final PathMatcher pm : matchers) {
            if (pm.matches(path)) {
                return true;
            }
        }
        return false;
    }

    private List<IDirectoryEntry> findAll(final boolean includeDirectories, final boolean includeFiles, final List<String> includeGlobs,
            final List<String> excludeGlobs, final int maxDepth) throws FilesystemException {
        if (this.exists()) {
            try {
                final List<IDirectoryEntry> result = new ArrayList<>();
                final List<PathMatcher> includeMatchers = this.createPathMatcher(includeGlobs);
                final List<PathMatcher> excludeMatchers = this.createPathMatcher(excludeGlobs);

                final SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes attrs) throws IOException {
                        if (includeDirectories) {
                            if (DirectoryImpl.this.matchesOneOf(path, includeMatchers) && !DirectoryImpl.this.matchesOneOf(path, excludeMatchers)) {
                                final IDirectory directory = new DirectoryImpl(DirectoryImpl.this.getFilesystem(), path);
                                result.add(directory);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                        if (includeFiles) {
                            if (DirectoryImpl.this.matchesOneOf(path, includeMatchers) && !DirectoryImpl.this.matchesOneOf(path, excludeMatchers)) {
                                final IFile file = new FileImpl(DirectoryImpl.this.getFilesystem(), path);
                                result.add(file);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                };

                Files.walkFileTree(this.asPath(), new HashSet<>(), maxDepth, visitor);
                return result;
            } catch (final IOException e) {
                throw new FilesystemException(e.getMessage(), e);
            }

        } else {
            throw new FilesystemException("Directory does not exist", null);
        }
    }

    // --- IDirectory ---

    @Override
    public List<IDirectoryEntry> getEntries() throws FilesystemException {
        if (this.exists()) {
            try {
                final ArrayList<IDirectoryEntry> result = new ArrayList<>();
                final PathMatcher matcher = this.asPath().getFileSystem().getPathMatcher("glob:" + this.getFullName() + "/" + "*");
                final SimpleFileVisitor<Path> v = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes attrs) throws IOException {
                        if (matcher.matches(path)) {
                            if (Files.isDirectory(path)) {
                                result.add(DirectoryImpl.this.getFilesystem().directory(path.toString()));
                            } else {
                                result.add(DirectoryImpl.this.getFilesystem().file(path.toString()));
                            }
                            return FileVisitResult.CONTINUE;
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                        if (matcher.matches(path)) {
                            if (Files.isDirectory(path)) {
                                result.add(DirectoryImpl.this.getFilesystem().directory(path.toString()));
                            } else {
                                result.add(DirectoryImpl.this.getFilesystem().file(path.toString()));
                            }
                            return FileVisitResult.CONTINUE;
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }
                };
                Files.walkFileTree(this.asPath(), v);
                return result;
            } catch (final Throwable t) {
                throw new FilesystemException(t.getMessage(), t);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public IFile createFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDirectory createDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IFile> findAllFiles(final List<String> includeGlobs, final List<String> excludeGlobs) throws FilesystemException {
        return this.findAll(false, true, includeGlobs, excludeGlobs, Integer.MAX_VALUE).stream().filter(element -> element instanceof IFile)
                .map(element -> (IFile) element).collect(Collectors.toList());
    }

    @Override
    public List<IDirectoryEntry> findAll(final List<String> includeGlobs, final List<String> excludeGlobs) throws FilesystemException {
        return this.findAll(true, true, includeGlobs, excludeGlobs, Integer.MAX_VALUE);
    }

}
