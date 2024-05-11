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

package flioris.listener.nuke;

import flioris.Bot;
import flioris.util.*;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import flioris.util.reaction.Reaction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelEventListener extends ListenerAdapter {
    private static final JDA jda = Bot.getJda();

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        Guild guild = event.getGuild();

        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            return;
        }

        String guildId = guild.getId();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guildId);

        if (guildSettings == null) {
            return;
        }

        ChannelUnion channel = event.getChannel();
        String channelId = channel.getId();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) {
                if (!log.getTargetId().equals(channelId)) {
                    continue;
                }

                User user = log.getUser();

                if (user == null) {
                    return;
                }

                String userId = user.getId();

                if (userId.equals(jda.getSelfUser().getId()) || userId.equals(guild.getOwnerId()) ||
                        guildSettings.getWhitelist().contains(userId)) {
                    return;
                }

                ActionType action = log.getType();

                if (channelId.equals(guildSettings.getBotChannelId())) {
                    Reaction.to(guild, user, action);
                    Recovery.from(guild, userId, action);
                    return;
                }

                History history = new History(action, channel);

                for (History oldHistory : HistoryCache.get(guildId, userId, channelId)) {
                    HistoryCache.remove(guildId, userId, oldHistory);
                }
                HistoryCache.put(guildId, userId, history);
                DelayedExecution.start(() -> HistoryCache.remove(guildId, userId, history), guildSettings.getCooldown());
                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getChannelDeleteLimit()) {
                    return;
                }

                Reaction.to(guild, user, action);
                Recovery.from(guild, userId, action);

                return;
            }
        });
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        Guild guild = event.getGuild();

        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            return;
        }

        String guildId = guild.getId();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guildId);

        if (guildSettings == null) {
            return;
        }

        ChannelUnion channel = event.getChannel();
        String channelId = channel.getId();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) {
                if (!log.getTargetId().equals(channelId)) {
                    continue;
                }

                User user = log.getUser();

                if (user == null) {
                    return;
                }

                String userId = user.getId();

                if (userId.equals(jda.getSelfUser().getId()) || userId.equals(guild.getOwnerId()) ||
                        guildSettings.getWhitelist().contains(userId)) {
                    return;
                }

                ActionType action = log.getType();
                History history = new History(action, channel);

                HistoryCache.put(guildId, userId, history);
                DelayedExecution.start(() -> HistoryCache.remove(guildId, userId, history), guildSettings.getCooldown());
                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getChannelCreateLimit()) {
                    return;
                }

                Reaction.to(guild, user, action);
                Recovery.from(guild, userId, action);

                return;
            }
        });
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        Guild guild = event.getGuild();

        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            return;
        }

        String guildId = guild.getId();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guildId);

        if (guildSettings == null) {
            return;
        }

        String channelId = event.getChannel().getId();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) {
                if (!log.getTargetId().equals(channelId)) {
                    continue;
                }

                User user = log.getUser();

                if (user == null) {
                    return;
                }

                String userId = user.getId();

                if (userId.equals(jda.getSelfUser().getId()) || userId.equals(guild.getOwnerId()) ||
                        guildSettings.getWhitelist().contains(userId)) {
                    return;
                }

                ActionType action = log.getType();
                Renamed renamed = new Renamed(channelId, event.getOldValue());
                History history = new History(action, renamed);

                if (!HistoryCache.contains(guildId, userId, renamed)) {
                    HistoryCache.put(guildId, userId, history);
                }
                DelayedExecution.start(() -> HistoryCache.remove(guildId, userId, history), guildSettings.getCooldown());
                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getChannelRenameLimit()) {
                    return;
                }

                Reaction.to(guild, user, action);
                Recovery.from(guild, userId, action);

                return;
            }
        });
    }
}
