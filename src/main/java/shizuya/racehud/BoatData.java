package shizuya.racehud;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatData {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final int TICKRATE = 20;
    
    public final String name;
    private final PlayerListEntry listEntry;

    public int ping;
    public int fps;

    public double xPos;
    public double zPos;
    public double yPos;
    public double speed; // m/s
    public double gLon; // m/s²
    public double gLat; // m/s²
    public double slipAngle; // °
    public double angularVelocity; // °/s
    public double steering;
    public double throttle;
    public double angleFacing; // °
    public double angleTravelling; // °

    private double xPosLast;
    private double zPosLast;
    private double speedLast; // m/s
    private double angleFacingLast; // °
    private double angleTravellingLast; // °

    public Deque<Double> steeringTrace = new ArrayDeque<Double>(Collections.nCopies(40, 0d));
    public Deque<Double> throttleTrace = new ArrayDeque<Double>(Collections.nCopies(40, 0d));

    public double time = 0; // s
    private String fileName = null;
    private Boolean telemetryStart = false;

    public int cp = Config.checkpointSkip;
    public double delta; // s
    public double speedDiff; // m/s
    private double startTime; // s

    public int lapCount = 0;
    public double lastLap = 0; // s
    public double avgLap = 0; // s
    public double bestLap = 0; // s

    public BoatData() {
        this.name = Common.client.player.getEntityName();
        this.listEntry = Common.client.getNetworkHandler().getPlayerListEntry(Common.client.player.getUuid());
        if (Config.telemetryEnabled) this.telemetryFileInit();
        if (Config.checkpointEnabled) this.checkpointInit();
    }

    public void update() {
        BoatEntity boat = (BoatEntity) Common.client.player.getVehicle();
        Vec3d velocity = boat.getVelocity().multiply(1, 0, 1); // Ignore vertical speed
        this.ping = this.listEntry.getLatency();

        this.updateLast();

        this.xPos = boat.getX();
        this.zPos = boat.getZ();
        this.yPos = boat.getY();
        this.speed = velocity.length() * TICKRATE;
        this.angleFacing = boat.getYaw();
        this.angularVelocity = (this.angleFacing - this.angleFacingLast) * TICKRATE;
        this.angleTravelling = Math.toDegrees(Math.atan2(-velocity.getX(), velocity.getZ()));
        this.slipAngle = this.speed == 0 ? 0 : normaliseAngle(this.angleFacing - this.angleTravelling);
        this.gLon = (this.speed - this.speedLast) * TICKRATE;
        this.gLat = Math.sin(Math.toRadians(this.angleTravelling - this.angleTravellingLast) / 2) * this.speedLast * 2 * TICKRATE;
        this.steering = (CLIENT.options.leftKey.isPressed() ? -1d : 0) + (CLIENT.options.rightKey.isPressed() ? 1d : 0);
        this.throttle = (CLIENT.options.forwardKey.isPressed() ? 1d : 0) + (CLIENT.options.backKey.isPressed() ? -0.125 : 0);
        this.updateTrace();

        if (Config.telemetryEnabled && this.throttle > 0.01) this.telemetryStart = true;
        if (this.telemetryStart) this.telemetryWrite();
        if (Config.checkpointEnabled) this.checkpoint();
        this.time += 0.05;
    }

    private void updateLast() {
        this.xPosLast = this.xPos;
        this.zPosLast = this.zPos;
        this.speedLast = this.speed;
        this.angleFacingLast = this.angleFacing;
        this.angleTravellingLast = this.angleTravelling;
    }

    private static double normaliseAngle(double angle) {
        angle = (angle % 360 + 360) % 360;
        return angle > 180 ? angle - 360 : angle;
    }

    private void telemetryFileInit() {
        this.fileName = Config.telemetryPath + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now()) + ".csv";
        try {
            FileWriter fw = new FileWriter(this.fileName);
            fw.write("time,speed,gLon,gLat,slipAngle,angularVelocity,steering,throttle,xPos,zPos,yPos\n");
            fw.close();
        } catch (IOException e) {
        }
    }

    private void telemetryWrite() {
        if (this.fileName == null) return;
        try {
            FileWriter fw = new FileWriter(this.fileName, true);
            fw.write(String.format("%.2f" + ",%.4f".repeat(10) + "\n", this.time, this.speed, this.gLon, this.gLat, this.slipAngle, this.angularVelocity, 
            this.steering, this.throttle, this.xPos, this.zPos, this.yPos));
            fw.close();
        } catch (IOException e) {
        }
    }

    private void updateTrace() {
        steeringTrace.removeFirst();
        steeringTrace.addLast(this.steering);
        throttleTrace.removeFirst();
        throttleTrace.addLast(this.throttle);
    }

    private void checkpointInit() {
        if (Config.checkpointFileLoaded != Config.checkpointFile) Config.loadCheckpoints();
    }

    private void checkpoint() {
        if (this.cp >= Config.checkpoints) return;
        for (double dot = dotProduct(this.xPos, this.zPos); dot > Config.checkpointdata.get(this.cp)[4]; dot = dotProduct(this.xPos, this.zPos)) {
            double dotLast = dotProduct(this.xPosLast, this.zPosLast);
            double subtick = Math.min(Math.max((Config.checkpointdata.get(this.cp)[4] - dotLast) / (dot - dotLast), 0), 1);
            double subtickTime = this.time - 0.05 + subtick * 0.05;
            
            if (this.cp == Config.checkpointSkip) {
                if (this.startTime != 0) {
                    this.lastLap = subtickTime - this.startTime;
                    this.avgLap = (this.avgLap * this.lapCount + this.lastLap) / ++this.lapCount;
                    if (this.bestLap == 0 || this.lastLap < this.bestLap) this.bestLap = this.lastLap;
                }
                this.startTime = subtickTime;
            }

            this.delta = subtickTime - this.startTime - Config.checkpointdata.get(this.cp)[0] + Config.checkpointdata.get(Config.checkpointSkip)[0];
            this.speedDiff = this.speedLast * subtick + this.speed * (1 - subtick) - Config.checkpointdata.get(this.cp)[1];

            this.cp++;
            if (this.cp == Config.checkpoints) {
                if (Config.circularTrack) this.cp = Config.checkpointSkip;
                else break;
            }
        }
    }

    private double dotProduct(double x, double z) {
        return x * Config.checkpointdata.get(this.cp)[2] + z * Config.checkpointdata.get(this.cp)[3];
    }
}
