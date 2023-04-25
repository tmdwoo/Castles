package castles.castles.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

import static castles.castles.item.Items.getItemCore;

public class ItemHandler implements Listener {
    private void createCastle(Location location, Player player) {
        /*
        TODO: create castle 함수
        CastleCommand.java에서 사용한 것과 같은 방식으로 성을 지을 수 있는지 검사
        persistent data가 has() true면 이미 성을 만들고 있으므로 에러와 함께 return
        createCastle 성 생성시 플레이어 persistent data에 int형으로 bukkit task id 저장 후 10초의 scheduler로 기다린 후 채팅이 없으면 생성 이벤트 취소
        10초 내로 player chat event가 생성될 경우 큰따옴표 여부 검사 후 없으면 생성
        */
        if (!location.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            player.sendMessage(Component.text("You can only build a castle in the overworld", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onCoreUse(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().isSimilar(getItemCore()) && Objects.equals(event.getHand(), EquipmentSlot.HAND)) {
            Location location = event.getClickedBlock().getLocation().add(event.getBlockFace().getDirection());

        }
    }
}
