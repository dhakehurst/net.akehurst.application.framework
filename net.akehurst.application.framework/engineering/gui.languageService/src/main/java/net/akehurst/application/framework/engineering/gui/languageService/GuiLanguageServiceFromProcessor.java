package net.akehurst.application.framework.engineering.gui.languageService;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiLanguageService;
import net.akehurst.application.framework.technology.interfaceGui.data.editor.IGuiSyntaxHighlightDefinition;
import net.akehurst.language.core.ILanguageProcessor;
import net.akehurst.language.core.parser.IParseTree;
import net.akehurst.language.core.parser.ParseFailedException;
import net.akehurst.language.core.parser.ParseTreeException;
import net.akehurst.language.core.parser.RuleNotFoundException;

public class GuiLanguageServiceFromProcessor implements IGuiLanguageService {

	public GuiLanguageServiceFromProcessor(final ILanguageProcessor processor) {
		this.processor = processor;
	}

	final ILanguageProcessor processor;

	@Override
	public String getIdentity() {
		return this.processor.getGrammar().getName();
	}

	@Deprecated
	@Override
	public List<IGuiSyntaxHighlightDefinition> getSyntaxHighlighting() {
		return new ArrayList<>();
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub

	}

	@Override
	public void revert() {
		// TODO Auto-generated method stub

	}

	@Override
	public void save() {
		// TODO Auto-generated method stub

	}

	@Override
	public String update(final String text) {
		try {
			final Reader reader = new StringReader(text);
			System.out.println("parsing " + text);
			final IParseTree pt = this.processor.getParser().parse("element", reader);
			System.out.println("parse success " + pt.getRoot().getName());
			final ToJsonVisitor visitor = new ToJsonVisitor();
			final JsonObject json = pt.accept(visitor, null);
			return json.toString();

		} catch (ParseFailedException | ParseTreeException | RuleNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Json.createObjectBuilder().add("error", "error").build().toString();
	}

	@Override
	public void assist() {
		// TODO Auto-generated method stub

	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hover() {
		// TODO Auto-generated method stub

	}

	@Override
	public void highlight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void occurrences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void format() {
		// TODO Auto-generated method stub

	}

}
