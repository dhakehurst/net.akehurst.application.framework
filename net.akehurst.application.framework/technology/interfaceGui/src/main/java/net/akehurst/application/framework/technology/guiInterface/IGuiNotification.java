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
package net.akehurst.application.framework.technology.guiInterface;

import java.util.Map;

import net.akehurst.application.framework.technology.authentication.TechSession;


public interface IGuiNotification {

	void notifyReady();
		
	void notifyEventOccured(TechSession session, String sceneId, String elementId, String eventType, Map<String, Object> data);
	
	void notifyDowloadRequest(TechSession session, String filename, IGuiCallback callback);

	void notifyUpload(TechSession session, String filename);
}
