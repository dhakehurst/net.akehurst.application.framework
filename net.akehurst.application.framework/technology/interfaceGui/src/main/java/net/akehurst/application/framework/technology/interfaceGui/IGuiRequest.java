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
import java.util.Map;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiLanguageDefinition;

public interface IGuiRequest {

	void createStage(StageIdentity stageId, boolean authenticated, URL content);

	<T extends IGuiScene> T createScene(StageIdentity stageId, SceneIdentity sceneId, Class<T> sceneClass, URL content);

	void switchTo(UserSession session, StageIdentity stageId, SceneIdentity sceneId, Map<String, String> sceneArguments);

	void requestRecieveEvent(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, String eventType);

	<T extends IGuiDialog> T createDialog(Class<T> dialogClass, UserSession session, IGuiScene scene, String modalId, String title, String dialogContent);

	void addElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String newElementId, String type);

	/**
	 *
	 * @param parentId
	 * @param newElementId
	 * @param type
	 * @param attributes
	 *            as a JSON string, using single quotes, e.g. "{ 'xxx':2, 'aa':'bb' }"
	 * @param content
	 */
	void addElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String newElementId, String type, String attributes,
			Object content);

	/**
	 * remove an element from the scene
	 *
	 * @param session
	 * @param stageId
	 * @param sceneId
	 * @param dialogId
	 */
	void removeElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String dialogId);

	/**
	 * clean the content of an element, but leave it present
	 *
	 * @param session
	 * @param stageId
	 * @param sceneId
	 * @param elementId
	 */
	void clearElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId);

	void setTitle(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String text);

	void setText(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String id, String text);

	void addChart(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String chartId, Integer width, Integer height,
			String chartType, String jsonChartData, String jsonChartOptions);

	void addDiagram(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String diagramId, String jsonDiagramData);

	void set(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementName, String propertyName, Object value);

	void tableAppendRow(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String tableId, Map<String, Object> rowData);

	void tableRemoveRow(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String tableId, String rowId);

	void showDialog(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String dialogId, String dialogContent);

	void addEditor(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String initialContent,
			IGuiLanguageDefinition languageDefinition);
}
