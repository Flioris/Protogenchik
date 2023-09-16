/**
 * Copyright © 2023 Flioris
 *
 * This file is part of Protogenchik.
 *
 * Protogenchik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package flioris.db.impl;

import flioris.db.IDatabase;
import flioris.util.GuildSettings;
import flioris.util.reaction.ReactionType;

import java.sql.*;
import java.util.HashSet;

public class SQLiteDatabase implements IDatabase {
    private final String url;

    public SQLiteDatabase(String path) throws SQLException {
        url = "jdbc:sqlite:"+path;
        try (Connection cn = connect(); Statement st = cn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS guilds ('guild_id' INTEGER PRIMARY KEY NOT NULL, " +
                    "'bot_channel_id' INTEGER DEFAULT 0, " +
                    "'lang' TEXT DEFAULT 'en', " +
                    "'antinuke' INTEGER DEFAULT 0, " +
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
            st.executeUpdate("CREATE TABLE IF NOT EXISTS blacklist ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'text' TEXT NOT NULL);");
        }
    }

    private Connection connect() throws SQLException {return DriverManager.getConnection(url);}

    @Override
    public void addGuild(long guildId) {
        try (Connection c = connect()) {
            c.createStatement().executeUpdate("INSERT INTO guilds (guild_id) VALUES('"+guildId+"')");
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public void removeGuild(long guildId) {
        try (Connection c = connect()) {
            c.createStatement().executeUpdate("DELETE FROM guilds WHERE guild_id = '"+guildId+"'");
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public void updateGuild(long guildId, String key, Object value) {
        try (Connection c = connect()) {
            c.createStatement().executeUpdate("UPDATE guilds SET "+key+" = '"+value+"' WHERE guild_id = '"+guildId+"'");
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public Boolean guildsContain(long guildId) {
        try (Connection c = connect()) {
            ResultSet result = c.createStatement().executeQuery("SELECT * FROM guilds WHERE guild_id = '"+guildId+"'");
            return result.next();
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public GuildSettings getGuildSettings(long guildId) {
        try (Connection c = connect()) {
            ResultSet result = c.createStatement().executeQuery("SELECT * FROM guilds WHERE guild_id = '"+guildId+"'");
            result.next();

            HashSet<Long> whitelist = new HashSet<>();
            if (result.getString(13) != null) for (String str : result.getString(13).split(" ")) whitelist.add(Long.parseLong(str));

            return new GuildSettings(guildId,
                    result.getLong(2),
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
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public HashSet<Long> getGuilds() {
        try (Connection c = connect()) {
            ResultSet result = c.createStatement().executeQuery("SELECT * FROM guilds");
            HashSet<Long> guilds = new HashSet<>();
            while (result.next()) guilds.add(result.getLong(1));
            return guilds;
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public void blacklistAdd(String text) {
        try (Connection c = connect()) {
            c.createStatement().executeUpdate("INSERT INTO blacklist (text) VALUES('"+text+"')");
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public void blacklistRemove(String text) {
        try (Connection c = connect()) {
            c.createStatement().executeUpdate("DELETE FROM blacklist WHERE text = '"+text+"'");
        } catch (SQLException e) {throw new RuntimeException(e);}
    }

    @Override
    public Boolean blacklistContains(String text) {
        try (Connection c = connect()) {
            ResultSet result = c.createStatement().executeQuery("SELECT * FROM blacklist WHERE text = '"+text+"'");
            return result.next();
        } catch (SQLException e) {throw new RuntimeException(e);}
    }
}
