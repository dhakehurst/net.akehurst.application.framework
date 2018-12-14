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
package net.akehurst.application.framework.common.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.akehurst.application.framework.common.ActiveObject;
import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.test.annotation.MockActiveObjectInstance;
import net.akehurst.application.framework.common.test.annotation.MockComponentInstance;
import net.akehurst.application.framework.common.test.annotation.MockPassiveObjectInstance;
import net.akehurst.application.framework.common.test.annotation.TestActiveObjectInstance;
import net.akehurst.application.framework.common.test.annotation.TestComponentInstance;
import net.akehurst.application.framework.common.test.annotation.TestPassiveObjectInstance;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.datatype.common.DatatypeAbstract;

public class TestCompositionTreeWalker {

    public TestCompositionTreeWalker(final ILogger logger) {
        this.logger = logger;
    }

    ILogger logger;

    static public class TestPartInfo extends DatatypeAbstract {
        public TestPartInfo(final Field field, final TestPartKind kind, final Class<? extends IIdentifiableObject> class_, final String id) {
            super(field, kind, class_, id);
            this.field = field;
            this.kind = kind;
            this.class_ = class_;
            this.id = id;
        }

        Field field;
        TestPartKind kind;
        Class<? extends IIdentifiableObject> class_;
        String id;
    }

    interface TestBuildAction {
        IIdentifiableObject execute(TestPartKind partKind, Class<? extends IIdentifiableObject> partClass, String partId) throws ApplicationFrameworkException;
    }

    public void build(final IIdentifiableObject object, final TestBuildAction action) {

        final Set<TestPartInfo> partInfo = TestCompositionTreeWalker.findPartInfo(object.afId(), object.getClass());

        for (final TestPartInfo pi : partInfo) {
            try {
                final IIdentifiableObject part = action.execute(pi.kind, pi.class_, pi.id);
                pi.field.set(object, part);
                // this.build(idPrefix, part, action);
            } catch (final Throwable t) {
                this.logger.log(LogLevel.ERROR, "unable to build part", t);
            }
        }

    }

    interface WalkAction {
        void execute(IIdentifiableObject tObj, String tObjId);
    }

    public void walkOneAndApply(final IIdentifiableObject obj, final WalkAction action) {
        action.execute(obj, obj.afId());

        final List<IIdentifiableObject> parts = this.findParts(obj.afId(), obj);

        for (final IIdentifiableObject p : parts) {
            action.execute(p, p.afId());
        }

    }

    public void walkAllAndApply(final IApplication applicationObject, final WalkAction action) {
        action.execute(applicationObject, applicationObject.afId());

        final List<IIdentifiableObject> parts = this.findParts(applicationObject.afId(), applicationObject);

        for (final IIdentifiableObject p : parts) {
            action.execute(p, p.afId());
            this.walkAndApply(applicationObject.afId(), p, action);
        }

    }

    void walkAndApply(final String idPrefix, final IIdentifiableObject part, final WalkAction action) {

        final List<IIdentifiableObject> parts = this.findParts(idPrefix, part);

        for (final IIdentifiableObject p : parts) {
            final String id = idPrefix + "." + p.afId();
            action.execute(p, id);
            this.walkAndApply(id, p, action);
        }

    }

    static public Set<TestPartInfo> findPartInfo(final String idPrefix, final Class<?> class_) {
        final Set<TestPartInfo> result = new HashSet<>();

        if (null == class_.getSuperclass()) {
        } else {
            final Set<TestPartInfo> sp = TestCompositionTreeWalker.findPartInfo(idPrefix, class_.getSuperclass());
            result.addAll(sp);
        }
        for (final Field f : class_.getDeclaredFields()) {
            final TestPartInfo pi = TestCompositionTreeWalker.findPartInfo(idPrefix, f);
            if (null == pi) {
                // ignore
            } else {
                result.add(pi);
            }
        }

        return result;
    }

    static private TestPartInfo findPartInfo(final String idPrefix, final Field f) {
        f.setAccessible(true);
        TestPartInfo pi = null;

        final TestComponentInstance ann = f.getAnnotation(TestComponentInstance.class);
        if (null == ann) {
            // do nothing
        } else {
            pi = TestCompositionTreeWalker.findPartInfo((Class<? extends IComponent>) f.getType(), idPrefix, ann.id(), f, TestPartKind.TEST_COMPONENT);
        }

        final MockComponentInstance ann1 = f.getAnnotation(MockComponentInstance.class);
        if (null == ann1) {
            // do nothing
        } else {
            pi = TestCompositionTreeWalker.findPartInfo((Class<? extends IComponent>) f.getType(), idPrefix, ann1.id(), f, TestPartKind.MOCK_COMPONENT);
        }

        final TestActiveObjectInstance ann2 = f.getAnnotation(TestActiveObjectInstance.class);
        if (null == ann2) {
            // do nothing
        } else {
            pi = TestCompositionTreeWalker.findPartInfo((Class<? extends ActiveObject>) f.getType(), idPrefix, ann2.id(), f, TestPartKind.TEST_ACTIVE_OBJECT);
        }
        final MockActiveObjectInstance ann3 = f.getAnnotation(MockActiveObjectInstance.class);
        if (null == ann3) {
            // do nothing
        } else {
            pi = TestCompositionTreeWalker.findPartInfo((Class<? extends ActiveObject>) f.getType(), idPrefix, ann3.id(), f, TestPartKind.MOCK_ACTIVE_OBJECT);
        }

        final TestPassiveObjectInstance ann4 = f.getAnnotation(TestPassiveObjectInstance.class);
        if (null == ann4) {
            // do nothing
        } else {
            pi = TestCompositionTreeWalker.findPartInfo((Class<? extends IIdentifiableObject>) f.getType(), idPrefix, ann4.id(), f,
                    TestPartKind.TEST_PASSIVE_OBJECT);
        }
        final MockPassiveObjectInstance ann5 = f.getAnnotation(MockPassiveObjectInstance.class);
        if (null == ann5) {
            // do nothing
        } else {
            pi = TestCompositionTreeWalker.findPartInfo((Class<? extends IIdentifiableObject>) f.getType(), idPrefix, ann5.id(), f,
                    TestPartKind.MOCK_PASSIVE_OBJECT);
        }
        return pi;
    }

    static <FT extends IIdentifiableObject> TestPartInfo findPartInfo(final Class<FT> fieldType, final String idPrefix, String objId, final Field f,
            final TestPartKind partKind) {
        if (objId.isEmpty()) {
            objId = f.getName();
        } else {
            // do nothing
        }
        final String partId = idPrefix + "." + objId;
        return new TestPartInfo(f, partKind, fieldType, partId);
    }

    private List<IIdentifiableObject> findParts(final String idPrefix, final IIdentifiableObject object) {
        final List<IIdentifiableObject> result = new ArrayList<>();
        final Set<TestPartInfo> partInfo = TestCompositionTreeWalker.findPartInfo(idPrefix, object.getClass());
        for (final TestPartInfo pi : partInfo) {
            try {
                final IIdentifiableObject part = (IIdentifiableObject) pi.field.get(object);
                result.add(part);
            } catch (final Throwable t) {
                this.logger.log(LogLevel.ERROR, "unable to find part", t);
            }
        }
        return result;
    }

}
