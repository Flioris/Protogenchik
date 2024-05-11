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

package flioris.util;

import java.util.HashMap;
import java.util.Map;

public class ProtectedGuildsCache {
    private static final Map<String, GuildSettings> settings = new HashMap<>();

    public static GuildSettings get(String guildId) {
        return settings.get(guildId);
    }

    public static boolean contains(String guildId) {
        return settings.containsKey(guildId);
    }

    public static void put(String guildId, GuildSettings guildSettings) {
        settings.put(guildId, guildSettings);
    }

    public static void putAll(Map<String, GuildSettings> guildsSettings) {
        settings.putAll(guildsSettings);
    }

    public static void remove(String guildId) {
        settings.remove(guildId);
    }
}