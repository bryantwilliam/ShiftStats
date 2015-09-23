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
        String CONFIG_PATH_HOSTNAME = "HostName";
        String CONFIG_PATH_PORT = "Port";
        String CONFIG_PATH_DATABASE = "Database";
        String CONFIG_PATH_USERNAME = "Username";
        String CONFIG_PATH_PASSWORD = "Password";

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

    public long getKills(UUID playerUUID, KillMethod... killMethods) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_KILLS, playerUUID);
    }

    public long getDeaths(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_DEATHS, playerUUID);
    }

    public long getOresMined(UUID playerUUID) throws SQLException, ClassNotFoundException {
        return getEntry(COLUMN_INDEX_ORES_MINED, playerUUID);
    }

    public void addWins(UUID playerUUID, long newValue) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_WINS, playerUUID, newValue);
    }

    public void addLosses(UUID playerUUID, long newValue) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_LOSSES, playerUUID, newValue);
    }

    public void addPoints(UUID playerUUID, long newValue) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_POINTS, playerUUID, newValue);
    }

    public void addKills(UUID playerUUID, long newValue, KillMethod... killMethods) throws SQLException, ClassNotFoundException {
        String COLUMN_INDEX;
        for (KillMethod killMethod : killMethods) {
            switch (killMethod) {
                case BOW:
                    COLUMN_INDEX = COLUMN_INDEX_KILLS_WITH_BOW;
                    break;
                case SWORD:
                    COLUMN_INDEX = COLUMN_INDEX_KILLS_WITH_SWORD;
                    break;
                case VOID:
                    COLUMN_INDEX = COLUMN_INDEX_KILLS_WITH_VOID;
                    break;
                case OTHER:
                default:
                    COLUMN_INDEX = COLUMN_INDEX_KILLS_WITH_OTHER;
            }
            addToEntry(COLUMN_INDEX, playerUUID, newValue);
        }
        addToEntry(COLUMN_INDEX_KILLS, playerUUID, newValue);
    }

    public void addDeaths(UUID playerUUID, long newValue) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_DEATHS, playerUUID, newValue);
    }

    public void addOresMined(UUID playerUUID, long newValue) throws SQLException, ClassNotFoundException {
        addToEntry(COLUMN_INDEX_ORES_MINED, playerUUID, newValue);
    }

    public enum KillMethod {
        BOW, SWORD, VOID, OTHER
    }
}
