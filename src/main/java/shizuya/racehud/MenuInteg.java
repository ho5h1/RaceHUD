package shizuya.racehud;

import java.io.File;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;

public class MenuInteg implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(TITLE);
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory cat = builder.getOrCreateCategory(CAT);
            cat.addEntry(entryBuilder.startBooleanToggle(ENABLED, Config.enabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newVal -> Config.enabled = newVal)
                    .build())
                .addEntry(entryBuilder.startBooleanToggle(EXTENDED, Config.extended)
                    .setDefaultValue(true)
                    .setSaveConsumer(newVal -> Config.extended = newVal)
                    .setTooltip(TIP_EXTENDED)
                    .build())
                .addEntry(entryBuilder.startIntSlider(Y_OFFSET, Config.yOffset, 0, 300)
                    .setDefaultValue(36)
                    .setSaveConsumer(newVal -> Config.yOffset = newVal)
                    .setTooltip(Y_OFFSET_TOOLTIP)
                    .build())
                .addEntry(entryBuilder.startEnumSelector(BAR_TYPE, BarType.class, BarType.values()[Config.barType])
                    .setDefaultValue(BarType.PACKED)
                    .setTooltip(TIP_BAR, TIP_BAR_PACKED, TIP_BAR_MIXED, TIP_BAR_BLUE)
                    .setSaveConsumer(newVal -> Config.barType = newVal.ordinal())
                    .setEnumNameProvider(value -> Text.translatable("racehud.option.bar_type." + value.toString()))
                    .build())
                .addEntry(entryBuilder.startEnumSelector(SPEED_FORMAT, SpeedFormat.class, SpeedFormat.values()[Config.speedType])
                    .setDefaultValue(SpeedFormat.MS)
                    .setSaveConsumer(newVal -> Config.setSpeedUnit(newVal.ordinal()))
                    .setEnumNameProvider(value -> Text.translatable("racehud.option.speed_format." + value.toString()))
                    .build())
                .addEntry(entryBuilder.startEnumSelector(ACCELERATION_FORMAT, AccelerationFormat.class, AccelerationFormat.values()[Config.accelerationType])
                    .setDefaultValue(AccelerationFormat.MSS)
                    .setSaveConsumer(newVal -> Config.setAccelerationUnit(newVal.ordinal()))
                    .setEnumNameProvider(value -> Text.translatable("racehud.option.acceleration_format." + value.toString()))
                    .build())
                .addEntry(entryBuilder.startEnumSelector(TIME_FORMAT, TimeFormat.class, TimeFormat.values()[Config.timeFormat])
                    .setDefaultValue(TimeFormat.S)
                    .setSaveConsumer(newVal -> Config.timeFormat = newVal.ordinal())
                    .setEnumNameProvider(value -> Text.translatable("racehud.option.time_format." + value.toString()))
                    .build());

            SubCategoryBuilder telemetry = entryBuilder.startSubCategory(TELEMETRY).setExpanded(true);
            telemetry.add(entryBuilder.startBooleanToggle(TELEMETRY_ENABLED, Config.telemetryEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newVal -> Config.telemetryEnabled = newVal)
                    .build());
            telemetry.add(entryBuilder.startStrField(TELEMETRY_DIRECTORY, Config.telemetryPath)
                    .setDefaultValue(Config.configDirectory + File.separator + "racehud" + File.separator + "telemetry" + File.separator)
                    .setSaveConsumer(newVal -> Config.telemetryPath = newVal)
                    .setTooltip(TELEMETRY_DIRECTORY_TOOLTIP)
                    .build());
            cat.addEntry(telemetry.build());

            SubCategoryBuilder checkpoints = entryBuilder.startSubCategory(CHECKPOINT).setExpanded(true);
            checkpoints.add(entryBuilder.startBooleanToggle(CHECKPOINT_ENABLED, Config.checkpointEnabled)
                    .setDefaultValue(false)
                    .setSaveConsumer(newVal -> Config.checkpointEnabled = newVal)
                    .build());
            checkpoints.add(entryBuilder.startStrField(CHECKPOINT_DIRECTORY, Config.checkpointPath)
                    .setDefaultValue(Config.configDirectory + File.separator + "racehud" + File.separator + "checkpoints" + File.separator)
                    .setSaveConsumer(newVal -> Config.checkpointPath = newVal)
                    .setTooltip(CHECKPOINT_DIRECTORY_TOOLTIP)
                    .build());
            checkpoints.add(entryBuilder.startStrField(CHECKPOINT_FILE, Config.checkpointFile)
                    .setDefaultValue("checkpoint_file.cf")
                    .setSaveConsumer(newVal -> Config.checkpointFile = newVal)
                    .setTooltip(CHECKPOINT_FILE_TOOLTIP)
                    .build());
            checkpoints.add(entryBuilder.startBooleanToggle(CIRCULAR_TRACK, Config.circularTrack)
                    .setDefaultValue(false)
                    .setSaveConsumer(newVal -> Config.circularTrack = newVal)
                    .setTooltip(CIRCULAR_TRACK_TOOLTIP)
                    .build());
            checkpoints.add(entryBuilder.startBooleanToggle(LAPTIME_STATS, Config.laptimeStats)
                    .setDefaultValue(false)
                    .setSaveConsumer(newVal -> Config.laptimeStats = newVal)
                    .setTooltip(LAPTIME_STATS_TOOLTIP)
                    .build());
            checkpoints.add(entryBuilder.startIntField(CHECKPOINT_SKIP, Config.checkpointSkip)
                    .setDefaultValue(0)
                    .setSaveConsumer(newVal -> Config.checkpointSkip = newVal)
                    .setTooltip(CHECKPOINT_SKIP_TOOLTIP)
                    .build());
            cat.addEntry(checkpoints.build());

            builder.setSavingRunnable(() -> Config.save());
            return builder.build();
        };
    }

    public enum BarType {
        PACKED, MIXED, BLUE
    }
    public enum SpeedFormat {
        MS, KMPH, MPH, KT
    }
    public enum AccelerationFormat {
        MSS, G
    }
    public enum TimeFormat {
        S, MS
    }

    private static final MutableText
        TITLE = Text.translatable("racehud.config.title"),
        CAT = Text.translatable("racehud.config.cat"),
        ENABLED = Text.translatable("racehud.option.enabled"),
        EXTENDED = Text.translatable("racehud.option.extended"),
        TELEMETRY = Text.translatable("racehud.option.telemetry"),
        TELEMETRY_ENABLED = Text.translatable("racehud.option.telemetry_enabled"),
        TELEMETRY_DIRECTORY = Text.translatable("racehud.option.telemetry_directory"),
        TELEMETRY_DIRECTORY_TOOLTIP = Text.translatable("racehud.tooltip.telemetry_directory"),
        CHECKPOINT = Text.translatable("racehud.option.checkpoint"),
        CHECKPOINT_ENABLED = Text.translatable("racehud.option.checkpoint_enabled"),
        CHECKPOINT_DIRECTORY = Text.translatable("racehud.option.checkpoint_directory"),
        CHECKPOINT_DIRECTORY_TOOLTIP = Text.translatable("racehud.tooltip.checkpoint_directory"),
        CHECKPOINT_FILE = Text.translatable("racehud.option.checkpoint_file"),
        CHECKPOINT_FILE_TOOLTIP = Text.translatable("racehud.tooltip.checkpoint_file"),
        CIRCULAR_TRACK = Text.translatable("racehud.option.circular_track"),
        CIRCULAR_TRACK_TOOLTIP = Text.translatable("racehud.tooltip.circular_track"),
        LAPTIME_STATS = Text.translatable("racehud.option.laptime_stats"),
        LAPTIME_STATS_TOOLTIP = Text.translatable("racehud.tooltip.laptime_stats"),
        CHECKPOINT_SKIP = Text.translatable("racehud.option.checkpoint_skip"),
        CHECKPOINT_SKIP_TOOLTIP = Text.translatable("racehud.tooltip.checkpoint_skip"),
        BAR_TYPE = Text.translatable("racehud.option.bar_type"),
        SPEED_FORMAT = Text.translatable("racehud.option.speed_format"),
        ACCELERATION_FORMAT = Text.translatable("racehud.option.acceleration_format"),
        TIME_FORMAT = Text.translatable("racehud.option.time_format"),
        TIP_EXTENDED = Text.translatable("racehud.tooltip.extended"),
        TIP_BAR = Text.translatable("racehud.tooltip.bar_type"),
        TIP_BAR_PACKED = Text.translatable("racehud.tooltip.bar_type.packed"),
        TIP_BAR_MIXED = Text.translatable("racehud.tooltip.bar_type.mixed"),
        TIP_BAR_BLUE = Text.translatable("racehud.tooltip.bar_type.blue"),
        Y_OFFSET = Text.translatable("racehud.option.y_offset"),
        Y_OFFSET_TOOLTIP = Text.translatable("racehud.tooltip.y_offset");
}
