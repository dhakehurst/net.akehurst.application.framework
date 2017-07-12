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
import java.util.concurrent.Future;

import net.akehurst.application.framework.common.interfaceUser.UserSession;

public interface IGuiRequest {

	/**
	 *
	 * @param stageId
	 * @param rootPath
	 * @param authenticationRedirectURL
	 *            pass null for non authenticated roots
	 */
	void createStage(StageIdentity stageId, String rootPath, StageIdentity authenticationStageId, SceneIdentity authenticationSceneId);

	<T extends IGuiScene> T createScene(StageIdentity stageId, SceneIdentity sceneId, Class<T> sceneClass, URL content);

	/**
	 * add authentication to the gui
	 *
	 * @param session
	 * @throws GuiException
	 */
	void addAuthentication(UserSession session) throws GuiException;

	/**
	 * clear/remove authentication from the gui
	 *
	 * @param session
	 * @throws GuiException
	 */
	void clearAuthentication(UserSession session) throws GuiException;

	Future<String> oauthAuthorise(UserSession session, String clientId, String clientSecret, String site, String tokenPath, String authorisationPath,
			String scopes);

	/**
	 * if location starts with '/' then navigate to a location relative to the root of this site
	 *
	 * else
	 *
	 * assume a full URL and navigate to it
	 *
	 * @param session
	 * @param location
	 */
	void navigateTo(UserSession session, String location);

	void newWindow(UserSession session, String location);

	void switchTo(UserSession session, StageIdentity stageId, SceneIdentity sceneId, Map<String, String> sceneArguments);

	void download(UserSession session, String location, String filename);

	void upload(final UserSession session, final String uploadLink, final String filenameElementId);

	/**
	 * register a handler for a specific message
	 * 
	 * @param session
	 * @param channelId
	 * @param func
	 */
	void onRequest(final UserSession session, final String channelId, final IGuiRequestMessage func);

	void requestRecieveEvent(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, GuiEventType eventType);

	<T extends IGuiDialog> T dialogCreate(Class<T> dialogClass, UserSession session, IGuiScene scene, DialogIdentity dialogId, String title,
			String dialogContent);

	void dialogCreate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, DialogIdentity dialogId, String dialogContent);

	void dialogOpen(UserSession session, StageIdentity stageId, SceneIdentity sceneId, DialogIdentity dialogId);

	void dialogClose(UserSession session, StageIdentity stageId, SceneIdentity sceneId, DialogIdentity dialogId);

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
	void removeElement(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId);

	/**
	 * clear the content of an element, but leave it present
	 *
	 * @param session
	 * @param stageId
	 * @param sceneId
	 * @param elementId
	 */
	void elementClear(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId);

	void elementSetDisabled(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, boolean value);

	void elementSetLoading(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, boolean value);

	void elementSetProperty(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementName, String propertyName, Object value);

	void elementAddClass(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementName, String className);

	void elementRemoveClass(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementName, String className);

	void setTitle(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String text);

	void textSetValue(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String id, String text);

	void diagramCreate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String jsonDiagramData);

	void diagramUpdate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String jsonDiagramData);

	void tableAddColumn(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId, String colHeaderContent,
			String rowTemplateCellContent, String existingRowCellContent);

	void tableClearAllColumnHeaders(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String elementId);

	void tableAppendRow(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String tableId, Map<String, Object> rowData);

	void tableRemoveRow(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String tableId, String rowId);

	void tableClearAllRows(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String tableId);

	void editorCreate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String initialContent, String languageId);

	void updateParseTree(final UserSession session, final StageIdentity stageId, final SceneIdentity sceneId, final String editorId,
			final String jsonParseTreeData);

	void chartCreate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String chartId, String chartType, String jsonChartData,
			String jsonChartOptions);

	<X, Y> void chartAddDataItem(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String chartId, String seriesName, X x, Y y);

	void graphCreate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String jsonGraphData);

	void graphUpdate(UserSession session, StageIdentity stageId, SceneIdentity sceneId, String parentId, String jsonGraphData);

}
