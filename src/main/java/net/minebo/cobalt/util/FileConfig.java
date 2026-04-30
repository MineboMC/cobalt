package net.minebo.cobalt.util;

import lombok.Getter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class FileConfig {

	private final File file;
	private Map<String, Object> config = new LinkedHashMap<>();

	public FileConfig(File dataFolder, String fileName) {
		this.file = new File(dataFolder, fileName);
		init();
	}

	public FileConfig(File file) {
		this.file = file;
		init();
	}

	private void init() {
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();

				// Copy from resources if available (inside JAR)
				InputStream resource = getClass().getClassLoader().getResourceAsStream(file.getName());
				if (resource != null) {
					Files.copy(resource, file.toPath());
					resource.close();
				} else {
					file.createNewFile();
				}
			}

			load();
		} catch (IOException e) {
			System.err.println("Failed to initialize config file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void load() {
		try (FileInputStream fis = new FileInputStream(file)) {
			Yaml yaml = createYaml();
			Object loaded = yaml.load(fis);
			if (loaded instanceof Map) {
				this.config = (Map<String, Object>) loaded;
			} else {
				this.config = new LinkedHashMap<>();
			}
		} catch (IOException e) {
			System.err.println("Could not load config file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			file.getParentFile().mkdirs();
			Yaml yaml = createYaml();

			try (Writer writer = new FileWriter(file)) {
				yaml.dump(config, writer);
			}
		} catch (IOException e) {
			System.err.println("Could not save config file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	private Yaml createYaml() {
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		dumperOptions.setPrettyFlow(true);
		dumperOptions.setIndent(2);

		LoaderOptions loaderOptions = new LoaderOptions();

		Representer representer = new Representer(dumperOptions);
		representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		// SafeConstructor is required in SnakeYAML 2.0+
		return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions);
	}

	public Object get(String path) {
		return get(path, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String path, T def) {
		String[] keys = path.split("\\.");
		Map<String, Object> current = config;

		for (int i = 0; i < keys.length; i++) {
			Object value = current.get(keys[i]);
			if (value == null) return def;
			if (i == keys.length - 1) {
				return (T) value;
			}
			if (value instanceof Map) {
				current = (Map<String, Object>) value;
			} else {
				return def;
			}
		}
		return def;
	}

	public void set(String path, Object value) {
		String[] keys = path.split("\\.");
		Map<String, Object> current = config;

		for (int i = 0; i < keys.length - 1; i++) {
			current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new LinkedHashMap<>());
		}
		current.put(keys[keys.length - 1], value);
	}
}