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

package flioris.db;

import flioris.db.impl.SQLiteDatabase;
import flioris.util.GuildSettings;

import java.io.File;
import java.util.HashSet;

public class Core {
    private final IDatabase db;

    public Core() {
        try {
            db = new SQLiteDatabase(new File("proto.db").getAbsolutePath());
        } catch (Exception e) {throw new RuntimeException(e);}
    }

    public void addGuild(long guildId) {
        db.addGuild(guildId);
    }

    public void updateGuild(long guildId, String key, Object value) {
        db.updateGuild(guildId, key, value);
    }

    public Boolean guildsContain(long guildId) {
        return db.guildsContain(guildId);
    }

    public void removeGuild(long guildId) {
        db.removeGuild(guildId);
    }

    public GuildSettings getGuildSettings(long guildId) {
        return db.getGuildSettings(guildId);
    }

    public HashSet<Long> getGuilds() {
        return db.getGuilds();
    }

    public void blacklistAdd(String text) {
        db.blacklistAdd(text);
    }

    public Boolean blacklistContains(String text) {
        return db.blacklistContains(text);
    }

    public void blacklistRemove(String text) {
        db.blacklistRemove(text);
    }
}