package com.gmail.gogobebe2.shiftstats;

import code.husky.mysql.MySQL;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class ShiftStats extends JavaPlugin {
    private MySQL database;

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

    @Override
    public void onEnable() {
        getLogger().info("Starting up " + this.getName() + ". If you need me to update this plugin, email at gogobebe2@gmail.com");
        saveDefaultConfig();
        setupDatabase();
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
                            "`" + COLUMN_INDEX_ORES_MINED + "` BIGINT)"
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

    private long getEntry(final String COLUMN_INDEX, UUID playerUUID) throws SQLException, ClassNotFoundException {
        ResultSet resultSet = safelyCreateStatement().executeQuery(
                "SELECT " + COLUMN_INDEX + " FROM `" + TABLE_NAME + "` "
                        + "WHERE `" + COLUMN_INDEX_UUID + "` = '" + playerUUID.toString() + "';");
        if (!resultSet.next()) return 0;
        return resultSet.getLong(COLUMN_INDEX);
    }

    private void addToEntry(final String COLUMN_INDEX, UUID playerUUID, long addition) throws SQLException, ClassNotFoundException {
        Statement statement = safelyCreateStatement();
        long oldValue = getEntry(COLUMN_INDEX, playerUUID);
        long newValue = oldValue + addition;
        if (oldValue == 0) {
            statement.executeUpdate("INSERT INTO `" + TABLE_NAME + "` (`" + COLUMN_INDEX_UUID + "`, `" + COLUMN_INDEX + "`) " +
                    "VALUES ('" + playerUUID + "', '" + newValue + "');");
        } else {
            statement.executeUpdate("UPDATE `" + TABLE_NAME + "` " +
                    "SET `" + COLUMN_INDEX + "` = '" + newValue + "' " +
                    "WHERE `" + COLUMN_INDEX_UUID + "` = '" + playerUUID.toString() + "';");
        }
    }

    public long getWins(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_WINS, playerUUID);
    }

    public long getLosses(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_LOSSES, playerUUID);
    }

    public long getPoints(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_POINTS, playerUUID);
    }

    public long getKills(UUID playerUUID, KillMethod killMethod) throws SQLException, ClassNotFoundException {
        return getEntry(killMethodToColumnIndex(killMethod), playerUUID);
    }

    public long getDeaths(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_DEATHS, playerUUID);
    }

    public long getOresMined(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_ORES_MINED, playerUUID);
    }

    public void addWins(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_WINS, playerUUID, value);
    }

    public void addLosses(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_LOSSES, playerUUID, value);
    }

    public void addPoints(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_POINTS, playerUUID, value);
    }

    public void addKills(UUID playerUUID, long value, KillMethod... killMethods) throws SQLException, ClassNotFoundException {
        for (KillMethod killMethod : killMethods) {
            addToEntry(killMethodToColumnIndex(killMethod), playerUUID, value);
        }
        addToEntry(COLUMN_INDEX_KILLS, playerUUID, value);
    }

    public void addDeaths(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_DEATHS, playerUUID, value);
    }

    public void addOresMined(UUID playerUUID, long value) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_ORES_MINED, playerUUID, value);
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
            case ALL: default:
                return COLUMN_INDEX_KILLS;
        }
    }

    public enum KillMethod {
        BOW, SWORD, VOID, OTHER, ALL
    }
}
