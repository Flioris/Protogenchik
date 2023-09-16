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

package flioris.util.history;

import flioris.util.Renamed;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;

import java.util.HashMap;
import java.util.HashSet;

public class HistoryCache {
    private static HashMap<Long, HashSet<History>> history = new HashMap<>();

    public static HashSet<History> get(long guildId, long memberId) {
        HashSet<History> historySet = new HashSet<>();
        for (History h : history.get(guildId)) if (h.getMemberId()==memberId) historySet.add(h);
        return historySet;
    }

    public static HashSet<History> get(long guildId, long memberId, ActionType actionType) {
        HashSet<History> historySet = new HashSet<>();
        for (History h : get(guildId, memberId)) if (h.getType() == actionType) historySet.add(h);
        return historySet;
    }

    public static HashSet<History> get(long guildId, long memberId, long id) {
        HashSet<History> historySet = new HashSet<>();
        for (History h : get(guildId, memberId)) {
            switch (h.getObject()) {
                case ChannelUnion channel -> {
                    if (channel.getIdLong()==id) historySet.add(h);
                }
                case Role role -> {
                    if (role.getIdLong()==id)historySet.add(h);
                }
                case Renamed renamed -> {
                    if (renamed.getId()==id) historySet.add(h);
                }
                default -> {}
            }
        }
        return historySet;
    }

    public static boolean contains(long guildId) {return history.containsKey(guildId);}

    public static boolean contains(long guildId, long memberId, Renamed renamed) {
        for (History h : get(guildId, memberId, ActionType.ROLE_UPDATE)) {
            if (((Renamed) h.getObject()).getId()==renamed.getId()) return true;
        }
        for (History h : get(guildId, memberId, ActionType.CHANNEL_UPDATE)) {
            if (((Renamed) h.getObject()).getId()==renamed.getId()) return true;
        }
        return false;
    }

    public static void add(long guildId, HashSet<History> h) {history.put(guildId, h);}

    public static void add(long guildId, History h) {history.get(guildId).add(h);}

    public static void rem(long guildId, History h) {history.get(guildId).remove(h);}
}