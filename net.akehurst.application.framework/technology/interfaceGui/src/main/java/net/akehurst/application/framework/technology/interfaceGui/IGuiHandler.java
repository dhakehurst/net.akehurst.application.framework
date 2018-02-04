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
package net.akehurst.application.framework.technology.interfaceGui;

import java.net.URL;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiHandler {

    void addAuthentication(UserSession session) throws GuiException;

    void clearAuthentication(UserSession session) throws GuiException;

    void navigateTo(UserSession session, String location);

    IGuiScene getScene(final SceneIdentity sceneId);

    <T extends IGuiScene> T getScene(final SceneIdentity sceneId, final Class<T> sceneType);

    <T extends IGuiScene> T createScene(final StageIdentity stageId, final SceneIdentity sceneId, final Class<T> sceneClass, IGuiSceneHandler sceneHandler,
            final URL content);

}
