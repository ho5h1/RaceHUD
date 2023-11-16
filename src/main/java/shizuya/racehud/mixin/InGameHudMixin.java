package shizuya.racehud.mixin;

import shizuya.racehud.HudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shizuya.racehud.Common;
import shizuya.racehud.Config;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.DrawContext;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void render(DrawContext context, float tickDelta, CallbackInfo info) {
        if(Config.enabled && Common.ridingBoat && !(Common.client.currentScreen instanceof ChatScreen)) {
            HudRenderer.get().render(context);
        }
    }

    @Inject(
            method = "renderStatusBars",
            at = @At("HEAD"),
            cancellable = true)
    private void renderStatusBars(DrawContext context, CallbackInfo ci) {
        if(!(Config.enabled && Common.ridingBoat && !(Common.client.currentScreen instanceof ChatScreen))) return;
        ci.cancel();
    }

    @Inject(
            method = "renderExperienceBar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if(!(Config.enabled && Common.ridingBoat && !(Common.client.currentScreen instanceof ChatScreen))) return;
        ci.cancel();
    }

    @Inject(
            method = "renderHotbar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        if(!(Config.enabled && Common.ridingBoat && !(Common.client.currentScreen instanceof ChatScreen))) return;
        ci.cancel();
    }
}
