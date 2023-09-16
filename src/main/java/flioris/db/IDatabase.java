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

import flioris.util.GuildSettings;

import java.util.HashSet;

public interface IDatabase {
    void addGuild(long guildId);

    void removeGuild(long guildId);

    void updateGuild(long guildId, String key, Object value);

    Boolean guildsContain(long guildId);

    GuildSettings getGuildSettings(long guildId);

    HashSet<Long> getGuilds();

    void blacklistAdd(String text);

    void blacklistRemove(String text);

    Boolean blacklistContains(String text);
}