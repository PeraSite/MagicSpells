package com.nisovin.magicspells.variables.variabletypes;

import com.nisovin.magicspells.variables.Variable;

import org.bukkit.configuration.ConfigurationSection;

public class GlobalStringVariable extends Variable {

	private String value;

	@Override
	public void loadExtraData(ConfigurationSection section) {
		super.loadExtraData(section);

		value = section.getString("default-value", "");
	}

	@Override
	protected void init() {

	}

	@Override
	public void set(String player, double amount) {

	}

	@Override
	public double getValue(String player) {
		return 0;
	}

	@Override
	public String getStringValue(String player) {
		return value;
	}

	@Override
	public void parseAndSet(String player, String textValue) {
		value = textValue;
	}

	@Override
	public void reset(String player) {
		value = "";
	}

}
