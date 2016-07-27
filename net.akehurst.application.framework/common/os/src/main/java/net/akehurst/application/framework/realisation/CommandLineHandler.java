package net.akehurst.application.framework.realisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLineHandler implements ICommandLineHandler {

	static class CommandLineArgumentValue {
		CommandLineArgumentValue(String name, String value) {
			this.name = name;
			this.value = value;
		}
		String name;
		String value;
	}
	
	public CommandLineHandler(String groupRegEx, String argumentStart) {
		this.groupRegEx = groupRegEx;
		this.argumentStart = argumentStart;
		this.definedArgs = new HashMap<>();
		this.defineGroup("");
		this.groupArgs = new HashMap<>();
		// add default group
		this.groupArgs.put("", new HashMap<>());
	}
	
	String groupRegEx;
	String argumentStart;
	Map<String,List<ArgumentDefinition>> definedArgs;
	
	Map<String, Map<String,String>> groupArgs;
	
	@Override
	public void defineGroup(String groupName) {
		this.definedArgs.put(groupName, new ArrayList<>());
	}

	@Override
	public void defineArgument(String group, String name, Class<?> type, boolean required, boolean hasValue, Object defaultValue, String description) {
		List<ArgumentDefinition> args = this.definedArgs.get(group);
		args.add(new ArgumentDefinition(group, name, type, required, hasValue, defaultValue, description));
	}

	@Override
	public String getHelp() {
		String result = "";
		for(String grp:this.definedArgs.keySet()) {
			result += (grp.isEmpty() ? "<default>": grp) + System.lineSeparator();
			List<ArgumentDefinition> da = this.definedArgs.get(grp);
			for(ArgumentDefinition ad:da) {
				result += "  " + ad.getName() + " : " + ad.getType().getSimpleName() + (ad.getRequired() ? " [1] " : " [?] ") + "(" + (null==ad.getDefaultValue()?"":ad.getDefaultValue()) + ") - " + ad.getDescription();
				result += System.lineSeparator();
			}
			result += System.lineSeparator();
		}
		return result;
	}
	
	@Override
	public void parse(String[] args) {
		//default group if nothing else parsed
		String currentGroup = ""; 
		for(String arg: args) {
			if (arg.startsWith(this.argumentStart)) {
				this.parseArgument(currentGroup, arg.substring(argumentStart.length()));
			} else if (arg.matches(this.groupRegEx)){
				currentGroup = arg;
				groupArgs.put(currentGroup, new HashMap<>());
			} else {
				throw new RuntimeException("Command line argument doesn't match expected pattern "+arg);
			}
		}
	}

	
	private void parseArgument(String currentGroup, String arg) {
		String[] split = arg.split("=");
		Map<String,String> groupArgs = this.groupArgs.get(currentGroup);
		if (null==groupArgs) {
			//internal error group must have been parsed before an argument
		} else {
			if (split.length > 1) {
				//has a value
				groupArgs.put(split[0], split[1]);
			} else {
				// null indicates presence
				groupArgs.put(split[0], null);
			}
		}
	}
	
	@Override
	public boolean hasGroup(String group) {
		return this.groupArgs.containsKey(group);
	}
	
	@Override
	public String getArgumentValue(String group, String name) {
		Map<String,String> groupArgs = this.groupArgs.get(group);
		if (null==groupArgs) {
			return null;
		} else {
			return groupArgs.get(name);
		}
	}

	@Override
	public boolean hasArgument(String group, String name) {
		Map<String,String> groupArgs = this.groupArgs.get(group);
		if (null==groupArgs) {
			return false;
		} else {
			return groupArgs.containsKey(name);
		}
	}

}
