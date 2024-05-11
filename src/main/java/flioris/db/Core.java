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

package flioris.db;

import flioris.db.impl.SQLiteDatabase;
import flioris.util.GuildSettings;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class Core {
    private final IDatabase db;

    public Core() {
        try {
            db = new SQLiteDatabase(new File("proto.db").getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addGuild(String guildId) {
        db.addGuild(guildId);
    }

    public void updateGuild(String guildId, String key, Object value) {
        db.updateGuild(guildId, key, value);
    }

    public Map<String, GuildSettings> getProtectedGuilds() {
        return db.getProtectedGuilds();
    }

    public boolean guildsContain(String guildId) {
        return db.guildsContain(guildId);
    }

    public void removeGuild(String guildId) {
        db.removeGuild(guildId);
    }

    public GuildSettings getGuildSettings(String guildId) {
        return db.getGuildSettings(guildId);
    }

    public Set<String> getGuilds() {
        return db.getGuilds();
    }

    public void blacklistAdd(String id) {
        db.blacklistAdd(id);
    }

    public boolean blacklistContains(String id) {
        return db.blacklistContains(id);
    }

    public void blacklistRemove(String id) {
        db.blacklistRemove(id);
    }
}