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

package flioris.listener.nuke;

import flioris.Bot;
import flioris.util.DelayedExecution;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import flioris.util.GuildSettings;
import flioris.util.Recovery;
import flioris.util.Renamed;
import flioris.util.reaction.Reaction;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashSet;

public class ChannelEventListener extends ListenerAdapter {

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        ChannelUnion channel = event.getChannel();
        long channelId = channel.getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == channelId) {
                User user = log.getUser();
                long userId = user.getIdLong();
                ActionType action = log.getType();

                if (userId == Bot.getJda().getSelfUser().getIdLong() || userId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;
                if (channelId == guildSettings.getBotChannelId()) {
                    Reaction.to(guild, user, action);
                    Recovery.from(guildId, userId, action);
                    return;
                }

                History history = new History(userId, action, channel);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                for (History h : HistoryCache.get(guildId, userId, channelId)) HistoryCache.rem(guildId, h);
                HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getChannelDeleteLimit()) return;
                Reaction.to(guild, user, action);
                Recovery.from(guildId, userId, action);

                return;
            }
        });
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        ChannelUnion channel = event.getChannel();
        long channelId = channel.getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == channelId) {
                User user = log.getUser();
                long userId = user.getIdLong();
                ActionType action = log.getType();

                if (userId == Bot.getJda().getSelfUser().getIdLong() || userId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                History history = new History(userId, action, channel);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getChannelCreateLimit()) return;
                Reaction.to(guild, user, action);
                Recovery.from(guildId, userId, action);

                return;
            }
        });
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        long channelId = event.getChannel().getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == channelId) {
                User user = log.getUser();
                long userId = user.getIdLong();
                ActionType action = log.getType();

                if (userId == Bot.getJda().getSelfUser().getIdLong() || userId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                Renamed renamed = new Renamed(channelId, event.getOldValue());
                History history = new History(userId, action, renamed);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                if (!HistoryCache.contains(guildId, userId, renamed)) HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getChannelRenameLimit()) return;
                Reaction.to(guild, user, action);
                Recovery.from(guildId, userId, action);

                return;
            }
        });
    }
}
