package castles.castles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static castles.castles.Castles.*;
import static java.lang.Math.*;



public class Utils {

    public static final int CHUNK_SIZE = 16;

    public static final HashMap<DyeColor, TextColor> d2tColorMap = new HashMap<>() {{
        put(DyeColor.WHITE, TextColor.color(0xF9FFFE));
        put(DyeColor.LIGHT_GRAY, TextColor.color(0x9D9D97));
        put(DyeColor.GRAY, TextColor.color(0x474F52));
        put(DyeColor.BLACK, TextColor.color(0x1D1D21));
        put(DyeColor.BROWN, TextColor.color(0x835432));
        put(DyeColor.RED, TextColor.color(0xB02E26));
        put(DyeColor.ORANGE, TextColor.color(0xF9801D));
        put(DyeColor.YELLOW, TextColor.color(0xFED83D));
        put(DyeColor.LIME, TextColor.color(0x80C71F));
        put(DyeColor.GREEN, TextColor.color(0x5E7C16));
        put(DyeColor.CYAN, TextColor.color(0x169C9C));
        put(DyeColor.LIGHT_BLUE, TextColor.color(0x3AB3DA));
        put(DyeColor.BLUE, TextColor.color(0x3C44AA));
        put(DyeColor.PURPLE, TextColor.color(0x8932B8));
        put(DyeColor.MAGENTA, TextColor.color(0xC74EBD));
        put(DyeColor.PINK, TextColor.color(0xF38BAA));
    }};

    public static final HashMap<TextColor, DyeColor> t2dColorMap = new HashMap<>() {{
        put(NamedTextColor.WHITE, DyeColor.WHITE);
        put(NamedTextColor.GRAY, DyeColor.LIGHT_GRAY);
        put(NamedTextColor.DARK_GRAY, DyeColor.GRAY);
        put(NamedTextColor.BLACK, DyeColor.BLACK);
        put(NamedTextColor.DARK_RED, DyeColor.RED);
        put(NamedTextColor.RED, DyeColor.PINK);
        put(NamedTextColor.GOLD, DyeColor.ORANGE);
        put(NamedTextColor.YELLOW, DyeColor.YELLOW);
        put(NamedTextColor.GREEN, DyeColor.LIME);
        put(NamedTextColor.DARK_GREEN, DyeColor.GREEN);
        put(NamedTextColor.AQUA, DyeColor.LIGHT_BLUE);
        put(NamedTextColor.DARK_AQUA, DyeColor.CYAN);
        put(NamedTextColor.DARK_BLUE, DyeColor.BLUE);
        put(NamedTextColor.BLUE, DyeColor.BLUE);
        put(NamedTextColor.DARK_PURPLE, DyeColor.PURPLE);
        put(NamedTextColor.LIGHT_PURPLE, DyeColor.MAGENTA);

        for (DyeColor color : d2tColorMap.keySet()) {
            put(d2tColorMap.get(color), color);
        }
    }};

    public static final HashMap<TextColor, String> TextColor2ChatCode = new HashMap<>(){{
        put(NamedTextColor.BLACK, "§0");
        put(NamedTextColor.DARK_BLUE, "§1");
        put(NamedTextColor.DARK_GREEN, "§2");
        put(NamedTextColor.DARK_AQUA, "§3");
        put(NamedTextColor.DARK_RED, "§4");
        put(NamedTextColor.DARK_PURPLE, "§5");
        put(NamedTextColor.GOLD, "§6");
        put(NamedTextColor.GRAY, "§7");
        put(NamedTextColor.DARK_GRAY, "§8");
        put(NamedTextColor.BLUE, "§9");
        put(NamedTextColor.GREEN, "§a");
        put(NamedTextColor.AQUA, "§b");
        put(NamedTextColor.RED, "§c");
        put(NamedTextColor.LIGHT_PURPLE, "§d");
        put(NamedTextColor.YELLOW, "§e");
        put(NamedTextColor.WHITE, "§f");
    }};

    public static final HashMap<DyeColor, Material> DyeColor2Wool = new HashMap<>() {{
        put(DyeColor.WHITE, Material.WHITE_WOOL);
        put(DyeColor.LIGHT_GRAY, Material.LIGHT_GRAY_WOOL);
        put(DyeColor.GRAY, Material.GRAY_WOOL);
        put(DyeColor.BLACK, Material.BLACK_WOOL);
        put(DyeColor.BROWN, Material.BROWN_WOOL);
        put(DyeColor.RED, Material.RED_WOOL);
        put(DyeColor.ORANGE, Material.ORANGE_WOOL);
        put(DyeColor.YELLOW, Material.YELLOW_WOOL);
        put(DyeColor.LIME, Material.LIME_WOOL);
        put(DyeColor.GREEN, Material.GREEN_WOOL);
        put(DyeColor.CYAN, Material.CYAN_WOOL);
        put(DyeColor.LIGHT_BLUE, Material.LIGHT_BLUE_WOOL);
        put(DyeColor.BLUE, Material.BLUE_WOOL);
        put(DyeColor.PURPLE, Material.PURPLE_WOOL);
        put(DyeColor.MAGENTA, Material.MAGENTA_WOOL);
        put(DyeColor.PINK, Material.PINK_WOOL);
    }};

    public static final HashMap<Integer, List<String>> RampartTypes = new HashMap<>();
    static {
        RampartTypes.put(1, List.of("STONE_BRICKS", "STONE_BRICK_STAIRS", "STONE_BRICK_SLAB", "CRACKED_STONE_BRICKS"));
        RampartTypes.put(2, List.of("DEEPSLATE_BRICKS", "DEEPSLATE_BRICK_STAIRS", "DEEPSLATE_BRICK_SLAB", "CRACKED_DEEPSLATE_BRICKS"));
        RampartTypes.put(3, List.of("POLISHED_BLACKSTONE_BRICKS", "POLISHED_BLACKSTONE_BRICK_STAIRS", "POLISHED_BLACKSTONE_BRICK_SLAB", "CRACKED_POLISHED_BLACKSTONE_BRICKS"));
        RampartTypes.put(4, List.of("NETHER_BRICKS", "NETHER_BRICK_STAIRS", "NETHER_BRICK_SLAB", "CRACKED_NETHER_BRICKS"));
        RampartTypes.put(5, List.of("QUARTZ_BRICKS", "QUARTZ_STAIRS", "QUARTZ_SLAB", "END_STONE_BRICKS"));
    }


    public static final ArrayList<PotionEffectType> badEffects = new ArrayList<>() {{
        add(PotionEffectType.SLOW);
        add(PotionEffectType.SLOW_DIGGING);
        add(PotionEffectType.HARM);
        add(PotionEffectType.CONFUSION);
        add(PotionEffectType.BLINDNESS);
        add(PotionEffectType.HUNGER);
        add(PotionEffectType.WEAKNESS);
        add(PotionEffectType.POISON);
        add(PotionEffectType.WITHER);
        add(PotionEffectType.LEVITATION);
        add(PotionEffectType.UNLUCK);
        add(PotionEffectType.BAD_OMEN);
    }};

    public static final List<String> nbtBlocks = new ArrayList<>(Arrays.asList(
            "ACACIA_HANGING_SIGN", "ACACIA_SIGN", "ACACIA_WALL_HANGING_SIGN", "ACACIA_WALL_SIGN", "ACACIA_DOOR", "ACACIA_FENCE_GATE",
            "BAMBOO_HANGING_SIGN", "BAMBOO_SIGN", "BAMBOO_WALL_HANGING_SIGN", "BAMBOO_WALL_SIGN",
            "BIRCH_HANGING_SIGN", "BIRCH_SIGN", "BIRCH_WALL_HANGING_SIGN", "BIRCH_WALL_SIGN", "BIRCH_DOOR", "BIRCH_FENCE_GATE",
            "CRIMSON_HANGING_SIGN", "CRIMSON_SIGN", "CRIMSON_WALL_HANGING_SIGN", "CRIMSON_WALL_SIGN", "CRIMSON_DOOR", "CRIMSON_FENCE_GATE",
            "DARK_OAK_HANGING_SIGN", "DARK_OAK_SIGN", "DARK_OAK_WALL_HANGING_SIGN", "DARK_OAK_WALL_SIGN", "DARK_OAK_DOOR", "DARK_OAK_FENCE_GATE",
            "JUNGLE_HANGING_SIGN", "JUNGLE_SIGN", "JUNGLE_WALL_HANGING_SIGN", "JUNGLE_WALL_SIGN", "JUNGLE_DOOR", "JUNGLE_FENCE_GATE",
            "MANGROVE_HANGING_SIGN", "MANGROVE_SIGN", "MANGROVE_WALL_HANGING_SIGN", "MANGROVE_WALL_SIGN", "MANGROVE_DOOR", "MANGROVE_FENCE_GATE",
            "OAK_HANGING_SIGN", "OAK_SIGN", "OAK_WALL_HANGING_SIGN", "OAK_WALL_SIGN", "OAK_DOOR", "OAK_FENCE_GATE",
            "SPRUCE_HANGING_SIGN", "SPRUCE_SIGN", "SPRUCE_WALL_HANGING_SIGN", "SPRUCE_WALL_SIGN", "SPRUCE_DOOR", "SPRUCE_FENCE_GATE",
            "WARPED_HANGING_SIGN", "WARPED_SIGN", "WARPED_WALL_HANGING_SIGN", "WARPED_WALL_SIGN", "WARPED_DOOR", "WARPED_FENCE_GATE",
            "IRON_DOOR",
            "BARREL", "BEE_NEST", "BEEHIVE",
            "BLACK_BED", "BLUE_BED", "BROWN_BED", "CYAN_BED", "GRAY_BED", "GREEN_BED", "LIGHT_BLUE_BED", "LIGHT_GRAY_BED",
            "LIME_BED", "MAGENTA_BED", "ORANGE_BED", "PINK_BED", "PURPLE_BED", "RED_BED", "WHITE_BED", "YELLOW_BED",
            "BLAST_FURNACE", "FURNACE", "SMOKER", "BREWING_STAND",
            "BLACK_SHULKER_BOX", "BLUE_SHULKER_BOX", "BROWN_SHULKER_BOX", "CYAN_SHULKER_BOX", "GRAY_SHULKER_BOX",
            "GREEN_SHULKER_BOX", "LIGHT_BLUE_SHULKER_BOX", "LIGHT_GRAY_SHULKER_BOX", "LIME_SHULKER_BOX",
            "MAGENTA_SHULKER_BOX", "ORANGE_SHULKER_BOX", "PINK_SHULKER_BOX", "PURPLE_SHULKER_BOX", "RED_SHULKER_BOX",
            "WHITE_SHULKER_BOX", "YELLOW_SHULKER_BOX",
            "CAULDRON", "COMPOSTER", "LAVA_CAULDRON", "POWDER_SNOW_CAULDRON", "WATER_CAULDRON",
            "CHEST", "TRAPPED_CHEST",
            "COMPARATOR", "DAYLIGHT_DETECTOR", "DISPENSER", "DROPPER", "END_PORTAL_FRAME", "HOPPER", "JUKEBOX",
            "LECTERN", "MOVING_PISTON", "NOTE_BLOCK", "PISTON", "PISTON_HEAD", "POINTED_DRIPSTONE", "REPEATER", "SHULKER_BOX",
            "SPAWNER", "STICKY_PISTON"
    ));

    public static double getMod(double a, double b) {
        return (b + a % b) % b;
    }

    public static int getMod(int a, int b) {
        return (b + a % b) % b;
    }

    public static TextColor getStateColor(String state) {
        switch (state) {
            case "PEACEFUL":
                return TextColor.color(0x00FF00);
            case "PREPARATION":
                return TextColor.color(0xFFFF00);
            case "WAR":
                return TextColor.color(0xFF0000);
            default:
                return TextColor.color(0xFFFFFF);
        }
    }


    public static class ChunkPos implements Serializable, Cloneable {
        public UUID world;
        public int x;
        public int z;

        public ChunkPos(World world, int x, int z) {
            this.world = world.getUID();
            this.x = x;
            this.z = z;
        }

        public ChunkPos(@NotNull Chunk chunk) {
            this.world = chunk.getWorld().getUID();
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        public ChunkPos(@NotNull Location loc) {
            this(loc.getChunk());
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public World getWorld() {
            return Bukkit.getWorld(world);
        }

        public Chunk getChunk() {
            return getWorld().getChunkAt(x, z);
        }

        public boolean isAdjacent(ChunkPos other) {
            if (other.world.equals(world) && !equals(other)) {
                return Math.abs(other.x - x) <= 1 && Math.abs(other.z - z) <= 1;
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPos chunkPos = (ChunkPos) o;
            return x == chunkPos.x && z == chunkPos.z && world.equals(chunkPos.world);
        }

        @Override
        public ChunkPos clone() {
            try {
                return (ChunkPos) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        @Override
        public String toString() {
            return "ChunkPos{" + "world=" + Bukkit.getWorld(world).getName() + ", x=" + x + ", z=" + z + '}';
        }
    }

    public enum WorldEnv {
        OVERWORLD(63, 133, World.Environment.NORMAL),
        NETHER(32, 121, World.Environment.NETHER),
        END(9, 171, World.Environment.THE_END);

        private final int minY;
        private final int maxY;
        private final World.Environment environment;

        WorldEnv(int minY, int maxY, World.Environment environment) {
            this.minY = minY;
            this.maxY = maxY;
            this.environment = environment;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxY() {
            return maxY;
        }
    }

    public static @NotNull WorldEnv getWorldEnv(World world) {
        for (WorldEnv worldEnv : WorldEnv.values()) {
            if (world.getEnvironment() == worldEnv.environment) {
                return worldEnv;
            }
        }
        return WorldEnv.OVERWORLD;
    }

    public static final NamespacedKey castlesKey = new NamespacedKey(plugin, "castles");

    public static final Collection<Material> HOLLOW_MATERIALS = new HashSet<>();
    public static final Collection<Material> DAMAGING_TYPES = new HashSet<>(Arrays.asList(
            Material.CACTUS,
            Material.CAMPFIRE,
            Material.FIRE,
            Material.MAGMA_BLOCK,
            Material.SOUL_CAMPFIRE,
            Material.SOUL_FIRE,
            Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE
    ));
    public static final Collection<Material> BEDS = new HashSet<>(Arrays.asList(
            Material.WHITE_BED,
            Material.ORANGE_BED,
            Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED,
            Material.YELLOW_BED,
            Material.LIME_BED,
            Material.PINK_BED,
            Material.GRAY_BED,
            Material.LIGHT_GRAY_BED,
            Material.CYAN_BED,
            Material.PURPLE_BED,
            Material.BLUE_BED,
            Material.BROWN_BED,
            Material.GREEN_BED,
            Material.RED_BED,
            Material.BLACK_BED
            ));
    public static final Collection<Material> PORTAL = new HashSet<>(Arrays.asList(
            Material.NETHER_PORTAL,
            Material.END_PORTAL,
            Material.END_GATEWAY
    ));

    static {
        for (Material material : Material.values()) {
            if (material.isTransparent()) {
                HOLLOW_MATERIALS.add(material);
            }
        }
        HOLLOW_MATERIALS.remove(Material.BARRIER);
        HOLLOW_MATERIALS.remove(Material.DIRT_PATH);
        HOLLOW_MATERIALS.remove(Material.FARMLAND);
        HOLLOW_MATERIALS.add(Material.LIGHT);
        HOLLOW_MATERIALS.add(Material.WATER);
    }

    /**
     * Checks if a block is above air.
     * @param world The world the block is in
     * @param x The x coordinate of the block
     * @param y The y coordinate of the block
     * @param z The z coordinate of the block
     * @return True if the block is above air
     */
    public static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
        return y > world.getMaxHeight() || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType());
    }

    /**
     * Checks if a block damages entities.
     * @param world The world the block is in
     * @param x The x coordinate of the block
     * @param y The y coordinate of the block
     * @param z The z coordinate of the block
     * @return true if the block damages entities
     */
    public static boolean isBlockDamaging(final World world, final int x, final int y, final int z) {
        final Material block = world.getBlockAt(x, y, z).getType();
        final Material below = world.getBlockAt(x, y - 1, z).getType();
        final Material above = world.getBlockAt(x, y + 1, z).getType();

        if (DAMAGING_TYPES.contains(block) || below == Material.LAVA || BEDS.contains(below)) {
            return true;
        }

        if (PORTAL.contains(block)) {
            return true;
        }

        return !HOLLOW_MATERIALS.contains(block) || !HOLLOW_MATERIALS.contains(above);
    }

    /**
     * Checks if a block is a safe block to teleport to.
     * @param world The world the block is in
     * @param x The x coordinate of the block
     * @param y The y coordinate of the block
     * @param z The z coordinate of the block
     * @return true if the block is not safe
     */
    public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
        return isBlockDamaging(world, x, y, z) || isBlockAboveAir(world, x, y, z) || !HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).getType());
    }

    /**
     * Checks if X and Z are within the bounds of the world.
     * @param world the world to check
     * @param x the x coordinate
     * @param z the z coordinate
     * @return true if the X and Z are within the world's bounds
     */
    public static boolean isBlockOutsideWorldBorder(final World world, final int x, final int z) {
        final Location center = world.getWorldBorder().getCenter();
        final int radius = (int) world.getWorldBorder().getSize() / 2;
        final int x1 = center.getBlockX() - radius, x2 = center.getBlockX() + radius;
        final int z1 = center.getBlockZ() - radius, z2 = center.getBlockZ() + radius;
        return x < x1 || x > x2 || z < z1 || z > z2;
    }

    /**
     * Get the X coordinate inside the world border.
     *
     * @param world The world to check
     * @param x The X coordinate
     * @return The X coordinate inside the world border.
     */
    public static int getXInsideWorldBorder(final World world, final int x) {
        final Location center = world.getWorldBorder().getCenter();
        final int radius = (int) world.getWorldBorder().getSize() / 2;
        final int x1 = center.getBlockX() - radius, x2 = center.getBlockX() + radius;
        if (x < x1) {
            return x1;
        } else if (x > x2) {
            return x2;
        }
        return x;
    }

    /**
     * Get the Z coordinate inside the world border.
     *
     * @param world The world to check
     * @param z The Z coordinate
     * @return The Z coordinate inside the world border.
     */
    public static int getZInsideWorldBorder(final World world, final int z) {
        final Location center = world.getWorldBorder().getCenter();
        final int radius = (int) world.getWorldBorder().getSize() / 2;
        final int z1 = center.getBlockZ() - radius, z2 = center.getBlockZ() + radius;
        if (z < z1) {
            return z1;
        } else if (z > z2) {
            return z2;
        }
        return z;
    }

    /**
     * Get safe location in the chunk nearest to the given location.
     *
     * @param loc original destination location
     * @return safe location to teleport to
     */
    public static Location getSafeDestination(final Location loc){
        final World world = loc.getWorld();
        final int worldMinY = world.getMinHeight();
        final int worldLogicalY = world.getLogicalHeight();
        int x = loc.getBlockX();
        int y = (int) round(loc.getY());
        int z = loc.getBlockZ();
        ChunkPos chunk = new ChunkPos(loc);
        if (isBlockOutsideWorldBorder(world, x, z)) {
            x = getXInsideWorldBorder(world, x);
            z = getZInsideWorldBorder(world, z);
        }
        final int origY = y;
        while (isBlockAboveAir(world, x, y, z)) {
            y--;
            if (y < worldMinY) {
                y = origY;
                break;
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            HashMap<int[], Double> blocks = new HashMap<>();
            for (int k = -2; k < 3; k++) {
                int cy = y + k;
                if (cy < worldMinY || cy > worldLogicalY) {
                    continue;
                }
                for (int i = 0; i < CHUNK_SIZE; i++) {
                    for (int j = 0; j < CHUNK_SIZE; j++) {
                        int cx = chunk.getX() * CHUNK_SIZE + i;
                        int cz = chunk.getZ() * CHUNK_SIZE + j;
                        if (!isBlockUnsafe(world, cx, cy, cz)) {
                            blocks.put(new int[]{cx, cy, cz}, pow(cx - x, 2) + pow(cy - y, 2) + pow(cz - z, 2));
                        }
                    }
                }
            }
            if (blocks.size() > 0) {
                int[] block = blocks.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
                x = block[0];
                y = block[1];
                z = block[2];
                break;
            } else {
                y += 5;
                if (y > worldLogicalY) {
                    y = origY;
                    break;
                }
            }
        }
        return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }

    /**
     * Removes all leading specified character in a string.
     * @param str The string to remove leading character from.
     * @param chars The character to remove.
     * @return The string with leading characters removed.
     */
    public static String lstrip(String str, String chars) {
        return lstrip(str, chars, Integer.MAX_VALUE);
    }

    /**
     * Removes all leading specified character in a string.
     * @param str The string to remove leading character from
     * @param chars The character to remove
     * @param count The maximum number of characters to remove
     * @return The string with leading characters removed
     */
    public static String lstrip(String str, String chars, int count) {
        int start = 0;
        while (start < str.length() && chars.indexOf(str.charAt(start)) != -1 && start < count) {
            start++;
        }
        return str.substring(start);
    }

    /**
     * Removes all trailing specified character in a string.
     * @param str The string to remove trailing character from
     * @param chars The character to remove
     * @return The string with trailing characters removed
     */

    public static String rstrip(String str, String chars) {
        return rstrip(str, chars, Integer.MAX_VALUE);
    }

    /**
     * Removes all trailing specified character in a string.
     * @param str The string to remove trailing character from
     * @param chars The character to remove
     * @param count The maximum number of characters to remove
     * @return The string with trailing characters removed
     */
    public static String rstrip(String str, String chars, int count) {
        int end = str.length();
        while (end > 0 && chars.indexOf(str.charAt(end - 1)) != -1 && (str.length() - end) < count) {
            end--;
        }
        return str.substring(0, end);
    }

    /**
     * Removes all leading and trailing specified character in a string.
     * @param str The string to remove leading and trailing character from
     * @param chars The character to remove
     * @return The string with leading and trailing characters removed
     */
    public static String strip(String str, String chars) {
        return lstrip(rstrip(str, chars), chars);
    }

    /**
     * Removes all leading and trailing specified character in a string.
     * @param str The string to remove leading and trailing character from
     * @param chars The character to remove
     * @param count The maximum number of characters to remove
     * @return The string with leading and trailing characters removed
     */
    public static String strip(String str, String chars, int count) {
        return lstrip(rstrip(str, chars, count), chars, count);
    }

    /**
     * Counts the number character in a string.
     * @param str The string to search
     * @param sub The substring to search for
     */
    public static int countChar(String str, String sub) {
        return (str.length() - str.replace(sub, "").length()) / sub.length();
    }

    /**
     * Checks if a string is a valid integer.
     * @param str The string to check
     * @return True if the string is a valid integer, false otherwise
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static Component getTeamComponent(Team team) {
        return team.displayName().color(team.hasColor() ? team.color() : NamedTextColor.WHITE).hoverEvent(HoverEvent.showText(Component.text(team.getName())));
    }

    public static Component getLocationComponent(@NotNull Location location) {
        if (location.getX() == location.getBlockX() && location.getY() == location.getBlockY() && location.getZ() == location.getBlockZ()) {
            return Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(), NamedTextColor.GRAY);
        } else {
            return Component.text(location.getX() + ", " + location.getY() + ", " + location.getZ(), NamedTextColor.GRAY);
        }
    }

    /**
     * Parse a string to integer and unit.
     * @param str The string to parse
     *            The string must be in the format of <number><unit>
     * @return The parsed integer and unit
     */
    public static @Nullable AbstractMap.SimpleEntry<Double, String> parseIntegerAndUnit(String str) {
        Pattern pattern = Pattern.compile("(-\\d+|\\d+)(\\.\\d+|\\.|)([A-z]|)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            return new AbstractMap.SimpleEntry<>(Double.parseDouble(matcher.group(1)+matcher.group(2)), matcher.group(3));
        } else {
            return null;
        }
    }

    /**
     * Time unit to second conversion.
     * @param time The time to convert
     * @param unit The time unit
     * @return The time in seconds
     */
     public static @Nullable Integer timeUnitToSeconds(Double time, @NotNull String unit) {
         switch (unit) {
             case "d":
                 return toIntExact(round(time * 86400));
             case "h":
                 return toIntExact(round(time * 3600));
             case "m":
                 return toIntExact(round(time * 60));
             case "s":
             case "":
                 return toIntExact(round(time));
             default:
                 return null;
         }
     }

     /**
      * Get a string representation of a time from seconds.
      * @param time The time in seconds
      * @return The string representation of the time
      */
     public static String secondsToTimeString(int time) {
         int days = time / 86400;
         int hours = (time / 3600) % 24;
         int minutes = (time / 60) % 60;
         int seconds = time % 60;
         if (days > 0) {
             return String.format("%dd:%02dh:%02dm:%02ds", days, hours, minutes, seconds);
         } else {
             return String.format("%dh:%02dm:%02ds", hours, minutes, seconds);
         }
     }

    /**
     * combine Arguments which are covered by quotes into one argument
     * @param args The arguments to combine
     * @return combined arguments
     */
    public static String[] combineArgs(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();
        boolean isInQuotes = false;
        int currentIndex = 0;
        for (String arg : args) {
            if (arg.startsWith("\"")) {
                isInQuotes = true;
            }
            if (isInQuotes) {
                if (newArgs.size() == currentIndex) {
                    newArgs.add(arg);
                } else {
                    newArgs.set(currentIndex, newArgs.get(currentIndex) + " " + arg);
                }
                if (arg.endsWith("\"")) {
                    isInQuotes = false;
                    currentIndex++;
                }
            } else {
                newArgs.add(arg);
                currentIndex++;
            }
        }
        return newArgs.toArray(new String[0]);
    }

    /**
     * Lower strings in an array.
     * @param strings The strings to lower
     * @return The lower strings
     */
    public static String[] lowerStrings(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i].toLowerCase();
        }
        return strings;
    }

    /**
     * get (X, Z) coordinate where a line forwarded by a distance ends.
     * @param X1 line starting X coordinate
     * @param Z1 line starting Z coordinate
     * @param X2 line forward X coordinate
     * @param Z2 line forward Z coordinate
     * @param distance distance from line starting point
     * @return double[] contains coordinates
     */
    public static double[] getLineForward(double X1, double Z1, double X2, double Z2, double distance) {
        double[] result = new double[2];
        double a = X2 - X1;
        double b = Z2 - Z1;
        double r = distance / Math.sqrt(a * a + b * b);
        result[0] = X1 + a * r;
        result[1] = Z1 + b * r;
        return result;
    }

    /**
     *get (X, Z) coordinates list where a line passes through.
     * @param X1 line starting X coordinate
     * @param Z1 line starting Z coordinate
     * @param X2 line ending Z coordinate
     * @param Z2 line ending Z coordinate
     * @return int[] list contains coordinates where a line passes through
     */
    public static int[][] getLineCoordinates(Double X1, Double Z1, Double X2, Double Z2) {
        Bukkit.getLogger().info("X1: " + X1 + " Z1: " + Z1 + " X2: " + X2 + " Z2: " + Z2);
        int X1i = (int) Math.floor(X1), Z1i = (int) Math.floor(Z1), X2i = (int) Math.floor(X2), Z2i = (int) Math.floor(Z2);
        int[][] coordinates;
        if (X1.equals(X2)) {
            coordinates = new int[abs(Z2i - Z1i) + 1][2];
            int j = Z1 < Z2 ? 1 : -1;
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i][0] = X1i;
                coordinates[i][1] = Z1i + i * j;
            }
        } else if (Z1.equals(Z2)) {
            coordinates = new int[abs(X2i - X1i) + 1][2];
            int j = X1 < X2 ? 1 : -1;
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i][0] = X1i + i * j;
                coordinates[i][1] = Z1i;
            }
        } else {
            double k = (Z2 - Z1) / (X2 - X1);
            double b = Z1 - k * X1;
            if (abs(k) < 1) {
                coordinates = new int[abs(X2i - X1i) + 1][2];
                int j = X1 < X2 ? 1 : -1;
                for (int i = 0; i < coordinates.length; i++) {
                    coordinates[i][0] = X1i + i * j;
                    coordinates[i][1] = (int) floor(k * (X1 + i * j) + b);
                }
            } else {
                coordinates = new int[abs(Z2i - Z1i) + 1][2];
                int j = Z1 < Z2 ? 1 : -1;
                for (int i = 0; i < coordinates.length; i++) {
                    coordinates[i][0] = (int) floor((Z1 + i * j - b) / k);
                    coordinates[i][1] = Z1i + i * j;
                }
            }
        }
        return coordinates;
    }

    /**
     * Builds a flag from a location and returns the block list.
     * @param location The location of the flag
     * @param team The team of the flag owner
     * @return The serialized location list of blocks
     */
    public static HashMap<String, List<Map<String, Object>>> buildFlag(@NotNull Location location, @Nullable Team team) {
        List<Map<String, Object>> fences = new ArrayList<>();
        List<Map<String, Object>> wools = new ArrayList<>();
        final World world = location.getWorld();
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        WorldEnv worldEnv = getWorldEnv(world);
        ChunkPos chunk = new ChunkPos(location);
        int top = getWorldEnv(chunk.getWorld()).getMinY() + 12;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                top = max(top, chunk.getWorld().getHighestBlockYAt(chunk.getX() * 16 + i, chunk.getZ() * 16 + j, HeightMap.WORLD_SURFACE_WG) + 6);
            }
        }
        int destination = top + 6;
        int start = y + 3;
        Material wool = DyeColor2Wool.get(t2dColorMap.get(team != null && team.hasColor() ? team.color() : NamedTextColor.WHITE));
        Block cursor = world.getBlockAt(x, y - 1, z);
        wools.add(cursor.getLocation().serialize());
        cursor.setType(wool);
        double[] flagEnd = getLineForward(location.getX(), location.getZ(), location.getX() - getMod(location.getX(), CHUNK_SIZE) + 8, location.getZ() -  getMod(location.getZ(), CHUNK_SIZE) + 8, 8);
        int[][] flags = getLineCoordinates(location.getX(), location.getZ(), flagEnd[0], flagEnd[1]);
        if (worldEnv == WorldEnv.NETHER) {
            if (y > worldEnv.getMaxY() - 16) {
                destination = y - 16;
                start = y - 2;
                for (int i = start; i >= destination + 4; i--) {
                    cursor = world.getBlockAt(x, i, z);
                    cursor.setType(Material.CRIMSON_FENCE);
                    fences.add(cursor.getLocation().serialize());
                }
                for (int i = destination + 3; i >= destination; i--) {
                    for (int[] ints : flags) {
                        cursor = world.getBlockAt(ints[0], i, ints[1]);
                        cursor.setType(wool);
                        wools.add(cursor.getLocation().serialize());
                    }
                }
            } else {
                destination = y + 16;
                start = y + 3;
                for (int i = start; i <= destination - 4; i++) {
                    cursor = world.getBlockAt(x, i, z);
                    cursor.setType(Material.WARPED_FENCE);
                    fences.add(cursor.getLocation().serialize());
                }
                for (int i = destination - 3; i <= destination; i++) {
                    for (int[] ints : flags) {
                        cursor = world.getBlockAt(ints[0], i, ints[1]);
                        cursor.setType(wool);
                        wools.add(cursor.getLocation().serialize());
                    }
                }
            }
        } else {
            for (int i = start; i <= destination - 4; i++) {
                cursor = world.getBlockAt(x, i, z);
                cursor.setType(Material.OAK_FENCE);
                fences.add(cursor.getLocation().serialize());
            }
            for (int i = destination - 3; i <= destination; i++) {
                for (int[] ints : flags) {
                    cursor = world.getBlockAt(ints[0], i, ints[1]);
                    cursor.setType(wool);
                    wools.add(cursor.getLocation().serialize());
                }
            }
        }
        HashMap<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("fences", fences);
        result.put("wools", wools);
        return result;
    }

    public static Set<Location> getBorderCoords(@NotNull ArrayList<ChunkPos> chunks){
        int cMinX = chunks.get(0).getX(), cMinZ = chunks.get(0).getZ(), cMaxX = chunks.get(0).getX(), cMaxZ = chunks.get(0).getZ();
        World world = chunks.get(0).getWorld();
        for (ChunkPos chunk : chunks) {
            if (chunk.getX() < cMinX) cMinX = chunk.getX();
            if (chunk.getZ() < cMinZ) cMinZ = chunk.getZ();
            if (chunk.getX() > cMaxX) cMaxX = chunk.getX();
            if (chunk.getZ() > cMaxZ) cMaxZ = chunk.getZ();
        }
        Set<Location> xzCoords = new HashSet<>();

        ChunkPos cursorChunk;
        for (int i = cMinX - 1; i <= cMaxX + 1; i++) {
            for (int j = cMinZ - 1; j <= cMaxZ + 1; j++) {
                cursorChunk = new ChunkPos(world, i, j);
                if (!chunks.contains(cursorChunk)) {
                    if (chunks.contains(new ChunkPos(world, i + 1, j))) for (int k = 0; k < 16; k++) xzCoords.add(new Location(world, (i + 1) * 16, 0, j * 16 + k));
                    if (chunks.contains(new ChunkPos(world, i - 1, j))) for (int k = 0; k < 16; k++) xzCoords.add(new Location(world, i * 16 - 1, 0, j * 16 + k));
                    if (chunks.contains(new ChunkPos(world, i, j + 1))) for (int k = 0; k < 16; k++) xzCoords.add(new Location(world, i * 16 + k, 0, (j + 1) * 16));
                    if (chunks.contains(new ChunkPos(world, i, j - 1))) for (int k = 0; k < 16; k++) xzCoords.add(new Location(world, i * 16 + k, 0, j * 16 - 1));
                    if (chunks.contains(new ChunkPos(world, i + 1, j + 1))) xzCoords.add(new Location(world, (i + 1) * 16, 0, (j + 1) * 16));
                    if (chunks.contains(new ChunkPos(world, i + 1, j - 1))) xzCoords.add(new Location(world, (i + 1) * 16, 0, j * 16 - 1));
                    if (chunks.contains(new ChunkPos(world, i - 1, j + 1))) xzCoords.add(new Location(world, i * 16 - 1, 0, (j + 1) * 16));
                    if (chunks.contains(new ChunkPos(world, i - 1, j - 1))) xzCoords.add(new Location(world, i * 16 - 1, 0, j * 16 - 1));
                }
            }
        }
        return xzCoords;
    }

    public static HashMap<Map<String, Object>, String> getRampartCoords(@NotNull ArrayList<ChunkPos> chunks, int top) {
        World world = chunks.get(0).getWorld();
        Set<Location> xzCoords = getBorderCoords(chunks);

        List<Integer> bricks = new ArrayList<>(Arrays.asList(2, 3, 5, 6, 9, 10, 12, 13));
        List<Integer> slab = new ArrayList<>(Arrays.asList(1, 4, 11, 14));
        List<Integer> stairs = new ArrayList<>(Arrays.asList(7, 8));
        HashMap<Map<String, Object>, String> coords = new HashMap<>();
        Location cursor;
        int bottom = getWorldEnv(world).getMinY();

        for (Location location : xzCoords) {
            cursor = new Location(location.getWorld(), location.getX(), top, location.getZ());
            // check is corner of chunk
            if ((getMod(cursor.getBlockX(), 16) == 0 && getMod(cursor.getBlockZ(), 16) == 0) || (getMod(cursor.getBlockX(), 16) == 15 && getMod(cursor.getBlockZ(), 16) == 0) || (getMod(cursor.getBlockX(), 16) == 0 && getMod(cursor.getBlockZ(), 16) == 15) || (getMod(cursor.getBlockX(), 16) == 15 && getMod(cursor.getBlockZ(), 16) == 15)) {
                coords.put(cursor.serialize(), "BRICKS");
            } else if (bricks.contains(getMod(cursor.getBlockX(), 16)) || bricks.contains(getMod(cursor.getBlockZ(), 16))) {
                coords.put(cursor.serialize(), "BRICKS");
            } else if (slab.contains(getMod(cursor.getBlockX(), 16)) || slab.contains(getMod(cursor.getBlockZ(), 16))) {
                coords.put(cursor.serialize(), "SLAB");
            } else if (stairs.contains(getMod(cursor.getBlockX(), 16)) || stairs.contains(getMod(cursor.getBlockZ(), 16))) {
                coords.put(cursor.serialize(), "STAIRS");
            }
            for (int i = top - 1; i >= bottom; i--) {
                cursor = new Location(location.getWorld(), location.getX(), i, location.getZ());
                coords.put(cursor.serialize(), "BRICKS");
            }
        }
        return coords;
    }

    public static @NotNull String getDisplayName(@NotNull Team team) {
        return TextColor2ChatCode.getOrDefault(team.hasColor() ? team.color() : NamedTextColor.WHITE, "§f") + ((TextComponent) team.displayName()).content();
    }

    public static void loadTeamEntry() {
        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            teamToEntry.put(team, getDisplayName(team));
        }
        Objective bloodPoints = getBloodPointsObjective();
        for (String entry : bloodPoints.getScoreboard().getEntries()) {
            if (!teamToEntry.containsValue(entry)) {
                bloodPoints.getScoreboard().resetScores(entry);
            }
        }
    }

    public static @NotNull Objective getBloodPointsObjective() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("bloodPoints");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("bloodPoints", "dummy", "§4 Blood Points");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            for (String team : teamToEntry.values()) {
                objective.getScore(team).setScore(0);
            }
        }
        return objective;
    }

    public static void addScore(@Nullable Team team, int value) {
        if (team == null) return;
        Objective bloodPoints = getBloodPointsObjective();
        Score score = bloodPoints.getScore(getDisplayName(team));
        score.setScore(score.getScore() + value);
    }

    public static void setScore(@Nullable Team team, int value) {
        if (team == null) return;
        Objective bloodPoints = getBloodPointsObjective();
        Score score = bloodPoints.getScore(getDisplayName(team));
        score.setScore(value);
    }

    public static void loadVictims() {
        try {
            final File configFolder = plugin.getDataFolder();
            if (!configFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                configFolder.mkdir();
            }
            final File dataFile = new File(configFolder, "Victims.dat");
            if (dataFile.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(dataFile)) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    //noinspection unchecked
                    killerVictims = Collections.synchronizedMap((Map<UUID, List<UUID>>) objectInputStream.readObject());
                    objectInputStream.close();
                } catch (IOException | ClassNotFoundException e) {
                    killerVictims = Collections.synchronizedMap(new HashMap<>());
                }
            } else {
                killerVictims = Collections.synchronizedMap(new HashMap<>());
            }
        } catch (Exception e) {
            killerVictims = Collections.synchronizedMap(new HashMap<>());
        }
    }

    public static void saveVictims() {
        final File configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFolder.mkdir();
        }
        try {
            final File dataFile = new File(configFolder, "Victims.dat");
            if (!dataFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dataFile.createNewFile();
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(dataFile)) {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(killerVictims);
                objectOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCastles() {
        try {
            final File configFolder = plugin.getDataFolder();
            if (!configFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                configFolder.mkdir();
            }
            final File dataFile = new File(configFolder, "castles.dat");
            if (dataFile.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(dataFile)) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    //noinspection unchecked
                    castles = Collections.synchronizedList((List<Castle>) objectInputStream.readObject());
                    objectInputStream.close();
                } catch (IOException | ClassNotFoundException e) {
                    castles = Collections.synchronizedList(new ArrayList<>());
                }
            } else {
                castles = Collections.synchronizedList(new ArrayList<>());
            }
        } catch (Exception e) {
            castles = Collections.synchronizedList(new ArrayList<>());
        }
    }

    public static void saveCastles() {
        final File configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFolder.mkdir();
        }
        try {
            final File dataFile = new File(configFolder, "castles.dat");
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(castles);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the castle with the given name.
     * @param name The name of the castle
     * @return The castle with the given name or null if no castle with the given name exists
     */
    public static Castle getCastleByName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        for (Castle castle : castles) {
            if (castle.name.equals(name)) {
                return castle;
            }
        }
        return null;
    }

    /**
     * Get the castle at the given location.
     * @param location The location to check
     * @return The castle at the given location or null if no castle exists at the given location
     */
    public static Castle getCastleByLocation(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        return getCastleByChunk(new ChunkPos(location.getChunk()));
    }

    /**
     * Get the castle at the given chunk.
     * @param chunk The chunk to check
     * @return The castle at the given chunk or null if no castle exists at the given chunk
     */
    public static Castle getCastleByChunk(@Nullable ChunkPos chunk) {
        if (chunk == null) {
            return null;
        }
        Chunk mChunk = chunk.getChunk();
        String name = mChunk.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING);
        if (name != null) {
            return getCastleByName(name);
        }
        return null;
    }

    /**
     * Get the castle nearest to the given player.
     * @param location The location to check
     * @return The castle nearest to the given player or null if no castle exists
     */
    public static @Nullable Castle getNearestCastle(Location location) {
        Castle nearestCastle = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Castle castle : Castles.castles) {
            double distance = castle.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearestCastle = castle;
                nearestDistance = distance;
            }
        }
        return nearestCastle;
    }

    /**
     * Get the castle nearest to the given player.
     * @param player The player to check
     * @return The castle nearest to the given player or null if no castle exists
     */
    public static @Nullable Castle getNearestCastle(Player player) {
        return getNearestCastle(player.getLocation());
    }
}
