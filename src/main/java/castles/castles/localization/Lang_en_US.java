package castles.castles.localization;

import java.util.HashMap;

import static castles.castles.localization.Phrase.*;

public class Lang_en_US {
    public static final HashMap<Phrase, String> en_US = new HashMap<>(){{
        put(UNKNOWN_COMMAND, "Unknown or incomplete command, see below for error");
        put(INCORRECT_ARGUMENT, "Incorrect argument for command");
        put(EXPECTED_FLOAT, "Expected float");
        put(EXPECTED_INTEGER, "Expected integer");
        put(ERROR_HERE, "<--[HERE]");
        put(NAME_DUPLICATE, "A castle already exists by that name");
        put(NAME_DOUBLE_QUOTE, "Castle names cannot contain \"");
        put(REQUIRE_ENTITY, "An entity is required to run this command here");
        put(REQUIRE_PLAYER, "A player is required to run this command");
        put(CASTLE_ALREADY_IN_CHUNK, "A castle already exists in this chunk");
        put(CASTLE_OUT_OF_RANGE, "Castle must be between (%d, %d) to (%d, %d)");
        put(Y_COORD_OUT_OF_RANGE, "Y coordinate must be between %d and %d");
        put(CORE_ON_EDGE, "Core cannot be placed on the edge of a chunk");
        put(UNKNOWN_CASTLE, "Unknown castle '%s'");
        put(ALREADY_PART_OF_THE_CASTLE, "This chunk is already part of the castle");
        put(ALREADY_PART_OF_ANOTHER_CASTLE, "This chunk is already part of another castle");
        put(DIFFERENT_WORLD, "The location must be in the castle's world");
        put(TELEPORT_PERMISSION_OWNER_ONLY, "You must be the owner of this castle to teleport");
        put(OUT_OF_CASTLE, "The location must be in the castle");
        put(UNKNOWN_TEAM, "Unknown team '%s'");
        put(INVALID_UNIT, "Invalid unit");
        put(NEGATIVE_TIME, "Time must be non-negative");
        put(LEVEL_OUT_OF_RANGE, "Level must be between %d and %d");
        put(NOT_POSITIVE_HEALTH, "Health must be positive");
        put(HEALTH_EXCEEDS_MAX, "Health must be less than or equal to max health %1.0f");
        // State
        put(STATE_CHANGED, "{0} time started!");
        put(CURRENT_STATE, "Current state: {0}");
        put(PEACEFUL, "Peaceful");
        put(PREPARATION, "Preparation");
        put(WAR, "War");
        // Castle getComponent
        put(CASTLE_OWNER, "Owned by {0}");
        put(CASTLE_NO_OWNER, "No One");
        put(CASTLE_PROTECTED, "Protected for %s");
        put(CASTLE_NOT_PROTECTED, "Not Protected");
        put(CASTLE_RAMPART, "Rampart Level: %s | Health: %s/%s");
        put(CASTLE_CORE, "Core Level: %s | Health: %s/%s");
        // Castles list
        put(CASTLES_LIST_TITLE, "| Castles |");
        put(NO_CASTLES, "No castles");
        // Castles reload
        put(CASTLES_RELOAD, "Castles reloaded!");
        // Castles create
        put(CASTLES_CREATE, "Created castle {0}");
        // Castles remove
        put(CASTLES_REMOVE, "Removed castle {0}");
        // Castles teleport
        put(CASTLES_TELEPORT, "Teleported to castle {0}");
        // Castles modify
        put(CASTLES_MODIFY_LOCATION, "Set castle {0}'s location to {1}");
        put(CASTLES_MODIFY_NAME, "Set castle name to {0}");
        put(CASTLES_MODIFY_OWNER, "Set castle {0} owned by {1}");
        put(CASTLES_MODIFY_OWNER_NONE, "Set castle {0} unowned");
        put(CASTLES_MODIFY_PROTECTION, "Set castle {0}'s protection time to %s");
        put(CASTLES_MODIFY_PROTECTION_NONE, "Castle {0} is no longer protected");
        // Castles core
        put(CASTLES_CORE_LEVEL, "Set castle {0}'s core level to %d");
        put(CASTLES_CORE_HEALTH, "Set castle {0}'s core health to %1.0f");
        // Castles rampart
        put(CASTLES_RAMPART_LEVEL, "Set castle {0}'s rampart level to %d");
        put(CASTLES_RAMPART_HEALTH, "Set castle {0}'s rampart health to %d");
        // Castle Broadcasts
        put(CASTLE_RAMPART_ATTACKED, "The rampart of {0} has been attacked!");
        put(CASTLE_RAMPART_PERCENT, "%d%% of {0} Castle's rampart has been destroyed!");
        put(CASTLE_RAMPART_HEALTH, "{0} Castle's rampart health: %d/%d");
        put(CASTLE_RAMPART_DESTROYED, "The rampart of {0} has been destroyed!");
        put(CASTLE_CORE_ATTACKED, "The castle {0} has been attacked!");
        put(CASTLE_CORE_HEALTH, "{0} Castle's core health: %d/%d");
        put(CASTLE_CORE_RECOVERING, "The castle {0} is recovering!");
        put(CASTLE_CORE_RECOVERED, "The castle {0} has been recovered!");
        put(CASTLE_CORE_OCCUPIED, "The castle {0} has been occupied by {1}!");
        put(CASTLE_CORE_UNOCCUPIED, "The castle {0} has been unoccupied!");
        // Teleport
        put(TELEPORT_COOLDOWN_REMAINING, "You must wait %s before teleporting again");
        put(TELEPORT_WARMUP, "Teleporting to {0} in %s");
        put(TELEPORT_CANCELLED, "Teleport cancelled");
        put(BP_NOT_ENOUGH, "You do not have enough blood points to do that");
        // Blood Points
        put(NEGATIVE_BLOODPOINTS, "Blood points must be non-negative");
        put(UNKNOWN_PLAYER, "No player was found");
    }};
}