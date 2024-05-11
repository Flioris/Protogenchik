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

import flioris.util.GuildSettings;

import java.util.Map;
import java.util.Set;

public interface IDatabase {
    void addGuild(String guildId);

    void removeGuild(String guildId);

    void updateGuild(String guildId, String key, Object value);

    Map<String, GuildSettings> getProtectedGuilds();

    boolean guildsContain(String guildId);

    GuildSettings getGuildSettings(String guildId);

    Set<String> getGuilds();

    void blacklistAdd(String id);

    void blacklistRemove(String id);

    boolean blacklistContains(String id);
}