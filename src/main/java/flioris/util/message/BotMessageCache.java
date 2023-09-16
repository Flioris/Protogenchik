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

package flioris.util.message;

import java.util.HashSet;

public class BotMessageCache {
    private static HashSet<BotMessage> messages = new HashSet<>();

    public static void add(BotMessage message) {messages.add(message);}

    public static void remove(long id) {
        for (BotMessage message : messages) {
            if (message.getId()==id) messages.remove(message);
            break;
        }
    }

    public static Boolean contains(long id) {
        for (BotMessage message : messages) if (message.getId()==id) return true;
        return false;
    }

    public static BotMessage get(long id) {
        for (BotMessage message : messages) if (message.getId()==id) return message;
        return null;
    }
}
