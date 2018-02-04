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
package net.akehurst.application.framework.technology.gui.web.elements;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.grid.IGuiGrid;

public class WebGuiGrid extends WebGuiElement implements IGuiGrid {

    public WebGuiGrid(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
        super(guiRequest, scene, dialog, elementName);
    }

    @Override
    public void create(final UserSession session, final String jsonOptions) {
        this.getGuiRequest().gridCreate(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), jsonOptions);
    }

    @Override
    public void remove(final UserSession session) {
        this.getGuiRequest().gridRemove(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId());
    }

    @Override
    public void appendItem(final UserSession session, final Map<String, Object> data, final int x, final int y, final int w, final int h) {
        final Map<String, Integer> location = new HashMap<>();
        location.put("x", x);
        location.put("y", y);
        location.put("w", w);
        location.put("h", h);
        this.getGuiRequest().gridAppendItem(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), data, location);
    }

    @Override
    public void appendItem(final UserSession session, final Map<String, Object> data) {
        final Map<String, Integer> location = new HashMap<>();
        this.getGuiRequest().gridAppendItem(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), data, location);
    }

    @Override
    public void removeItem(final UserSession session, final String itemId) {
        this.getGuiRequest().gridRemoveItem(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), itemId);
    }

}
