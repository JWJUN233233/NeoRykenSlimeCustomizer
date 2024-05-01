package org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

public enum Version {

    v1_7_R1,
    v1_7_R2,
    v1_7_R3,
    v1_7_R4,
    v1_8_R1,
    v1_8_R2,
    v1_8_R3,
    v1_9_R1,
    v1_9_R2,
    v1_10_R1,
    v1_11_R1,
    v1_12_R1,
    v1_13_R1,
    v1_13_R2,
    v1_13_R3,
    v1_14_R1,
    v1_14_R2,
    v1_15_R1,
    v1_15_R2,
    v1_16_R1,
    v1_16_R2,
    v1_16_R3,
    v1_17_R1,
    v1_18_R1,
    v1_18_R2,
    v1_19_R1,
    v1_19_R2,
    v1_19_R3,
    v1_20_R1,
    v1_20_R2,
    v1_20_R3(4),
    v1_20_R4(5),
    v1_21_R1,
    v1_21_R2,
    v1_21_R3,
    v1_22_R1,
    v1_22_R2,
    v1_22_R3,
    v1_23_R1,
    v1_23_R2,
    v1_23_R3;

    private Integer value;
    private int[] minorVersions = null;
    private String shortVersion;
    private static int subVersion = 0;
    private static Version current = null;
    private static MinecraftPlatform platform = null;

    static {
        getCurrent();
    }

    Version(int... versions) {
        this();
        minorVersions = versions;
    }

    Version() {
        try {
            this.value = Integer.valueOf(this.name().replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
        }
        shortVersion = this.name().substring(0, this.name().length() - 3);
    }

    public Integer getValue() {
        return value;
    }

    public static boolean isPaperBranch() {
        switch (getPlatform()) {
        case mohist:
            break;
        case purpur:
        case folia:
        case paper:
        case pufferfish:
            return true;
        case craftbukkit:
        case spigot:
            return false;
        }
        return false;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public String getShortFormated() {
        return shortVersion.replace("v", "").replace("_", ".") + ".x";
    }

    public String getFormated() {
        return shortVersion.replace("v", "").replace("_", ".") + "." + subVersion;
    }

    public static boolean isPaper() {
        return getPlatform().equals(MinecraftPlatform.paper) || getPlatform().equals(MinecraftPlatform.folia) || getPlatform().equals(MinecraftPlatform.purpur);
    }

    public static boolean isSpigot() {
        return !getPlatform().equals(MinecraftPlatform.craftbukkit);
    }

    public static boolean isFolia() {
        return getPlatform().equals(MinecraftPlatform.folia);
    }

    public static boolean isPurpur() {
        return getPlatform().equals(MinecraftPlatform.purpur);
    }

    public static MinecraftPlatform getPlatform() {
        if (platform != null)
            return platform;

        if (Bukkit.getVersion().toLowerCase().contains("mohist")) {
            platform = MinecraftPlatform.mohist;
            return platform;
        }

        if (Bukkit.getVersion().toLowerCase().contains("arclight")) {
            platform = MinecraftPlatform.arclight;
            return platform;
        }

        if (Bukkit.getVersion().toLowerCase().contains("purpur")) {
            platform = MinecraftPlatform.purpur;
            return platform;
        }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            platform = MinecraftPlatform.folia;
            return platform;
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            platform = MinecraftPlatform.paper;
            return platform;
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("org.spigotmc.SpigotConfig");
            platform = MinecraftPlatform.spigot;
        } catch (ClassNotFoundException e1) {
            platform = MinecraftPlatform.craftbukkit;
        }

        return platform;
    }

    public static Version getCurrent() {
        if (current != null)
            return current;
        String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");

        try {
            String vr = Bukkit.getBukkitVersion().split("-", 2)[0];
            String[] split = vr.split("\\.");
            if (split.length <= 2)
                subVersion = 0;
            else {
                subVersion = Integer.parseInt(split[2]);
            }
        } catch (Throwable e) {
        }

        String vv = v[v.length - 1];
        for (Version one : values()) {
            if (one.name().equalsIgnoreCase(vv)) {
                current = one;
                break;
            }
        }

        if (current == null) {
            String ve = Bukkit.getBukkitVersion().split("-", 2)[0];
            main: for (Version one : values()) {
                if (one.name().equalsIgnoreCase(ve)) {
                    current = one;
                    break;
                }
                List<String> cleanVersion = one.getMinorVersions();
                for (String cv : cleanVersion) {
                    if (ve.equalsIgnoreCase(cv)) {
                        current = one;
                        break main;
                    }
                }
            }
        }

        if (current == null) {
            String ve = Bukkit.getBukkitVersion().split("-", 2)[0];
            for (Version one : values()) {
                if (ve.startsWith(one.getSimplifiedVersion())) {
                    current = one;
                    Bukkit.getConsoleSender().sendMessage("§c[RykenSlimeCustomizer] §eServer version detection needs aditional update");
                    break;
                }
            }
        }

        return current;
    }

    public boolean isLower(Version version) {
        return getValue() < version.getValue();
    }

    public boolean isHigher(Version version) {
        return getValue() > version.getValue();
    }

    public boolean isEqualOrLower(Version version) {
        return getValue() <= version.getValue();
    }

    public boolean isEqualOrHigher(Version version) {
        return getValue() >= version.getValue();
    }

    public static boolean isCurrentEqualOrHigher(Version v) {
        return current.getValue() >= v.getValue();
    }

    public static boolean isCurrentHigher(Version v) {
        return current.getValue() > v.getValue();
    }

    public static boolean isCurrentLower(Version v) {
        return current.getValue() < v.getValue();
    }

    public static boolean isCurrentEqualOrLower(Version v) {
        return current.getValue() <= v.getValue();
    }

    public static boolean isCurrentEqual(Version v) {
        return current.getValue() == v.getValue();
    }

    public static boolean isCurrentSubEqualOrHigher(int subVersion) {
        return Version.subVersion >= subVersion;
    }

    public static boolean isCurrentSubHigher(int subVersion) {
        return Version.subVersion > subVersion;
    }

    public static boolean isCurrentSubLower(int subVersion) {
        return Version.subVersion < subVersion;
    }

    public static boolean isCurrentSubEqualOrLower(int subVersion) {
        return Version.subVersion <= subVersion;
    }

    public static boolean isCurrentSubEqual(int subVersion) {
        return Version.subVersion == subVersion;
    }

    public static Integer convertVersion(String v) {
        v = v.replaceAll("[^\\d.]", "");
        Integer version = 0;
        if (v.contains(".")) {
            String lVersion = "";
            for (String one : v.split("\\.")) {
                String s = one;
                if (s.length() == 1)
                    s = "0" + s;
                lVersion += s;
            }

            try {
                version = Integer.parseInt(lVersion);
            } catch (Exception e) {
            }
        } else {
            try {
                version = Integer.parseInt(v);
            } catch (Exception e) {
            }
        }
        return version;
    }

    public static String deconvertVersion(Integer v) {

        StringBuilder version = new StringBuilder();

        String vs = String.valueOf(v);

        while (vs.length() > 0) {
            int subv = 0;
            try {
                if (vs.length() > 2) {
                    subv = Integer.parseInt(vs.substring(vs.length() - 2));
                    version.insert(0, "." + subv);
                } else {
                    subv = Integer.parseInt(vs);
                    version.insert(0, subv);
                }
            } catch (Throwable e) {

            }
            if (vs.length() > 2)
                vs = vs.substring(0, vs.length() - 2);
            else
                break;
        }

        return version.toString();
    }

    private String getSimplifiedVersion() {
        return this.name().substring(1).replace("_", ".").split("R", 2)[0];
    }

    public List<String> getMinorVersions() {

        if (minorVersions == null)
            return new ArrayList<String>();

        return Arrays.stream(minorVersions)
            .mapToObj(version -> getSimplifiedVersion() + version)
            .collect(Collectors.toList());
    }
}
