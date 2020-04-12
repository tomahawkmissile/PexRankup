package com.gmail.tomahawkmissile2.pexrankup;

import java.io.File;
import java.util.List;

public class Config {
	static YamlManager manager = new YamlManager(new File(Main.plugin.getDataFolder()+"/config.yml"));
	
	public synchronized static void set(String path, Object value) {
		manager.writeYaml(path, value);
	}
	public synchronized static Object get(String path) {
		return manager.readYaml(path);
	}
	public synchronized static List<String> getStringList(String path) {
		return manager.readStringList(path);
	}
	public synchronized static List<String> getSectionHeaders(String path) {
		return manager.readSectionHeaders(path);
	}
}
