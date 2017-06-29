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
package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.technology.interfaceGui.DialogIdentity;
import net.akehurst.application.framework.technology.interfaceGui.GuiEvent;
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class VertxGuiDialog extends VertxGuiScene implements IGuiDialog {

	public VertxGuiDialog(final String afId, final IGuiRequest guiRequest, final IGuiScene scene, final DialogIdentity dialogId, final String dialogContent) {
		super(afId, guiRequest, scene.getStageId(), scene.getSceneId());
		// this.scene = scene;
		this.dialogId = dialogId;
		this.dialogElementPrefix = this.dialogId.asPrimitive() + "_";
		this.dialogContent = dialogContent;
	}

	private static final String[] PROPERTIES_TO_PREFIX_ARR = { "id", "data-ref", "for", "list" };
	private static final List<String> PROPERTIES_TO_PREFIX = Arrays.asList(VertxGuiDialog.PROPERTIES_TO_PREFIX_ARR);

	@ServiceReference
	ILogger logger;

	// private final IGuiScene scene;
	private final DialogIdentity dialogId;
	private final String dialogElementPrefix;
	private final String dialogContent;

	@Override
	public DialogIdentity getId() {
		return this.dialogId;
	}

	@Override
	public void create(final UserSession session) {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream(this.dialogContent + ".html");
		final String content = this.readAndPrefixIds(is);
		this.getGuiRequest().dialogCreate(session, this.stageId, this.sceneId, this.getId(), content);
	}

	@Override
	public void open(final UserSession session) {
		this.getGuiRequest().dialogOpen(session, this.stageId, this.sceneId, this.dialogId);
	}

	@Override
	public void close(final UserSession session) {
		this.getGuiRequest().dialogClose(session, this.stageId, this.sceneId, this.dialogId);
	}

	// @Override
	// public void delete(final UserSession session) {
	// this.getGuiRequest().dialogDelete(session, this.stageId, this.sceneId, this.dialogId.asPrimitive());
	// }

	// TODO: create own set of event handlers, must unregister them when dialog closes

	@Override
	public void notifyEventOccured(final GuiEvent event) {
		// remove the dialog prefix from event data
		final int len = this.dialogElementPrefix.length();
		final Map<String, Object> ed = event.getEventData();
		final Map<String, Object> newEventData = new HashMap<>();
		for (final Map.Entry<String, Object> me : ed.entrySet()) {
			final String key = me.getKey();
			if ("afRowId".equals(key)) {
				final String newValue = me.getValue().toString().substring(len);
				newEventData.put(key, newValue);
			} else if (key.startsWith(this.dialogElementPrefix)) {
				final String newKey = key.substring(len);
				final Object value = me.getValue();
				if (value instanceof List) {
					final ArrayList<Object> newList = new ArrayList<>();
					for (final Object lv : (List<Object>) value) {
						if (lv instanceof Map) {
							final Map<String, Object> newMap = new HashMap<>();
							for (final Map.Entry<String, Object> lvme : ((Map<String, Object>) lv).entrySet()) {
								final String newMapKey = lvme.getKey().substring(len);
								newMap.put(newMapKey, lvme.getValue());
							}
							newList.add(newMap);
						} else {
							newList.add(lv);
						}
					}
					newEventData.put(newKey, newList);
				} else {
					newEventData.put(newKey, value);
				}
			} else {
				// should not be any other event data for a dialog! I think
			}
		}
		final GuiEvent newEvent = new GuiEvent(event.getSession(), event.getSignature(), newEventData);
		super.notifyEventOccured(newEvent);
	}

	private String readAndPrefixIds(final InputStream inStream) {
		final StringWriter result = new StringWriter();

		try {
			final XMLOutputFactory output = XMLOutputFactory.newInstance();
			final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
			final XMLEventWriter writer = output.createXMLEventWriter(result);

			final XMLInputFactory factory = XMLInputFactory.newInstance();
			final XMLEventReader reader = factory.createXMLEventReader(inStream);

			while (reader.hasNext()) {
				final XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					final StartElement element = event.asStartElement();
					final Iterator<Attribute> itt = element.getAttributes();
					final List<Attribute> newAtts = new ArrayList<>();
					while (itt.hasNext()) {
						final Attribute att = itt.next();
						final String localName = att.getName().getLocalPart();
						if (VertxGuiDialog.PROPERTIES_TO_PREFIX.contains(localName)) {
							final String value = this.dialogElementPrefix + att.getValue();
							final Attribute newAtt = eventFactory.createAttribute(localName, value);
							newAtts.add(newAtt);
						} else {
							newAtts.add(att);
						}
					}

					final StartElement newStEl = eventFactory.createStartElement(element.getName(), newAtts.iterator(), element.getNamespaces());
					writer.add(newStEl);

				} else {
					writer.add(event);
				}
			}
			writer.flush();
			writer.close();
		} catch (FactoryConfigurationError | XMLStreamException e) {
			this.logger.log(LogLevel.ERROR, "Exception trying to create GuiDialog");
			this.logger.log(LogLevel.ERROR, e.getMessage());
		}

		return result.toString();
	}
}
