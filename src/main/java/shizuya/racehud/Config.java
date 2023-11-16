package shizuya.racehud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import net.fabricmc.loader.api.FabricLoader;

public class Config {

    public static String configDirectory = FabricLoader.getInstance().getConfigDir().toString();
    private static File configFile = new File(configDirectory, "racehud.properties");
    
    public static boolean enabled = true;
    public static boolean extended = true;
    public static int yOffset = 36;
    public static int barType = 0;
    public static String speedUnit = " m/s";
    public static String angleUnit = " °";
    public static String accelerationUnit = " m/s²";
    public static int timeFormat = 0;
    public static boolean telemetryEnabled = false;
    public static String telemetryPath = configDirectory + File.separator + "racehud" + File.separator + "telemetry" + File.separator;
    public static boolean checkpointEnabled = false;
    public static String checkpointPath = configDirectory + File.separator + "racehud" + File.separator + "checkpoints" + File.separator;
    public static String checkpointFile = "checkpoint_file.cf";
    public static String checkpointFileLoaded = "";
    public static boolean circularTrack = false;
    public static int checkpointSkip = 0;
    public static boolean laptimeStats = false;

    public static double speedRate = 1d;
    public static int speedType = 0;
    public static double accelerationRate = 1d;
    public static int accelerationType = 0;

    public static ArrayList<Double[]> checkpointdata = new ArrayList<Double[]>();
    public static int checkpoints = 0;

    private Config() {}

    public static void load() {
        try {
            if (configFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    String[] s = line.split(" ");
                    if (s.length < 2) continue;
                    switch (s[0]) {
                    case "enabled":
                        enabled = Boolean.parseBoolean(s[1]); break;
                    case "extended":
                        extended = Boolean.parseBoolean(s[1]); break;
                    case "telemetryEnabled":
                        telemetryEnabled = Boolean.parseBoolean(s[1]); break;
                    case "telemetryDirectory":
                        telemetryPath = s[1]; break;
                    case "checkpointEnabled":
                        checkpointEnabled = Boolean.parseBoolean(s[1]); break;
                    case "checkpointFile":
                        checkpointFile = s[1]; break;
                    case "circularTrack":
                        circularTrack = Boolean.parseBoolean(s[1]); break;
                    case "checkpointSkip":
                        checkpointSkip = Integer.parseInt(s[1]); break;
                    case "laptimeStats":
                        laptimeStats = Boolean.parseBoolean(s[1]); break;
                    case "yoffset":
                        yOffset = Integer.parseInt(s[1]); break;
                    case "barType":
                        barType = Integer.parseInt(s[1]); break;
                    case "speedUnit":
                        setSpeedUnit(Integer.parseInt(s[1])); break;
                    case "accelerationUnit":
                        setAccelerationUnit(Integer.parseInt(s[1])); break;
                    case "timeFormat":
                        timeFormat = Integer.parseInt(s[1]); break;
                    default:
                        continue;
                    }
                }
                br.close();
            } else {
                save();
            }
            File telemetryDirectory = new File(telemetryPath);
            if (!telemetryDirectory.exists()) telemetryDirectory.mkdirs();
            File checkpointDirectory = new File(checkpointPath);
            if (!checkpointDirectory.exists()) checkpointDirectory.mkdirs();
        }
        catch (Exception e) {
        }

        if (barType > 2 || barType < 0) {
            barType = 0;
        }

        if (Config.checkpointEnabled) loadCheckpoints();
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write("enabled " + enabled + "\n");
            writer.write("extended " + extended + "\n");
            writer.write("telemetryEnabled " + telemetryEnabled + "\n");
            writer.write("telemetryDirectory " + telemetryPath + "\n");
            writer.write("checkpointEnabled " + checkpointEnabled + "\n");
            writer.write("checkpointFile " + checkpointFile + "\n");
            writer.write("circularTrack " + circularTrack + "\n");
            writer.write("checkpointSkip " + checkpointSkip + "\n");
            writer.write("laptimeStats " + laptimeStats + "\n");
            writer.write("yOffset " + yOffset + "\n");
            writer.write("barType " + barType + "\n");
            writer.write("speedUnit " + speedType + "\n");
            writer.write("accelerationUnit " + accelerationType + "\n");
            writer.write("timeFormat " + timeFormat + "\n");
            writer.close();
        }
        catch (Exception e) {
        }
    }

    public static void setSpeedUnit(int type) {
        Config.speedType = type;
        switch (type) {
        case 1:
            Config.speedRate = 3.6;
            Config.speedUnit = " km/h";
            break;
        case 2:
            Config.speedRate = 2.236936;
            Config.speedUnit = " mph";
            break;
        case 3:
            Config.speedRate = 1.943844;
            Config.speedUnit = " kt";
            break;
        case 0:
        default:
            Config.speedRate = 1d;
            Config.speedUnit = " m/s";
        }
    }

    public static void setAccelerationUnit(int type) {
        Config.speedType = type;
        switch (type) {
        case 1:
            Config.accelerationRate = 0.101972;
            Config.accelerationUnit = " g  ";
            break;
        case 0:
        default:
            Config.accelerationRate = 1d;
            Config.accelerationUnit = " m/s²";
        }
    }

    public static void loadCheckpoints() {
        checkpointdata.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(checkpointPath + checkpointFile));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (line.contains("time")) continue;
                String[] s = line.split(",");
                if (s.length < 5) continue;
                Double[] data = new Double[5];
                for (int i = 0; i < 5; i++) {
                    data[i] = Double.parseDouble(s[i]);
                }
                checkpointdata.add(data);
            }
            br.close();
        }
        catch (Exception e) {
        }
        checkpointFileLoaded = checkpointFile;
        checkpoints = checkpointdata.size();
    }
}
