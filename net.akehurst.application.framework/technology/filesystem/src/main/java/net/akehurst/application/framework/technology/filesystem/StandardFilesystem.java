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

import java.nio.file.Path;
import java.nio.file.Paths;

import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.realisation.Port;
import net.akehurst.application.framework.technology.interfaceFilesystem.IDirectory;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;

public class StandardFilesystem extends AbstractComponent implements IFilesystem, IService {

    public StandardFilesystem(final String id) {
        super(id);
    }

    @Override
    public void afConnectParts() {}

    @Override
    public void afRun() {
        // TODO Auto-generated method stub

    }

    @Override
    public Object createReference(final String locationId) {
        return this;
    }

    // ---------- IFilesystem ---------
    @Override
    public IFile file(final String pathName) {
        final Path path = Paths.get(pathName);
        return new FileImpl(this, path);
    }

    @Override
    public IDirectory directory(final String pathName) {
        final Path path = Paths.get(pathName);
        return new DirectoryImpl(this, path);
    }

    // ---------- Ports ---------
    @PortInstance
    @PortContract(provides = IFilesystem.class)
    Port portFs;

    public Port portFs() {
        return this.portFs;
    }

}
