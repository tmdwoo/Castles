package castles.castles.localization;

import java.util.HashMap;

import static castles.castles.localization.Phrase.*;

public class Lang_ko_KR {
    public static final HashMap<Phrase, String> ko_KR = new HashMap<>(){{
        put(UNKNOWN_COMMAND, "알 수 없거나 불완전한 명령어입니다. 아래의 오류를 확인하세요");
        put(INCORRECT_ARGUMENT, "명령어에 잘못된 인수가 있습니다");
        put(EXPECTED_FLOAT, "Expected float");
        put(EXPECTED_INTEGER, "Expected integer");
        put(ERROR_HERE, "<--[여기]");
        put(NAME_DUPLICATE, "A castle already exists by that name");
        put(NAME_DOUBLE_QUOTE, "Castle names cannot contain \"");
        put(REQUIRE_ENTITY, "An entity is required to run this command here");
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
        put(INVALID_UNIT, "잘못된 단위입니다");
        put(NEGATIVE_TIME, "Time must be non-negative");
        put(LEVEL_OUT_OF_RANGE, "Level must be between %d and %d");
        put(NOT_POSITIVE_HEALTH, "Health must be positive");
        put(HEALTH_EXCEEDS_MAX, "Health must be less than or equal to max health %1.0f");
        // State
        put(STATE_CHANGED, "{0}시간이 시작되었습니다!");
        put(CURRENT_STATE, "현재 상태: {0}");
        put(PEACEFUL, "평화");
        put(PREPARATION, "준비");
        put(WAR, "전쟁");
        // Castle getComponent
        put(CASTLE_OWNER, "소유자 {0}");
        put(CASTLE_NO_OWNER, "없음");
        put(CASTLE_PROTECTED, "%s 동안 보호됨");
        put(CASTLE_NOT_PROTECTED, "보호되지 않음");
        put(CASTLE_RAMPART, "성벽 레벨: %s | 체력: %s/%s");
        put(CASTLE_CORE, "코어 레벨: %s | 체력: %s/%s");
        // Castles list
        put(CASTLES_LIST_TITLE, "| 성 |");
        put(NO_CASTLES, "성이 없습니다");
        // Castles create
        put(CASTLES_CREATE, "{0}을(를) 생성했습니다");
        // Castles remove
        put(CASTLES_REMOVE, "{0}을(를) 제거했습니다");
        // Castles teleport
        put(CASTLES_TELEPORT, "{0}(으)로 순간이동했습니다");
        // Castles modify
        put(CASTLES_MODIFY_LOCATION, "{0}의 위치를 {1}로 설정했습니다");
        put(CASTLES_MODIFY_NAME, "성의 이름을 {0}(으)로 설정했습니다");
        put(CASTLES_MODIFY_OWNER, "{0}의 소유자를 {1}(으)로 설정했습니다");
        put(CASTLES_MODIFY_OWNER_NONE, "{0}의 소유자를 제거했습니다");
        put(CASTLES_MODIFY_PROTECTION, "{0}의 보호 시간을 %s로 설정했습니다");
        put(CASTLES_MODIFY_PROTECTION_NONE, "{0}은(는) 더 이상 보호되지 않습니다");
        // Castles core
        put(CASTLES_CORE_LEVEL, "{0}의 코어 레벨을 %d로 설정했습니다");
        put(CASTLES_CORE_HEALTH, "{0}의 코어 체력을 %1.0f로 설정했습니다");
        // Castles rampart
        put(CASTLES_RAMPART_LEVEL, "{0}의 성벽 레벨을 %d로 설정했습니다");
        put(CASTLES_RAMPART_HEALTH, "{0}의 성벽 체력을 %d로 설정했습니다");
        // Castle Broadcasts
        put(CASTLE_RAMPART_ATTACKED, "{0}의 성벽이 공격받고 있습니다!");
        put(CASTLE_RAMPART_PERCENT, "{0}의 성벽의 %d%%가 파괴되었습니다!");
        put(CASTLE_RAMPART_HEALTH, "{0} 성벽 체력: %d/%d");
        put(CASTLE_RAMPART_DESTROYED, "{0}의 성벽이 파괴되었습니다!");
        put(CASTLE_CORE_ATTACKED, "{0}이(가) 공격받고 있습니다!");
        put(CASTLE_CORE_HEALTH, "{0} 체력: %d/%d");
        put(CASTLE_CORE_RECOVERING, "{0}이(가) 회복하고 있습니다!");
        put(CASTLE_CORE_RECOVERED, "{0}이(가) 회복되었습니다!");
        put(CASTLE_CORE_OCCUPIED, "{1}이(가) {0}을(를) 점령했습니다!");
        put(CASTLE_CORE_UNOCCUPIED, "{0}이(가) 해방되었습니다!");
        // Teleport
        put(TELEPORT_COOLDOWN_REMAINING, "%s초 후에 다시 순간이동할 수 있습니다");
        put(TELEPORT_WARMUP, "%s초 후에 {0}으로 순간이동합니다");
        put(TELEPORT_CANCELLED, "순간이동이 취소되었습니다");
        put(BP_NOT_ENOUGH, "블러드 포인트가 부족합니다");
        // Blood Points
        put(NEGATIVE_BLOODPOINTS, "블러드 포인트는 음수가 될 수 없습니다");
        put(UNKNOWN_PLAYER, "플레이어를 찾을 수 없습니다");
    }};
}