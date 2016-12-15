package net.akehurst.application.framework.technology.gui.vertx.elements;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import net.akehurst.application.framework.technology.interfaceGui.IGuiDialog;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.IGuiScene;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class VertxGuiDialog extends VertxGuiScene implements IGuiDialog {

	public VertxGuiDialog(final String afId, final IGuiRequest guiRequest, final IGuiScene scene, final String dialogId, final String dialogContent) {
		super(afId, guiRequest, scene.getStageId(), scene.getSceneId());
		this.scene = scene;
		this.dialogId = dialogId;
		this.dialogContent = dialogContent;
	}

	@ServiceReference
	ILogger logger;

	IGuiScene scene;
	String dialogId;
	String dialogContent;

	@Override
	public void show(final UserSession session) {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream(this.dialogContent + ".html");
		// final Scanner s = new Scanner(is);
		// String content = "";
		// while (s.hasNextLine()) {
		// content += s.nextLine();
		// }
		// s.close();
		final String content = this.readAndPrefixIds(is);
		this.guiRequest.showDialog(session, this.stageId, this.sceneId, this.dialogId, content);
	}

	@Override
	public void close(final UserSession session) {
		this.guiRequest.removeElement(session, this.stageId, this.sceneId, this.dialogId);
	}

	// TODO: create own set of event handlers, must unregister them when dialog closes

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
						if ("id".equals(localName) || "for".equals(localName)) {
							final String value = this.dialogId + "_" + att.getValue();
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
