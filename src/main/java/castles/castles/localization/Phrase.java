package castles.castles.localization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static castles.castles.localization.Lang_en_US.en_US;
import static castles.castles.localization.Lang_ko_KR.ko_KR;

public enum Phrase {
    UNKNOWN_COMMAND,
    INCORRECT_ARGUMENT,
    EXPECTED_FLOAT,
    EXPECTED_INTEGER,
    ERROR_HERE,
    NAME_DUPLICATE,
    NAME_DOUBLE_QUOTE,
    REQUIRE_ENTITY,
    REQUIRE_PLAYER,
    CASTLE_ALREADY_IN_CHUNK,
    Y_COORD_OUT_OF_RANGE,
    CORE_ON_EDGE,
    UNKNOWN_CASTLE,
    ALREADY_PART_OF_THE_CASTLE,
    ALREADY_PART_OF_ANOTHER_CASTLE,
    DIFFERENT_WORLD,
    TELEPORT_PERMISSION_OWNER_ONLY,
    OUT_OF_CASTLE,
    UNKNOWN_TEAM,
    INVALID_UNIT,
    NEGATIVE_TIME,
    LEVEL_OUT_OF_RANGE,
    NOT_POSITIVE_HEALTH,
    HEALTH_EXCEEDS_MAX,
    NEGATIVE_BLOODPOINTS,
    UNKNOWN_PLAYER,
    STATE_CHANGED,
    CURRENT_STATE,
    PEACEFUL,
    PREPARATION,
    WAR,
    CASTLE_OWNER,
    CASTLE_NO_OWNER,
    CASTLE_PROTECTED,
    CASTLE_NOT_PROTECTED,
    CASTLE_RAMPART,
    CASTLE_CORE,
    CASTLES_LIST_TITLE,
    NO_CASTLES,
    CASTLES_RELOAD,
    CASTLES_CREATE,
    CASTLES_REMOVE,
    CASTLES_TELEPORT,
    CASTLES_MODIFY_LOCATION,
    CASTLES_MODIFY_NAME,
    CASTLES_MODIFY_OWNER,
    CASTLES_MODIFY_OWNER_NONE,
    CASTLES_MODIFY_PROTECTION,
    CASTLES_MODIFY_PROTECTION_NONE,
    CASTLES_CORE_LEVEL,
    CASTLES_CORE_HEALTH,
    CASTLES_RAMPART_LEVEL,
    CASTLES_RAMPART_HEALTH,
    CASTLE_RAMPART_ATTACKED,
    CASTLE_RAMPART_PERCENT,
    CASTLE_RAMPART_HEALTH,
    CASTLE_RAMPART_DESTROYED,
    CASTLE_CORE_ATTACKED,
    CASTLE_CORE_HEALTH,
    CASTLE_CORE_RECOVERING,
    CASTLE_CORE_RECOVERED,
    CASTLE_CORE_OCCUPIED,
    CASTLE_CORE_UNOCCUPIED,
    TELEPORT_COOLDOWN_REMAINING,
    TELEPORT_WARMUP,
    TELEPORT_CANCELLED,
    BP_NOT_ENOUGH;

    public String getPhrase() {
        return en_US.get(this);
    }

    public String getPhrase(Locale locale) {
        switch (locale.getLanguage()) {
            case "ko":
                return ko_KR.getOrDefault(this, en_US.get(this));
            default:
                return en_US.getOrDefault(this, this.toString());
        }
    }

    public String getPhrase(Player player) {
        return getPhrase(player.locale());
    }

    public String getPhrase(CommandSender sender) {
        if (sender instanceof Player) {
            return getPhrase((Player) sender);
        } else {
            return getPhrase();
        }
    }

    public static Phrase stringToPhrase(String s) {
        for (Phrase phrase : Phrase.values()) {
            if (phrase.toString().equals(s)) {
                return phrase;
            }
        }
        return null;
    }

    public static Component formatComponent(TextComponent component, Component... args) {
        TextComponent result = Component.empty();
        String regex = "\\{\\d+}";
        Pattern pattern = Pattern.compile(regex);
        String text = component.content();
        Component child = Component.empty();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String group = matcher.group();
            int index = Integer.parseInt(group.substring(1, group.length() - 1));
            if (index < args.length) {
                child = child.append(Component.text(text.substring(0, matcher.start())));
                child = child.append(args[index]);
                text = text.substring(matcher.end());
                matcher = pattern.matcher(text);
            }
        }
        child = child.append(Component.text(text));
        result = result.append(child);
        return result;
    }
}
