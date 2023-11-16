package shizuya.racehud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;

public class Common implements ClientModInitializer {

    public static BoatData boatData;
    public static MinecraftClient client = null;
    public static boolean ridingBoat = false;

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        Config.load();
        ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
            if(client.player == null) return;

            if(client.player.getVehicle() instanceof BoatEntity) boatData.update();
            else if(ridingBoat) ridingBoat = false;
        });
    }
}
