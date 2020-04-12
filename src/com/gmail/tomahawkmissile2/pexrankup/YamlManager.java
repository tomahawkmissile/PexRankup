package com.gmail.tomahawkmissile2.pexrankup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class YamlManager {
	private File f;
	private YamlConfiguration y;
	
	public YamlManager(File f) {
		y=YamlConfiguration.loadConfiguration(f);
		this.f=f;
	}
	public void writeYaml(String path,Object value) {
		y.set(path, value);
		try {
			y.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Object readYaml(String path) {
		if(y.get(path)==null) {
			return null;
		}
		return y.get(path);
	}
	public Object[] readKeys() {
		return y.getKeys(true).toArray();
	}
	public void createSection(String path) {
		y.createSection(path);
	}
	public List<String> readStringList(String path) {
		return y.getStringList(path);
	}
	public List<String> readSectionHeaders(String path) {
		List<String> ret = new ArrayList<String>();
		ret.addAll(y.getConfigurationSection(path).getKeys(false));
		return ret;
	}
}
