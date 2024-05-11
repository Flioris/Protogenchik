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

package flioris.util.history;

import flioris.util.Renamed;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;

import java.util.*;

public class HistoryCache {
    private static final Map<String, Map<String, Set<History>>> history = new HashMap<>();

    public static Set<History> get(String guildId, String memberId) {
        Map<String, Set<History>> guildHistory = history.get(guildId);

        return guildHistory == null ? new HashSet<>() : guildHistory.get(memberId);
    }

    public static Set<History> get(String guildId, String memberId, ActionType actionType) {
        Set<History> historySet = new HashSet<>();

        for (History memberHistory : get(guildId, memberId)) {
            if (memberHistory.getType() == actionType) {
                historySet.add(memberHistory);
            }
        }

        return historySet;
    }

    public static HashSet<History> get(String guildId, String memberId, String id) {
        HashSet<History> historySet = new HashSet<>();

        for (History memberHistory : get(guildId, memberId)) {
            switch (memberHistory.getObject()) {
                case ChannelUnion channel -> {
                    if (channel.getId().equals(id)) {
                        historySet.add(memberHistory);
                    }
                }
                case Role role -> {
                    if (role.getId().equals(id)) {
                        historySet.add(memberHistory);
                    }
                }
                case Renamed renamed -> {
                    if (Objects.equals(renamed.getId(), id)) {
                        historySet.add(memberHistory);
                    }
                }
                default -> {}
            }
        }

        return historySet;
    }

    public static boolean contains(String guildId, String memberId, Renamed renamed) {
        for (History roleUpdates : get(guildId, memberId, ActionType.ROLE_UPDATE)) {
            if (((Renamed) roleUpdates.getObject()).getId().contains(renamed.getId())) {
                return true;
            }
        }
        for (History chanelUpdates : get(guildId, memberId, ActionType.CHANNEL_UPDATE)) {
            if (((Renamed) chanelUpdates.getObject()).getId().contains(renamed.getId())) {
                return true;
            }
        }

        return false;
    }

    public static void put(String guildId, String memberId, History memberHistory) {
        if (!history.containsKey(guildId)) {
            history.put(guildId, new HashMap<>());
        }
        if (!history.get(guildId).containsKey(memberId)) {
            history.get(guildId).put(memberId, new HashSet<>());
        }
        get(guildId, memberId).add(memberHistory);
    }

    public static void remove(String guildId, String memberId, History memberHistory) {
        get(guildId, memberId).remove(memberHistory);
    }
}