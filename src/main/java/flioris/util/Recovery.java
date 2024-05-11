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

import flioris.Bot;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;

public class Recovery {
    private static final JDA jda = Bot.getJda();

    public static void from(Guild guild, String memberId, ActionType actionType) {
        Member selfMember = guild.getSelfMember();

        for (History history : HistoryCache.get(guild.getId(), memberId, actionType)) {
            switch (actionType) {
                case CHANNEL_DELETE -> {
                    switch (history.getObject()) {
                        case StandardGuildChannel channel -> {
                            if (selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL)) {
                                channel.createCopy().queue();
                            }
                        }
                        case Category channel -> {
                            if (selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL)) {
                                channel.createCopy().queue();
                            }
                        }
                        default -> System.out.println("Unknown channel type: " + history.getObject().getClass());
                    }
                }
                case CHANNEL_CREATE -> {
                    GuildChannel channel = (GuildChannel) history.getObject();
                    if (selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL)) {
                        channel.delete().queue();
                    }
                }
                case CHANNEL_UPDATE -> {
                    Renamed r = (Renamed) history.getObject();
                    GuildChannel channel = jda.getChannelById(GuildChannel.class, r.getId());
                    if (channel != null && selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL,
                            Permission.VIEW_CHANNEL)) {
                        channel.getManager().setName(r.getOldName()).queue();
                    }
                }
                case ROLE_DELETE -> {
                    Role role = (Role) history.getObject();
                    if (selfMember.canInteract(role)) {
                        role.createCopy().queue();
                    }
                }
                case ROLE_CREATE -> {
                    Role role = (Role) history.getObject();
                    if (selfMember.canInteract(role)) {
                        role.delete().queue();
                    }
                }
                case ROLE_UPDATE -> {
                    Renamed r = (Renamed) history.getObject();
                    Role role = jda.getRoleById(r.getId());
                    if (role != null && selfMember.canInteract(role)) {
                        role.getManager().setName(r.getOldName()).queue();
                    }
                }
            }
            HistoryCache.remove(guild.getId(), memberId, history);
        }
    }
}
