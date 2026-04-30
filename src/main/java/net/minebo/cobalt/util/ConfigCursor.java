package net.minebo.cobalt.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class ConfigCursor {

	private final FileConfig fileConfig;
	@Setter
	private String path;

	public boolean exists() {
		return exists(null);
	}

	public boolean exists(String subPath) {
		String fullPath = buildPath(subPath);
		return fileConfig.get(fullPath) != null;
	}

	public Set<String> getKeys() {
		return getKeys(null);
	}

	public Set<String> getKeys(String subPath) {
		String fullPath = buildPath(subPath);
		Object value = fileConfig.get(fullPath);

		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> section = (Map<String, Object>) value;
			return section.keySet();
		}
		return Collections.emptySet();
	}

	public String getString(String subPath) {
		return fileConfig.get(buildPath(subPath), "");
	}

	public boolean getBoolean(String subPath) {
		return fileConfig.get(buildPath(subPath), false);
	}

	public int getInt(String subPath) {
		return fileConfig.get(buildPath(subPath), 0);
	}

	public long getLong(String subPath) {
		return fileConfig.get(buildPath(subPath), 0L);
	}

	public double getDouble(String subPath) {
		return fileConfig.get(buildPath(subPath), 0.0);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String subPath) {
		Object value = fileConfig.get(buildPath(subPath));
		if (value instanceof List) {
			return ((List<?>) value).stream()
					.map(String::valueOf)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public UUID getUuid(String subPath) {
		String uuidStr = getString(subPath);
		try {
			return UUID.fromString(uuidStr);
		} catch (Exception e) {
			return null;
		}
	}

	// Bukkit World removed - you can re-add if needed with your own world manager
	// public World getWorld(String subPath) { ... }

	public void set(Object value) {
		set(null, value);
	}

	public void set(String subPath, Object value) {
		String fullPath = buildPath(subPath);
		fileConfig.set(fullPath, value);
	}

	public void save() {
		fileConfig.save();
	}

	private String buildPath(String subPath) {
		if (path == null || path.isEmpty()) {
			return subPath == null || subPath.isEmpty() ? "" : subPath;
		}
		if (subPath == null || subPath.isEmpty()) {
			return path;
		}
		return path + "." + subPath;
	}
}