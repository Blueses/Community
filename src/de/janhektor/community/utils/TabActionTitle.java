package de.janhektor.community.utils;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabActionTitle {

    private static Class<?> title = getNMSClass("PacketPlayOutTitle");
    private static Class<?> enumtitleaction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
    private static Class<?> chatserial;

    static{
        if(compareMinecraftVersionServerIsHigherOrEqual("1.8.3")){
            chatserial = getNMSClass("IChatBaseComponent$ChatSerializer");
        }else{
            chatserial = getNMSClass("ChatSerializer");
        }
    }

    public static void sendActionBar(Player p, String text) {
        try {
            sendPacket(p, "PacketPlayOutChat", new Class[]{getNMSClass("IChatBaseComponent"), byte.class}, chatserial.getMethod("a", String.class).invoke(null, "{\"text\": \"" + text + "\"}"), (byte) 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendHeaderAndFooter(Player p, String header, String footer) {
        try {
            Object packet = getNMSClass("PacketPlayOutPlayerListHeaderFooter").newInstance();
            getField(packet.getClass().getDeclaredField("a")).set(packet, chatserial.getMethod("a", String.class).invoke(null, "{'text': '" + header + "'}"));
            getField(packet.getClass().getDeclaredField("b")).set(packet, chatserial.getMethod("a", String.class).invoke(null, "{'text': '" + footer + "'}"));
            sendPacket(p, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTitles(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTimings(p, fadeIn, stay, fadeOut);
        sendTitle(p, title);
        sendSubTitle(p, subtitle);
    }

    public static void sendTitle(Player p, String title, int fadeIn, int stay, int fadeOut) {
        sendTimings(p, fadeIn, stay, fadeOut);
        sendTitle(p, title);
    }

    public static void sendTitle(Player p, String title) {
        try {
            Object t = TabActionTitle.title.newInstance();
            Field f = t.getClass().getDeclaredField("a");
            f.setAccessible(true);
            f.set(t, getField(enumtitleaction.getDeclaredField("TITLE")).get(null));
            f = t.getClass().getDeclaredField("b");
            f.setAccessible(true);
            f.set(t, chatserial.getMethod("a", String.class).invoke(null, "{'text': '" + title + "'}"));
            sendPacket(p, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSubTitle(Player p, String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTimings(p, fadeIn, stay, fadeOut);
        sendSubTitle(p, subtitle);
    }

    public static void sendSubTitle(Player p, String subtitle) {
        try {
            Object t = title.newInstance();
            Field f = t.getClass().getDeclaredField("a");
            f.setAccessible(true);
            f.set(t, getField(enumtitleaction.getDeclaredField("SUBTITLE")).get(null));
            f = t.getClass().getDeclaredField("b");
            f.setAccessible(true);
            f.set(t, chatserial.getMethod("a", String.class).invoke(null, "{'text': '" + subtitle + "'}"));
            sendPacket(p, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendTimings(Player p, int fadeIn, int stay, int fadeOut) {
        try {
            Object t = title.newInstance();
            Field f = t.getClass().getDeclaredField("a");
            f.setAccessible(true);
            f.set(t, getField(enumtitleaction.getDeclaredField("TIMES")).get(null));
            f = t.getClass().getDeclaredField("c");
            f.setAccessible(true);
            f.set(t, fadeIn);
            f = t.getClass().getDeclaredField("d");
            f.setAccessible(true);
            f.set(t, stay);
            f = t.getClass().getDeclaredField("e");
            f.setAccessible(true);
            f.set(t, fadeOut);
            sendPacket(p, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reset(Player p) {
        try {
            Object t = title.newInstance();
            Field f = t.getClass().getDeclaredField("a");
            f.setAccessible(true);
            f.set(t, getField(enumtitleaction.getDeclaredField("RESET")).get(null));
            sendPacket(p, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clear(Player p) {
        try {
            Object t = title.newInstance();
            Field f = t.getClass().getDeclaredField("a");
            f.setAccessible(true);
            f.set(t, getField(enumtitleaction.getDeclaredField("CLEAR")).get(null));
            sendPacket(p, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void sendPacket(Player p, Object packet) {
        try {
            Object nmsPlayer = getNMSPlayer(p);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendPacket(Player p, String packetName, Class<?>[] parameterclass, Object... parameters) {
        try {
            Object nmsPlayer = getNMSPlayer(p);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + "." + packetName).getConstructor(parameterclass).newInstance(parameters);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1) + ".";
    }

    private static Class<?> getNMSClass(String className) {
        String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    private static Object getNMSPlayer(Player p) {
        try {
            return p.getClass().getMethod("getHandle").invoke(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Field getField(Field f) {
        f.setAccessible(true);
        return f;
    }

    private static boolean compareMinecraftVersionServerIsHigherOrEqual(String version) {
        String serverVersion = Bukkit.getVersion();
        serverVersion = serverVersion.substring(serverVersion.indexOf("(MC: ") + 5, serverVersion.length());
        serverVersion = serverVersion.substring(0, serverVersion.lastIndexOf(")"));
        String[] serverVersionArray = serverVersion.split("\\.");
        String[] toCompareVersionArray = version.split("\\.");

        if (serverVersionArray.length == 2) {
            int serverFirst = Integer.valueOf(serverVersionArray[0]);
            int toCompareFirst = Integer.valueOf(toCompareVersionArray[0]);
            if (toCompareFirst != serverFirst) {
                return toCompareFirst < serverFirst;
            }
            int serverSecond = Integer.valueOf(serverVersionArray[1]);
            int toCompareSecond = Integer.valueOf(toCompareVersionArray[1]);
            if (toCompareSecond != serverSecond) {
                return toCompareSecond < serverSecond;
            }
            if (toCompareVersionArray.length == 3) {
                return false;
            }
            return true;
        }
        if (serverVersionArray.length == 3) {
            int serverFirst = Integer.valueOf(serverVersionArray[0]);
            int toCompareFirst = Integer.valueOf(toCompareVersionArray[0]);
            if (toCompareFirst != serverFirst) {
                return toCompareFirst < serverFirst;
            }
            int serverSecond = Integer.valueOf(serverVersionArray[1]);
            int toCompareSecond = Integer.valueOf(toCompareVersionArray[1]);
            if (toCompareSecond != serverSecond) {
                return toCompareSecond < serverSecond;
            }
            if (toCompareVersionArray.length != 3) {
                return true;
            }
            int serverThird = Integer.valueOf(serverVersionArray[2]);
            int toCompareThird = Integer.valueOf(toCompareVersionArray[2]);
            if (toCompareThird != serverThird) {
                return toCompareThird < serverThird;
            }
            return true;
        }
        return false;
    }

}
