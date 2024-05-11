/**
 * Copyright © 2023 Flioris
 * <p>
 * This file is part of Protogenchik.
 * <p>
 * Protogenchik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package flioris.db.impl;

import flioris.db.IDatabase;
import flioris.util.GuildSettings;
import flioris.util.reaction.ReactionType;

import java.sql.*;
import java.util.*;

public class SQLiteDatabase implements IDatabase {
    private final String url;

    public SQLiteDatabase(String path) throws SQLException {
        url = "jdbc:sqlite:" + path;
        try (Connection cn = connect(); Statement st = cn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS guilds (" +
                    "'guild_id' STRING PRIMARY KEY NOT NULL, " +
                    "'bot_channel_id' STRING DEFAULT NULL, " +
                    "'lang' TEXT DEFAULT 'en', " +
                    "'antinuke' BOOLEAN DEFAULT FALSE, " +
                    "'member_remove_limit' INTEGER DEFAULT 3, " +
                    "'member_rename_limit' INTEGER DEFAULT 3, " +
                    "'channel_delete_limit' INTEGER DEFAULT 3, " +
                    "'channel_create_limit' INTEGER DEFAULT 3, " +
                    "'channel_rename_limit' INTEGER DEFAULT 3, " +
                    "'role_delete_limit' INTEGER DEFAULT 3, " +
                    "'role_create_limit' INTEGER DEFAULT 3, " +
                    "'role_rename_limit' INTEGER DEFAULT 3, " +
                    "'whitelist' TEXT DEFAULT NULL, " +
                    "'cooldown' INTEGER DEFAULT 60, " +
                    "'reaction_type' INTEGER DEFAULT 0);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS blacklist (" +
                    "'id' STRING PRIMARY KEY NOT NULL);");
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void addGuild(String guildId) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO guilds (guild_id) VALUES(?)");
            ps.setString(1, guildId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeGuild(String guildId) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM guilds WHERE guild_id = ?");
            ps.setString(1, guildId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGuild(String guildId, String key, Object value) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("UPDATE guilds SET " + key + " = ? WHERE guild_id = ?");
            ps.setObject(1, value);
            ps.setString(2, guildId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, GuildSettings> getProtectedGuilds() {
        try (Connection c = connect()) {
            ResultSet result = c.createStatement().executeQuery("SELECT * FROM guilds WHERE antinuke = TRUE");
            Map<String, GuildSettings> protectedGuilds = new HashMap<>();
            while (result.next()) {
                Set<String> whitelist = new HashSet<>();
                if (result.getString(13) != null) {
                    whitelist.addAll(Arrays.asList(result.getString(13).split(" ")));
                }
                protectedGuilds.put(result.getString(1), new GuildSettings(
                        result.getString(2),
                        result.getString(3),
                        result.getBoolean(4),
                        result.getByte(5),
                        result.getByte(6),
                        result.getByte(7),
                        result.getByte(8),
                        result.getByte(9),
                        result.getByte(10),
                        result.getByte(11),
                        result.getByte(12),
                        whitelist,
                        result.getShort(14),
                        ReactionType.fromValue(result.getInt(15))));
            }
            return protectedGuilds;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean guildsContain(String guildId) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM guilds WHERE guild_id = ?");
            ps.setString(1, guildId);
            ResultSet result = ps.executeQuery();
            return result.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GuildSettings getGuildSettings(String guildId) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM guilds WHERE guild_id = ?");
            ps.setString(1, guildId);
            ResultSet result = ps.executeQuery();
            if (!result.next()) {
                return null;
            }
            Set<String> whitelist = new HashSet<>();
            if (result.getString(13) != null) {
                whitelist.addAll(Arrays.asList(result.getString(13).split(" ")));
            }
            return new GuildSettings(
                    result.getString(2),
                    result.getString(3),
                    result.getBoolean(4),
                    result.getByte(5),
                    result.getByte(6),
                    result.getByte(7),
                    result.getByte(8),
                    result.getByte(9),
                    result.getByte(10),
                    result.getByte(11),
                    result.getByte(12),
                    whitelist,
                    result.getShort(14),
                    ReactionType.fromValue(result.getInt(15)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getGuilds() {
        try (Connection c = connect()) {
            ResultSet result = c.createStatement().executeQuery("SELECT * FROM guilds");
            Set<String> guilds = new HashSet<>();
            while (result.next()) {
                guilds.add(result.getString(1));
            }
            return guilds;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void blacklistAdd(String id) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO blacklist (id) VALUES(?)");
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void blacklistRemove(String id) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM blacklist WHERE id = ?");
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean blacklistContains(String id) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM blacklist WHERE id = ?");
            ps.setString(1, id);
            ResultSet result = ps.executeQuery();
            return result.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
