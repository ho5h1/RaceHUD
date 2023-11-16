package shizuya.racehud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class HudRenderer {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("racehud","textures/widgets.png");
    private static final HudRenderer INSTANCE = new HudRenderer();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    //                                                Pack   Mix  Blue
    private static final double[] MIN_V           = {   0d,   0d,  40d}; // Minimum display speed (m/s)
    private static final double[] MAX_V           = {  40d,  72d, 72.7}; // Maximum display speed (m/s)
    private static final double[] SCALE_V         = {  5.4,  3.0,  6.6}; // Pixels for 1 unit of speed (px*s/m) (BarWidth / (VMax - VMin))
    private static final double[] SCALE_V_COMPACT = {  3.6,  2.0,  4.4};

    private static final int CENTRE_X = CLIENT.getWindow().getScaledWidth() / 2;
    private static final int BOTTOM_Y = CLIENT.getWindow().getScaledHeight() - Config.yOffset + 6;
    private static final int[] SLOTS_X = {CENTRE_X - 104, CENTRE_X - 14, CENTRE_X + 14, CENTRE_X + 104};
    private static final int[] SLOTS_X_COMPACT = {CENTRE_X - 68, CENTRE_X, CENTRE_X + 68};
    private static final int[] SLOTS_Y = {BOTTOM_Y - 14, BOTTOM_Y - 4};

    public static HudRenderer get() {
        return INSTANCE;
    }

    public void render(DrawContext context) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawBackground(context);
        drawSpeedBar(context);
        if (Config.extended) {
            drawGMeter(context);
            drawThrottleTrace(context);
            drawSteeringTrace(context);
        } else {
            drawKeyInputs(context);
        }
        displayText(context);
        RenderSystem.disableBlend();
    }

    private static void drawBackground(DrawContext context) {
        context.drawTexture(WIDGETS_TEXTURE, CENTRE_X - getTextureWidth() / 2, BOTTOM_Y - 20, 0, getTextureOffset(), getTextureWidth(), 26);
        if (Config.laptimeStats) context.drawTexture(WIDGETS_TEXTURE, 0, 270 - 21, 0, 112, 70, 42);
    }

    private static void drawSpeedBar(DrawContext context) {
        context.drawTexture(WIDGETS_TEXTURE, CENTRE_X - getTextureWidth() / 2, BOTTOM_Y - 20, 0, 26 + getTextureOffset() + Config.barType * 10, getTextureWidth(), 5);
        if (Common.boatData.speed < MIN_V[Config.barType]) return;
        if (Common.boatData.speed > MAX_V[Config.barType]) {
            if ((CLIENT.world.getTime() & 2) != 0) { // Flash
                context.drawTexture(WIDGETS_TEXTURE, CENTRE_X - getTextureWidth() / 2, BOTTOM_Y - 20, 0, 26 + getTextureOffset() + Config.barType * 10 + 5, getTextureWidth(), 5);
            }
            return;
        }
        context.drawTexture(WIDGETS_TEXTURE, CENTRE_X - getTextureWidth() / 2, BOTTOM_Y - 20, 0, 26 + getTextureOffset() + Config.barType * 10 + 5, getBarLength() , 5);
    }

    private static int getTextureOffset() {
        return Config.extended ? 0 : 56;
    }

    private static int getTextureWidth() {
        return Config.extended ? 218 : 146;
    }

    private static int getBarLength() {
        return (int) ((Common.boatData.speed - MIN_V[Config.barType]) * (Config.extended ? SCALE_V[Config.barType] : SCALE_V_COMPACT[Config.barType]) + 1.5);
    }

    private static void drawGMeter(DrawContext context) {
        context.drawTexture(WIDGETS_TEXTURE, CENTRE_X - 9, BOTTOM_Y - 14, 218, 0, 18, 18);
        context.drawTexture(WIDGETS_TEXTURE, CENTRE_X - 1 + getGPosition(Common.boatData.gLat), BOTTOM_Y - 6 - getGPosition(Common.boatData.gLon), getGColour(), 0, 2, 2);
    }

    private static int getGPosition(double g) {
        return Math.min(Math.max((int) (g / 2.5), -8), 8);
    }

    private static int getGColour() {
        return Math.abs(Common.boatData.gLon) > 22.5 || Math.abs(Common.boatData.gLat) > 22.5 ? 238 : 236;
    }

    private static void drawThrottleTrace(DrawContext context) {
        int i = 0;
        for (double throttle : Common.boatData.throttleTrace) {
            context.drawTexture(WIDGETS_TEXTURE, SLOTS_X[1] - 40 + i, SLOTS_Y[0] + 4 + getTracePosition(throttle), getThrottleColour(throttle), 0, 1, 1);
            i++;
        }
    }

    private static void drawSteeringTrace(DrawContext context) {
        int i = 0;
        for (double steering : Common.boatData.steeringTrace) {
            context.drawTexture(WIDGETS_TEXTURE, SLOTS_X[2] + i, SLOTS_Y[0] + 4 - getTracePosition(steering), 242, 0, 1, 1);
            i++;
        }
    }

    private static int getThrottleColour(double throttle) {
        return throttle > 0 ? 240 : throttle < 0 ? 241 : 242;
    }

    private static int getTracePosition(double value) {
        return ((int) Math.signum(value)) * -4;
    }

    private static void drawKeyInputs(DrawContext context) {
        context.drawTexture(WIDGETS_TEXTURE, SLOTS_X_COMPACT[1] - 27, SLOTS_Y[1], 146, 56, 42, 9);
        Boolean[] inputs = {CLIENT.options.leftKey.isPressed(), 
                            CLIENT.options.backKey.isPressed(), 
                            CLIENT.options.forwardKey.isPressed(), 
                            CLIENT.options.rightKey.isPressed()};
        for (int i = 0; i < 4; i++) {
            if (inputs[i]) context.drawTexture(WIDGETS_TEXTURE, SLOTS_X_COMPACT[1] - 27 + 11 * i, SLOTS_Y[1], 146 + 11 * i, 65, 9, 9);
        }
    }

    private static void displayText (DrawContext context) {
        if (Config.extended) {
            drawTextAlign(context, getString(DisplayType.SPEED), SLOTS_X[0], SLOTS_Y[0], Alignment.LEFT);
            drawTextAlign(context, getString(DisplayType.SLIPANGLE), SLOTS_X[3], SLOTS_Y[0], Alignment.RIGHT);
            drawTextAlign(context, getString(DisplayType.PING), SLOTS_X[2], SLOTS_Y[1], Alignment.LEFT);
            drawTextAlign(context, getString(DisplayType.FPS), SLOTS_X[3], SLOTS_Y[1], Alignment.RIGHT);
            if (Config.checkpointEnabled) {
                drawTextAlign(context, getString(DisplayType.DELTA), SLOTS_X[0], SLOTS_Y[1], Alignment.LEFT);
                drawTextAlign(context, getString(DisplayType.SPEED_DIFF), SLOTS_X[1], SLOTS_Y[1], Alignment.RIGHT);
            } else {
                drawTextAlign(context, getString(DisplayType.ACCELERATION), SLOTS_X[1] - 1, SLOTS_Y[1], Alignment.RIGHT);
            }
        } else {
            drawTextAlign(context, getString(DisplayType.SPEED), SLOTS_X_COMPACT[0], SLOTS_Y[0], Alignment.LEFT);
            drawTextAlign(context, getString(DisplayType.SLIPANGLE), SLOTS_X_COMPACT[2], SLOTS_Y[0], Alignment.RIGHT);
            drawTextAlign(context, getString(DisplayType.PING), SLOTS_X_COMPACT[0], SLOTS_Y[1], Alignment.LEFT);
            drawTextAlign(context, getString(DisplayType.FPS), SLOTS_X_COMPACT[2], SLOTS_Y[1], Alignment.RIGHT);
            if (Config.checkpointEnabled) {
                drawTextAlign(context, getString(DisplayType.DELTA), SLOTS_X_COMPACT[1] + 6, SLOTS_Y[0], Alignment.CENTRE);
            } else {
                drawTextAlign(context, getString(DisplayType.ACCELERATION), SLOTS_X_COMPACT[1] + 6, SLOTS_Y[0], Alignment.CENTRE);
            }
        }
        if (Config.laptimeStats) {
            drawTextAlign(context, "Laps:", 2, 270 - 21 + 2, Alignment.LEFT);
            drawTextAlign(context, "Last:", 2, 270 - 21 + 12, Alignment.LEFT);
            drawTextAlign(context, "Aver:", 2, 270 - 21 + 22, Alignment.LEFT);
            drawTextAlign(context, "Best:", 2, 270 - 21 + 32, Alignment.LEFT);
            drawTextAlign(context, String.format("%d", Common.boatData.lapCount), 70 - 2, 270 - 21 + 2, Alignment.RIGHT);
            drawTextAlign(context, displayTime(Common.boatData.lastLap), 70 - 2, 270 - 21 + 12, Alignment.RIGHT);
            drawTextAlign(context, displayTime(Common.boatData.avgLap), 70 - 2, 270 - 21 + 22, Alignment.RIGHT);
            drawTextAlign(context, displayTime(Common.boatData.bestLap), 70 - 2, 270 - 21 + 32, Alignment.RIGHT);
        }
    }

    private static void drawTextAlign(DrawContext context, String text, int x, int y, Alignment align) {
        context.drawTextWithShadow(CLIENT.textRenderer, text, x - CLIENT.textRenderer.getWidth(text) * align.ordinal() / 2, y, 0xFFFFFF);
    }

    private enum Alignment {
        LEFT, CENTRE, RIGHT
    }

    private enum DisplayType {
        SPEED, SLIPANGLE, PING, FPS, ACCELERATION, DELTA, SPEED_DIFF
    }

    private static String getString(DisplayType type) {
        switch (type) {
        case SPEED:
            return String.format("%" + threeDigits(Common.boatData.speed * Config.speedRate) + Config.speedUnit, Common.boatData.speed * Config.speedRate);
        case SLIPANGLE:
            return String.format("%" + threeDigits(Common.boatData.slipAngle) + Config.angleUnit, Math.abs(Common.boatData.slipAngle));
        case PING:
            return getStringColour(type) + String.format("%03d §fms", Common.boatData.ping);
        case FPS:
            return getStringColour(type) + String.format("%03d §ffps", Common.boatData.fps);
        case ACCELERATION:
            if (!Config.extended) return String.format(getAccelerationFormat(), Common.boatData.gLon * Config.accelerationRate);
            return String.format(getAccelerationFormat(), Common.boatData.gLon * Config.accelerationRate, Math.abs(Common.boatData.gLat * Config.accelerationRate));
        case DELTA:
            return getStringColour(type) + String.format("%+" + threeDigits(Common.boatData.delta) + "s", Common.boatData.delta);
        case SPEED_DIFF:
            return getStringColour(type) + String.format("%+" + threeDigits(Common.boatData.speedDiff * Config.speedRate) + Config.speedUnit, Common.boatData.speedDiff * Config.speedRate);
        default:
            return "";
        }
    }

    private static String displayTime(double time) {
        if (Config.timeFormat == 1) {
            return String.format("%d:%05.2f", (int) time / 60, time % 60);
        } else {
            return String.format("%06.2f", time);
        }
    }

    private static String threeDigits(double value) {
        return Math.abs(value) >= 99.95 ? ".0f" : Math.abs(value) >= 9.95 ? ".1f" : ".2f";
    }

    private static String getAccelerationFormat() {
        String s = "%+" + threeDigits(Common.boatData.gLon * Config.accelerationRate);
        if (Config.extended) s += " / %" + threeDigits(Common.boatData.gLat * Config.accelerationRate);
        return s + Config.accelerationUnit;
    }

    private static String getStringColour(DisplayType type) {
        switch (type) {
        case PING:
            if (Common.boatData.ping == 0) return "§f";
            return Common.boatData.ping > 500 ? "§c" : Common.boatData.ping < 50 ? "§a" : "§f";
        case FPS:
            return Common.boatData.fps < 60 ? "§c" : Common.boatData.fps > 240 ? "§a" : "§f";
        case DELTA:
            return Common.boatData.delta > 0.025 ? "§c" : Common.boatData.delta < -0.025 ? "§a" : "§f";
        case SPEED_DIFF:
            return Common.boatData.speedDiff < -0.4 ? "§c" : Common.boatData.speedDiff > 0.4 ? "§a" : "§f";
        default:
            return "";
        }
    }
}
