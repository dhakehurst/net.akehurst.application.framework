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
import java.util.List;
import java.util.Map;

import org.jooq.lambda.tuple.Tuple3;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiEditor;

public class WebGuiEditor extends WebGuiText implements IGuiEditor {

    public WebGuiEditor(final IGuiRequest guiRequest, final IGuiScene scene, final IGuiDialog dialog, final String elementName) {
        super(guiRequest, scene, dialog, elementName);
    }

    /**
     * this.onEvent(session, "editor.InputChanged", (e) -> { final String text = (String) e.getDataItem(this.getElementId()); final String jsonParseTreeData =
     * this.languageService.update(text); super.getGuiRequest().updateParseTree(session, this.getScene().getStageId(), this.getScene().getSceneId(),
     * this.getElementId(), jsonParseTreeData); });
     */
    @Override
    public void create(final UserSession session, final String languageId, final String initialContent, final String options) {
        super.getGuiRequest().editorCreate(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(), initialContent, languageId,
                options);

    }

    @Override
    public void defineTextColourTheme(final UserSession session, final String themeName, final Map<String, Tuple3<String, String, String>> theme) {
        super.getGuiRequest().editorDefineTextColourTheme(session, this.getScene().getStageId(), this.getScene().getSceneId(), themeName, theme);
    }

    @Override
    public void updateParseTree(final UserSession session, final String jsonParseTreeData) {
        super.getGuiRequest().editorUpdateParseTree(session, this.getScene().getStageId(), this.getScene().getSceneId(), this.getElementId(),
                jsonParseTreeData);
    }

    @Override
    public void onProvideCompletionItems(final UserSession session, final IGuiEditor.onProvideCompletionItems handler) {
        super.getGuiRequest().onRequest("Editor.provideCompletionItems", (session1, channelId, data) -> {
            final String text = (String) data.get("text");
            final int position = (int) data.get("position");
            final List<Map<String, Object>> items = handler.provide(text, position);
            final Map<String, Object> result = new HashMap<>();
            result.put("result", items);
            return result;
        });
    }

}
