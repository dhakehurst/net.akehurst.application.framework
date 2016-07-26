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
	
	public CommandLineHandler() {
		this.definedGroups = new ArrayList<>();
		this.groupArgs = new HashMap<>();
		// add default group
		this.groupArgs.put("", new HashMap<>());
	}
	
	List<String> definedGroups;
	Map<String, Map<String,String>> groupArgs;
	
	@Override
	public void defineGroup(String groupName) {
		this.definedGroups.add(groupName);
	}

	@Override
	public void defineArgument(String group, String name, boolean required, boolean hasValue, Object defaultValue) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void parse(String[] args) {
		//default group if nothing else parsed
		String currentGroup = ""; 
		for(String arg: args) {
			if (arg.startsWith("-")) {
				this.parseArgument(currentGroup, arg.substring(1));
			} else {
				//TODO: maybe check format of group name ? should it be constrained?
				currentGroup = arg;
				groupArgs.put(currentGroup, new HashMap<>());
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
