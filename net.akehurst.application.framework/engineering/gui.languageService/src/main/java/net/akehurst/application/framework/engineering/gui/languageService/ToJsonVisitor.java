package net.akehurst.application.framework.engineering.gui.languageService;

import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import net.akehurst.language.core.parser.IBranch;
import net.akehurst.language.core.parser.ILeaf;
import net.akehurst.language.core.parser.INode;
import net.akehurst.language.core.parser.IParseTree;
import net.akehurst.language.core.parser.IParseTreeVisitor;

public class ToJsonVisitor implements IParseTreeVisitor<JsonObject, JsonObject, RuntimeException> {

	@Override
	public JsonObject visit(final IParseTree target, final JsonObject arg) throws RuntimeException {
		return target.getRoot().accept(this, null);
	}

	// TODO: reverse the 'isPattern' into something like 'canBeUsedAsClassifier' or 'isValidClassifier'

	@Override
	public JsonObject visit(final ILeaf target, final JsonObject arg) throws RuntimeException {
		final JsonObjectBuilder builder = Json.createObjectBuilder();
		final String name = target.getName();
		boolean isPattern = false;
		if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", name) && !name.contains("$")) {
			// ok use name
		} else {
			// if pattern or parser generated node..mark as a pattern
			isPattern = true;
		}

		builder.add("name", target.getName());
		builder.add("start", target.getStartPosition());
		builder.add("length", target.getMatchedTextLength());
		builder.add("isPattern", isPattern);
		return builder.build();
	}

	@Override
	public JsonObject visit(final IBranch target, final JsonObject arg) throws RuntimeException {
		final JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("name", target.getName());
		builder.add("start", target.getStartPosition());
		builder.add("length", target.getMatchedTextLength());
		final String name = target.getName();
		boolean isPattern = false;
		if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", name) && !name.contains("$")) {
			// ok use name
		} else {
			// if pattern or parser generated node..mark as a pattern
			isPattern = true;
		}

		builder.add("isPattern", isPattern);
		final JsonArrayBuilder ab = Json.createArrayBuilder();
		for (final INode n : target.getChildren()) {
			final JsonObject nobj = n.accept(this, null);
			ab.add(nobj);
		}
		final JsonArray array = ab.build();
		builder.add("children", array);

		return builder.build();
	}

}
