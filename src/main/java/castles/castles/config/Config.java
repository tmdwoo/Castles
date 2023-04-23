package castles.castles.config;

import castles.castles.scheduler.Scheduler;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static castles.castles.Castles.plugin;


public class Config {
    private static final Map<String, String[]> HEADERS = new HashMap<>();
    private static final Map<String, String> DEFAULT_VALUES = new LinkedHashMap<>();
    private static final String DEFAULT_FILE_HEADER = "#Castles Configuration File";
    public static final String LINE_SEPARATOR = "\n";

    private static final Config GLOBAL = new Config();
    private final HashMap<String, String> config;
    private Config defaults;

    public int MINIMUM_CASTLE_X;
    public int MINIMUM_CASTLE_Z;
    public int MAXIMUM_CASTLE_X;
    public int MAXIMUM_CASTLE_Z;
    public String STATE;
    public double WAR_TIME;
    public double PEACEFUL_TIME;
    public double PREPARATION_TIME;
    public int BP_PER_KILL;
    public int BP_MAX_PER_VICTIM;
    public int BP_PER_OCCUPY;
    public int BP_PER_CASTLE;
    public int TELEPORT_COST;
    public int BACK_COST;
    public int TELEPORT_COOLDOWN;
    public int TELEPORT_WARMUP;
    public String DISCORD_TOKEN;
    public String DISCORD_GUILD_ID;

    static {
        DEFAULT_VALUES.put("MINIMUM_CASTLE_X", "-10000");
        DEFAULT_VALUES.put("MINIMUM_CASTLE_Z", "-10000");
        DEFAULT_VALUES.put("MAXIMUM_CASTLE_X", "10000");
        DEFAULT_VALUES.put("MAXIMUM_CASTLE_Z", "10000");
        DEFAULT_VALUES.put("STATE", "PEACEFUL");
        DEFAULT_VALUES.put("WAR_TIME", "16.0");
        DEFAULT_VALUES.put("PEACEFUL_TIME", "18.0");
        DEFAULT_VALUES.put("PREPARATION_TIME", "12.0");
        DEFAULT_VALUES.put("BP_PER_KILL", "1");
        DEFAULT_VALUES.put("BP_MAX_PER_VICTIM", "10");
        DEFAULT_VALUES.put("BP_PER_OCCUPY", "30");
        DEFAULT_VALUES.put("BP_PER_CASTLE", "10");
        DEFAULT_VALUES.put("TELEPORT_COST", "0");
        DEFAULT_VALUES.put("BACK_COST", "2");
        DEFAULT_VALUES.put("TELEPORT_COOLDOWN", "30");
        DEFAULT_VALUES.put("TELEPORT_WARMUP", "5");
        DEFAULT_VALUES.put("DISCORD_TOKEN", "null");
        DEFAULT_VALUES.put("DISCORD_GUILD_ID", "null");

        HEADERS.put("MINIMUM_CASTLE_X", new String[] {
                "# The minimum X coordinate of a castle.",
                "# Cannot create a castle with a lower X coordinate than this."
        });
        HEADERS.put("MINIMUM_CASTLE_Z", new String[] {
                "# The minimum Z coordinate of a castle.",
                "# Cannot create a castle with a lower Z coordinate than this."
        });
        HEADERS.put("MAXIMUM_CASTLE_X", new String[] {
                "# The maximum X coordinate of a castle.",
                "# Cannot create a castle with this X coordinate or higher."
        });
        HEADERS.put("MAXIMUM_CASTLE_Z", new String[] {
                "# The maximum Z coordinate of a castle.",
                "# Cannot create a castle with this Z coordinate or higher."
        });
        HEADERS.put("STATE", new String[] {
                "# The current state of the server.",
                "# Can be PEACEFUL, PREPARATION, or WAR."
        });
        HEADERS.put("WAR_TIME", new String[] {
                "# The time of day when the server enters the WAR state.",
                "# Must be between 0.0 and 24.0."
        });
        HEADERS.put("PEACEFUL_TIME", new String[] {
                "# The time of day when the server enters the PEACEFUL state.",
                "# Must be between 0.0 and 24.0."
        });
        HEADERS.put("PREPARATION_TIME", new String[] {
                "# The time of day when the server enters the PREPARATION state.",
                "# Must be between 0.0 and 24.0."
        });
        HEADERS.put("BP_PER_KILL", new String[] {
                "# The amount of blood points a player gets for killing another player."
        });
        HEADERS.put("BP_MAX_PER_VICTIM", new String[] {
                "# The maximum amount of blood points a player can get from killing another player.",
                "# zero for no limit."
        });
        HEADERS.put("BP_PER_OCCUPY", new String[] {
                "# The amount of blood points a player gets for occupying a castle."
        });
        HEADERS.put("BP_PER_CASTLE", new String[] {
                "# The amount of blood points a player gets for having a castle."
        });
        HEADERS.put("TELEPORT_COST", new String[] {
                "# The amount of blood points a player must pay to teleport to a castle."
        });
        HEADERS.put("BACK_COST", new String[] {
                "# The amount of blood points a player must pay to teleport back to their castle."
        });
        HEADERS.put("TELEPORT_COOLDOWN", new String[] {
                "# The amount of time in seconds a player must wait before teleporting again."
        });
        HEADERS.put("TELEPORT_WARMUP", new String[] {
                "# The amount of time in seconds a player must wait before teleporting."
        });
        HEADERS.put("DISCORD_TOKEN", new String[] {
                "# The token of the Discord bot."
        });
        HEADERS.put("DISCORD_GUILD_ID", new String[] {
                "# The ID of the Discord guild."
        });
    }

    private void readValues() {
        this.MINIMUM_CASTLE_X = this.getInt("MINIMUM_CASTLE_X",-10000);
        this.MINIMUM_CASTLE_Z = this.getInt("MINIMUM_CASTLE_Z",-10000);
        this.MAXIMUM_CASTLE_X = this.getInt("MAXIMUM_CASTLE_X",10000);
        this.MAXIMUM_CASTLE_Z = this.getInt("MAXIMUM_CASTLE_Z",10000);
        this.STATE = this.get("STATE","PEACEFUL");
        this.WAR_TIME = this.getDouble("WAR_TIME",16.0);
        this.PEACEFUL_TIME = this.getDouble("PEACEFUL_TIME",18.0);
        this.PREPARATION_TIME = this.getDouble("PREPARATION_TIME",12.0);
        this.BP_PER_KILL = this.getInt("BP_PER_KILL",1);
        this.BP_MAX_PER_VICTIM = this.getInt("BP_MAX_PER_VICTIM",10);
        this.BP_PER_OCCUPY = this.getInt("BP_PER_OCCUPY",30);
        this.BP_PER_CASTLE = this.getInt("BP_PER_CASTLE",10);
        this.TELEPORT_COST = this.getInt("TELEPORT_COST",0);
        this.BACK_COST = this.getInt("BACK_COST",2);
        this.TELEPORT_COOLDOWN = this.getInt("TELEPORT_COOLDOWN",30);
        this.TELEPORT_WARMUP = this.getInt("TELEPORT_WARMUP",5);
        this.DISCORD_TOKEN = this.get("DISCORD_TOKEN","null");
        this.DISCORD_GUILD_ID = this.get("DISCORD_GUILD_ID","null");
    }

    public static void init() throws IOException {
        parseConfig(loadFiles(ConfigFile.CONFIG));
    }

    public static Config getGlobal() {
        return GLOBAL;
    }

    public Config() {
        this.config = new LinkedHashMap<>();
    }

    public void setDefaults(final Config defaults) {
        this.defaults = defaults;
    }

    private String get(final String key, final String dfl) {
        String configured = this.config.get(key);
        if (configured == null) {
            if (dfl != null) {
                return dfl;
            }
            if (this.defaults == null) {
                configured = DEFAULT_VALUES.get(key);
            }
            else {
                configured = this.defaults.config.getOrDefault(key, DEFAULT_VALUES.get(key));
            }
        }
        return configured;
    }

    private boolean getBoolean(final String key) {
        final String configured = this.get(key, null);
        return configured != null && configured.startsWith("t");
    }

    private boolean getBoolean(final String key, final boolean dfl) {
        final String configured = this.get(key, null);
        return configured == null ? dfl : configured.startsWith("t");
    }

    private int getInt(final String key) {
        return this.getInt(key, 0);
    }

    private int getInt(final String key, final int dfl) {
        String configured = this.get(key, null);

        if (configured == null) {
            return dfl;
        }

        configured = configured.replaceAll("[^-0-9]", "");

        return configured.isEmpty() ? 0 : Integer.parseInt(configured);
    }

    private double getDouble(final String key) {
        return this.getDouble(key, 0.0);
    }

    private double getDouble(final String key, final double dfl) {
        String configured = this.get(key, null);

        if (configured == null) {
            return dfl;
        }

        configured = configured.replaceAll("[^-0-9.]", "");

        return configured.isEmpty() ? 0.0 : Double.parseDouble(configured);
    }

    private String getString(final String key) {
        final String configured = this.get(key, null);
        return configured == null ? "" : configured;
    }

    public void clearConfig() {
        this.config.clear();
    }

    public void setValue(final String key, final String value) {
        this.config.put(key, value);
    }

    public void loadDefaults() {
        this.clearConfig();
        this.readValues();
    }

    public void load(final InputStream in) throws IOException {
        // if we fail reading, we will not corrupt our current config.
        final Map<String, String> newConfig = new LinkedHashMap<>(this.config.size());
        ConfigFile.load(in, newConfig, false);

        this.clearConfig();
        this.config.putAll(newConfig);

        this.readValues();
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

            // can't modify GLOBAL, we're likely off-main here
            final Config temp = new Config();
            temp.load(new ByteArrayInputStream(data));
            temp.addMissingOptions(globalFile);
        }
        else {
            final Config temp = new Config();
            temp.loadDefaults();
            temp.addMissingOptions(globalFile);
        }

        return map;
    }

    // this should only be called on the main thread
    private static void parseConfig(final Map<String, byte[]> data) {
        if (!Bukkit.isPrimaryThread()) {
            // we call reloads asynchronously
            // for now this solution is good enough to ensure we only modify on the main thread
            final CompletableFuture<Void> complete = new CompletableFuture<>();

            Scheduler.scheduleSyncDelayedTask(() -> {
                try {
                    parseConfig(data);
                }
                catch (final Throwable thr) {
                    if (thr instanceof ThreadDeath) {
                        throw (ThreadDeath) thr;
                    }
                    complete.completeExceptionally(thr);
                    return;
                }
                complete.complete(null);
            }, 0);

            complete.join();
            return;
        }

        // we need to load global first since it is used for config defaults
        final byte[] defaultData = data.get("config");
        if (defaultData != null) {
            try {
                GLOBAL.load(new ByteArrayInputStream(defaultData));
            }
            catch (final IOException ex) {
                throw new RuntimeException(ex); // shouldn't happen
            }
        }
        else {
            GLOBAL.loadDefaults();
        }

        for (final Map.Entry<String, byte[]> entry : data.entrySet()) {
            final byte[] fileData = entry.getValue();
            final Config config = new Config();
            config.setDefaults(GLOBAL);

            try {
                config.load(new ByteArrayInputStream(fileData));
            }
            catch (final IOException ex) {
                throw new RuntimeException(ex); // shouldn't happen
            }
        }
    }

    public void addMissingOptions(final File file) throws IOException {
        final boolean writeHeader = !file.exists() || file.length() == 0;
        try (final FileOutputStream fout = new FileOutputStream(file, true)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            if (writeHeader) {
                out.append(DEFAULT_FILE_HEADER);
                out.append(LINE_SEPARATOR);
            }

            for (final Map.Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
                final String key = entry.getKey();
                final String defaultValue = entry.getValue();

                final String configuredValue = this.config.get(key);

                if (configuredValue != null) {
                    continue;
                }

                final String[] header = HEADERS.get(key);

                if (header != null) {
                    out.append(LINE_SEPARATOR);
                    for (final String headerLine : header) {
                        out.append(headerLine);
                        out.append(LINE_SEPARATOR);
                    }
                }

                out.append(key);
                out.append(": ");
                out.append(defaultValue);
                out.append(LINE_SEPARATOR);
            }

            out.close();
        }
    }

    // save global config
    public void save(final File file) throws IOException {
        try (final FileOutputStream fout = new FileOutputStream(file)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            out.append(DEFAULT_FILE_HEADER);
            out.append(LINE_SEPARATOR);

            for (final Map.Entry<String, String> entry : this.config.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                final String[] header = HEADERS.get(key);

                if (header != null) {
                    out.append(LINE_SEPARATOR);
                    for (final String headerLine : header) {
                        out.append(headerLine);
                        out.append(LINE_SEPARATOR);
                    }
                }

                out.append(key);
                out.append(": ");
                out.append(String.valueOf(value));
                out.append(LINE_SEPARATOR);
            }

            out.close();
        }
    }

    public static void save() throws IOException {
        final File configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFolder.mkdirs();
        }

        final File globalFile = new File(configFolder, ConfigFile.CONFIG);
        if (!globalFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            globalFile.createNewFile();
        }
        getGlobal().save(globalFile);
    }
}
