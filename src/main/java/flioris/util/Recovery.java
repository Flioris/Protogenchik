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

package flioris.util;

import flioris.Bot;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;

public class Recovery {
    
    public static void from(long guildId, long memberId, ActionType actionType) {
        for (History history : HistoryCache.get(guildId, memberId, actionType)) {
            switch (actionType) {
                case CHANNEL_DELETE -> {
                    switch (history.getObject()) {
                        case StandardGuildChannel channel -> channel.createCopy().queue();
                        case Category channel -> channel.createCopy().queue();
                        default -> System.out.println("Unknown channel type: "+history.getObject().getClass());
                    }
                }
                case CHANNEL_CREATE -> ((GuildChannel) history.getObject()).delete().queue();
                case CHANNEL_UPDATE ->
                        ((GuildChannel) history.getObject()).getManager()
                                .setName(((Renamed) history.getObject()).getOldName()).queue();
                case ROLE_DELETE -> ((Role) history.getObject()).createCopy().queue();
                case ROLE_CREATE -> ((Role) history.getObject()).delete().queue();
                case ROLE_UPDATE -> {
                    Renamed r = (Renamed) history.getObject();
                    Bot.getJda().getRoleById(r.getId()).getManager().setName(r.getOldName()).queue();
                }
            }
            HistoryCache.rem(guildId, history);
        }
    }
}
