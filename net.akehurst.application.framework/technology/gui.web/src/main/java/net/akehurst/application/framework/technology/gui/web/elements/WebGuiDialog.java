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
import net.akehurst.application.framework.technology.interfaceGui.elements.IGuiElement;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class WebGuiDialog extends WebGuiScene implements IGuiDialog {

    public WebGuiDialog(final String afId, final IGuiRequest guiRequest, final IGuiScene scene, final DialogIdentity dialogId, final String dialogContent) {
        super(afId, guiRequest, scene.getStageId(), scene.getSceneId());
        this.scene = scene;
        this.dialogId = dialogId;
        this.dialogElementPrefix = this.dialogId.asPrimitive() + "_";
        this.prefixLength = this.dialogElementPrefix.length();
        this.dialogContent = dialogContent;
    }

    private static final String[] PROPERTIES_TO_UNPREFIX_ARR = { "afRowId", "afDeselected", "afSelected" };
    private static final List<String> PROPERTIES_TO_UNPREFIX = Arrays.asList(WebGuiDialog.PROPERTIES_TO_UNPREFIX_ARR);
    private static final String[] PROPERTIES_TO_PREFIX_ARR = { "id", "data-ref", "for", "list", "name" };
    private static final List<String> PROPERTIES_TO_PREFIX = Arrays.asList(WebGuiDialog.PROPERTIES_TO_PREFIX_ARR);

    @ServiceReference
    ILogger logger;

    private final IGuiScene scene;
    private final DialogIdentity dialogId;
    private final String dialogElementPrefix;
    private final int prefixLength;
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

        final Map<String, Object> ed = event.getEventData();
        final Map<String, Object> newEventData = this.removePrefix(ed);

        final GuiEvent newEvent = new GuiEvent(event.getSession(), event.getSignature(), newEventData);
        super.notifyEventOccured(newEvent);
    }

    @Override
    public IGuiElement getElement(final String elementId) {
        return new WebGuiElement(this.guiRequest, this.scene, this, this.dialogElementPrefix + elementId);
    }

    private Object removePrefix(final Object value) {
        if (value instanceof List<?>) {
            final List<Object> oldList = (List<Object>) value;
            final List<Object> newList = this.removePrefix(oldList);
            return newList;
        } else if (value instanceof Map<?, ?>) {
            final Map<String, Object> mapV = (Map<String, Object>) value;
            final Map<String, Object> newMap = this.removePrefix(mapV);
            return newMap;
        } else if (value instanceof String) {
            final String strVal = (String) value;
            if (strVal.startsWith(this.dialogElementPrefix)) {
                final String newValue = strVal.substring(this.prefixLength);
                return newValue;
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    private Map<String, Object> removePrefix(final Map<String, Object> map) {
        if (map.size() == 3 && map.containsKey("afHeaders") && map.containsKey("afRowIds") && map.containsKey("afRows")) {
            final Map<String, Object> newMapData = this.removePrefixFromTableData(map);
            return newMapData;
        } else {
            final Map<String, Object> result = new HashMap<>();
            for (final Map.Entry<String, Object> me : map.entrySet()) {
                final String key = me.getKey();
                if (WebGuiDialog.PROPERTIES_TO_UNPREFIX.contains(key)) {
                    final String newValue = me.getValue().toString().substring(this.prefixLength);
                    result.put(key, newValue);
                } else if (key.startsWith(this.dialogElementPrefix)) {
                    final String newKey = key.substring(this.prefixLength);
                    final Object value = me.getValue();
                    final Object newValue = this.removePrefix(value);
                    result.put(newKey, newValue);
                } else {
                    // should not be any other event data for a dialog! I think
                }
            }
            return result;
        }
    }

    private List<Object> removePrefix(final List<Object> list) {
        final ArrayList<Object> result = new ArrayList<>();
        for (final Object lv : list) {
            final Object newValue = this.removePrefix(lv);
            result.add(newValue);
        }
        return result;
    }

    Map<String, Object> removePrefixFromTableData(final Map<String, Object> tableData) {
        // data from a table
        final Map<String, Object> result = new HashMap<>();
        final List<String> headers = (List<String>) tableData.get("afHeaders");
        final List<String> newHeaders = new ArrayList<>();
        for (final String h : headers) {
            if (h.startsWith(this.dialogElementPrefix)) {
                final String newH = h.substring(this.prefixLength);
                newHeaders.add(newH);
            } else {
                newHeaders.add(h);
            }
        }

        final List<String> rowIds = (List<String>) tableData.get("afRowIds");
        final List<String> newRowIds = new ArrayList<>();
        for (final String rId : rowIds) {
            if (rId.startsWith(this.dialogElementPrefix)) {
                final String newId = rId.substring(this.prefixLength);
                newRowIds.add(newId);
            } else {
                newRowIds.add(rId);
            }
        }

        final List<List<Object>> rows = (List<List<Object>>) tableData.get("afRows");
        final List<List<Object>> newRows = new ArrayList<>();
        for (final List<Object> row : rows) {
            newRows.add(this.removePrefix(row));
        }
        result.put("afHeaders", newHeaders);
        result.put("afRowIds", newRowIds);
        result.put("afRows", newRows);
        return result;
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
                        if (WebGuiDialog.PROPERTIES_TO_PREFIX.contains(localName)) {
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
