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

package flioris.util.message;

import java.util.HashMap;
import java.util.Map;

public class BotMessageCache {
    private static final Map<String, BotMessage> messages = new HashMap<>();

    public static void put(String id, BotMessage message) {
        messages.put(id, message);
    }

    public static void remove(String id) {
        messages.remove(id);
    }

    public static boolean contains(String id) {
        return messages.containsKey(id);
    }

    public static BotMessage get(String id) {
        return messages.get(id);
    }
}
