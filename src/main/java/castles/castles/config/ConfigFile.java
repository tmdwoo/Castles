package castles.castles.config;

import castles.castles.localization.Phrase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

import static castles.castles.Castles.plugin;

public class ConfigFile extends Config {

    public static final String CONFIG = "config.yml";
    private static final TreeMap<String, String> DEFAULT_VALUES = new TreeMap<>();
    private static final String DEFAULT_FILE_HEADER = "# CoreProtect Language File (en)";
    private final HashMap<String, String> lang;

    public static void init(String fileName) throws IOException {
        for (Phrase phrase : Phrase.values()) {
            DEFAULT_VALUES.put(phrase.name(), phrase.getPhrase());
        }

        loadFiles(fileName);
    }

    public ConfigFile() {
        this.lang = new LinkedHashMap<>();
    }

    public void load(final InputStream in, String fileName) throws IOException {
        // if we fail reading, we will not corrupt our current config.
        final Map<String, String> newConfig = new LinkedHashMap<>(this.lang.size());
        ConfigFile.load(in, newConfig, true);

        this.lang.clear();
        this.lang.putAll(newConfig);

        for (final Entry<String, String> entry : this.lang.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
        }
    }

    // this function will close in
    public static void load(final InputStream in, final Map<String, String> config, boolean forceCase) throws IOException {
        try (final InputStream in0 = in) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("#")) {
                    continue;
                }

                final int split = line.indexOf(':');

                if (split == -1) {
                    continue;
                }

                String key = line.substring(0, split).trim();
                String value = line.substring(split + 1).trim();

                // Strip out single and double quotes from the start/end of the value
                if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
                    value = value.replaceAll("^'|'$", "");
                    value = value.replace("''", "'");
                    value = value.replace("\\'", "'");
                    value = value.replace("\\\\", "\\");
                }
                else if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.replaceAll("^\"|\"$", "");
                    value = value.replace("\\\"", "\"");
                    value = value.replace("\\\\", "\\");
                }

                if (forceCase) {
                    key = key.toUpperCase(Locale.ROOT);
                }
                config.put(key, value);
            }

            reader.close();
        }
    }

    private static Map<String, byte[]> loadFiles(String fileName) throws IOException {
        final File configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFolder.mkdirs();
        }

        final Map<String, byte[]> map = new HashMap<>();
        final File globalFile = new File(configFolder, fileName);

        if (globalFile.exists()) {
            // we always add options to the global config
            final byte[] data = Files.readAllBytes(globalFile.toPath());
            map.put("config", data);

            final ConfigFile temp = new ConfigFile();
            temp.load(new ByteArrayInputStream(data), fileName);
            temp.addMissingOptions(globalFile);
        }
        else {
            final ConfigFile temp = new ConfigFile();
            temp.addMissingOptions(globalFile);
        }

        return map;
    }

    @Override
    public void addMissingOptions(final File file) throws IOException {
        if (file.getName().startsWith(".")) {
            return;
        }

        final boolean writeHeader = !file.exists() || file.length() == 0;
        try (final FileOutputStream fout = new FileOutputStream(file, true)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            if (writeHeader) {
                out.append(DEFAULT_FILE_HEADER);
                out.append(Config.LINE_SEPARATOR);
            }

            for (final Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
                final String key = entry.getKey();
                final String defaultValue = entry.getValue().replaceAll("\"", "\\\\\"");

                final String configuredValue = this.lang.get(key);
                if (configuredValue != null) {
                    continue;
                }

                out.append(Config.LINE_SEPARATOR);
                out.append(key);
                out.append(": ");
                out.append("\"").append(defaultValue).append("\"");
            }

            out.close();
        }
    }
}