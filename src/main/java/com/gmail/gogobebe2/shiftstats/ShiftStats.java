package com.gmail.gogobebe2.shiftstats;

import code.husky.mysql.MySQL;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class ShiftStats extends JavaPlugin {
    private MySQL database;
    private static ShiftStats API;

    private final static String TABLE_NAME = "shift_players";
    private final static String COLUMN_INDEX_UUID = "UUID";
    private final static String COLUMN_INDEX_WINS = "Wins";
    private final static String COLUMN_INDEX_LOSSES = "Losses";
    private final static String COLUMN_INDEX_POINTS = "Points";
    private final static String COLUMN_INDEX_KILLS_WITH_BOW = "KillsWithBow";
    private final static String COLUMN_INDEX_KILLS_WITH_SWORD = "KillsWithSword";
    private final static String COLUMN_INDEX_KILLS_WITH_VOID = "KillsWithVoid";
    private final static String COLUMN_INDEX_KILLS_WITH_OTHER = "KillsWithOther";
    private final static String COLUMN_INDEX_KILLS = "Kills";
    private final static String COLUMN_INDEX_DEATHS = "Deaths";
    private final static String COLUMN_INDEX_ORES_MINED = "OresMined";
    private final static String COLUMN_INDEX_KITS = "Kits";

    public static ShiftStats getAPI() {
        return API;
    }

    @Override
    public void onEnable() {
        getLogger().info("Starting up " + this.getName() + ". If you need me to update this plugin, email at gogobebe2@gmail.com");
        saveDefaultConfig();
        setupDatabase();
        API = this;
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling " + this.getName() + ". If you need me to update this plugin, email at gogobebe2@gmail.com");
        closeDatabase();
    }

    private void setupDatabase() {
        final String CONFIG_PATH_HOSTNAME = "Hostname";
        final String CONFIG_PATH_PORT = "Port";
        final String CONFIG_PATH_DATABASE = "Database";
        final String CONFIG_PATH_USERNAME = "Username";
        final String CONFIG_PATH_PASSWORD = "Password";

        database = new MySQL(this,
                getConfig().getString(CONFIG_PATH_HOSTNAME),
                Integer.toString(getConfig().getInt(CONFIG_PATH_PORT)),
                getConfig().getString(CONFIG_PATH_DATABASE), getConfig().getString(CONFIG_PATH_USERNAME),
                getConfig().getString(CONFIG_PATH_PASSWORD));
        try {
            database.openConnection();
            Statement statement = database.getConnection().createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (" +
                            "`" + COLUMN_INDEX_UUID + "` VARCHAR(36), " +
                            "`" + COLUMN_INDEX_WINS + "` BIGINT, " +
                            "`" + COLUMN_INDEX_LOSSES + "` BIGINT, " +
                            "`" + COLUMN_INDEX_POINTS + "` BIGINT," +
                            "`" + COLUMN_INDEX_KILLS_WITH_BOW + "` BIGINT," +
                            "`" + COLUMN_INDEX_KILLS_WITH_SWORD + "` BIGINT," +
                            "`" + COLUMN_INDEX_KILLS_WITH_VOID + "` BIGINT," +
                            "`" + COLUMN_INDEX_KILLS_WITH_OTHER + "` BIGINT," +
                            "`" + COLUMN_INDEX_KILLS + "` BIGINT," +
                            "`" + COLUMN_INDEX_DEATHS + "` BIGINT," +
                            "`" + COLUMN_INDEX_ORES_MINED + "` BIGINT," +
                            "`" + COLUMN_INDEX_KITS + "` TEXT)"
            );

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
        try {
            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Statement safelyCreateStatement() throws SQLException, ClassNotFoundException {
        if (!database.checkConnection()) {
            database.openConnection();
        }
        return database.getConnection().createStatement();
    }

    private <TYPE> TYPE getEntry(UUID playerUUID, final String COLUMN_INDEX, Class<TYPE> type) throws SQLException, ClassNotFoundException, IllegalArgumentException {
        ResultSet resultSet = safelyCreateStatement().executeQuery(
                "SELECT " + COLUMN_INDEX + " FROM `" + TABLE_NAME + "` "
                        + "WHERE `" + COLUMN_INDEX_UUID + "` = '" + playerUUID.toString() + "';");
        if (!resultSet.next()) return null;
        if (type.isAssignableFrom(Long.class)) return type.cast(resultSet.getLong(COLUMN_INDEX));
        else if (type.isInstance(String[].class)) return type.cast(resultSet.getString(COLUMN_INDEX).split(","));
        else throw new IllegalArgumentException("You can only use Longs or String[] as types!");
    }

    private void addToEntry(UUID playerUUID, final String COLUMN_INDEX, String value, boolean exists) throws SQLException, ClassNotFoundException {
        Statement statement = safelyCreateStatement();
        if (exists) {
            statement.executeUpdate("INSERT INTO `" + TABLE_NAME + "` (`" + COLUMN_INDEX_UUID + "`, `" + COLUMN_INDEX + "`) " +
                    "VALUES ('" + playerUUID + "', '" + value + "');");
        } else {
            statement.executeUpdate("UPDATE `" + TABLE_NAME + "` " +
                    "SET `" + COLUMN_INDEX + "` = '" + value + "' " +
                    "WHERE `" + COLUMN_INDEX_UUID + "` = '" + playerUUID.toString() + "';");
        }
    }

    private long getLongEntry(UUID playerUUID, final String COLUMN_INDEX) throws SQLException, ClassNotFoundException {
        Long result = getEntry(playerUUID, COLUMN_INDEX, Long.class);
        if (result == null) return 0;
        else return result;
    }

    private void addToLongEntry(UUID playerUUID, final String COLUMN_INDEX, long addition) throws SQLException, ClassNotFoundException {
        long oldValue = getLongEntry(playerUUID, COLUMN_INDEX);
        long newValue = oldValue + addition;
        addToEntry(playerUUID, COLUMN_INDEX, String.valueOf(newValue), oldValue == 0);
    }

    public String[] getKits(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(playerUUID, COLUMN_INDEX_KITS, String[].class);
    }

    public void addKits(UUID playerUUID, String[] kits) throws SQLException, ClassNotFoundException {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < kits.length; i++) {
            string.append(kits[i]);
            if (i != kits.length - 1) string.append(",");
        }
        addToEntry(playerUUID, COLUMN_INDEX_KITS, string.toString(), getEntry(playerUUID, COLUMN_INDEX_KITS, String[].class) == null);
    }

    public long getWins(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getLongEntry(playerUUID, COLUMN_INDEX_WINS);
    }

    public long getLosses(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getLongEntry(playerUUID, COLUMN_INDEX_LOSSES);
    }

    public long getPoints(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getLongEntry(playerUUID, COLUMN_INDEX_POINTS);
    }

    public long getKills(UUID playerUUID, KillMethod killMethod) throws SQLException, ClassNotFoundException {
        return getLongEntry(playerUUID, killMethodToColumnIndex(killMethod));
    }

    public long getDeaths(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getLongEntry(playerUUID, COLUMN_INDEX_DEATHS);
    }

    public long getOresMined(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getLongEntry(playerUUID, COLUMN_INDEX_ORES_MINED);
    }

    public void addWins(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToLongEntry(playerUUID, COLUMN_INDEX_WINS, value);
    }

    public void addLosses(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToLongEntry(playerUUID, COLUMN_INDEX_LOSSES, value);
    }

    public void addPoints(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToLongEntry(playerUUID, COLUMN_INDEX_POINTS, value);
    }

    public void addKills(UUID playerUUID, long value, KillMethod... killMethods) throws SQLException, ClassNotFoundException {
        for (KillMethod killMethod : killMethods) {
            addToLongEntry(playerUUID, killMethodToColumnIndex(killMethod), value);
        }
        addToLongEntry(playerUUID, COLUMN_INDEX_KILLS, value);
    }

    public void addDeaths(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToLongEntry(playerUUID, COLUMN_INDEX_DEATHS, value);
    }

    public void addOresMined(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToLongEntry(playerUUID, COLUMN_INDEX_ORES_MINED, value);
    }

    private String killMethodToColumnIndex(KillMethod killMethod) {
        switch (killMethod) {
            case BOW:
                return COLUMN_INDEX_KILLS_WITH_BOW;
            case SWORD:
                return COLUMN_INDEX_KILLS_WITH_SWORD;
            case VOID:
                return COLUMN_INDEX_KILLS_WITH_VOID;
            case OTHER:
                return COLUMN_INDEX_KILLS_WITH_OTHER;
            case ALL : default:
                return COLUMN_INDEX_KILLS;
        }
    }

    public enum KillMethod {
        BOW, SWORD, VOID, OTHER, ALL
    }
}
