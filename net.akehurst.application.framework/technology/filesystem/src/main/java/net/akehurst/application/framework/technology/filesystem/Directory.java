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
import java.util.List;

import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectoryEntry;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

public class Directory extends DirectoryEntry implements IDirectory {

	public Directory(final IFilesystem fs, final Path path) {
		super(fs, path);
	}

	@Override
	public List<IDirectoryEntry> getEntries() throws FilesystemException {
		if (this.exists()) {
			try {
				final ArrayList<IDirectoryEntry> result = new ArrayList<>();
				final PathMatcher matcher = this.path.getFileSystem().getPathMatcher("glob:" + this.fullName().get() + "/" + "*");
				final SimpleFileVisitor<Path> v = new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes attrs) throws IOException {
						if (matcher.matches(path)) {
							if (Files.isDirectory(path)) {
								result.add(Directory.this.fs.directory(path.toString()));
							} else {
								result.add(Directory.this.fs.file(path.toString()));
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
								result.add(Directory.this.fs.directory(path.toString()));
							} else {
								result.add(Directory.this.fs.file(path.toString()));
							}
							return FileVisitResult.CONTINUE;
						} else {
							return FileVisitResult.CONTINUE;
						}
					}
				};
				Files.walkFileTree(this.path, v);
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

}
