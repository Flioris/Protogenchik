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
import flioris.util.Recovery;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import flioris.util.GuildSettings;
import flioris.util.Renamed;
import flioris.util.reaction.Reaction;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashSet;

public class RoleEventListener extends ListenerAdapter {

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        Role role = event.getRole();
        long roleId = role.getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == roleId) {
                User user = log.getUser();
                long userId = user.getIdLong();
                ActionType action = log.getType();

                if (userId == Bot.getJda().getSelfUser().getIdLong() || userId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                History history = new History(userId, action, role);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                for (History h : HistoryCache.get(guildId, userId, roleId)) HistoryCache.rem(guildId, h);
                HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getRoleDeleteLimit()) return;
                Reaction.to(guild, user, action);
                Recovery.from(guildId, userId, action);

                return;
            }
        });
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        Role role = event.getRole();
        long roleId = role.getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == roleId) {
                User user = log.getUser();
                long userId = user.getIdLong();
                ActionType action = log.getType();

                if (userId == Bot.getJda().getSelfUser().getIdLong() || userId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                History history = new History(userId, action, role);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getRoleCreateLimit()) return;
                Reaction.to(guild, user, action);
                Recovery.from(guildId, userId, action);

                return;
            }
        });
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        long roleId = event.getRole().getIdLong();

        event.getGuild().retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == roleId) {
                User user = log.getUser();
                long userId = user.getIdLong();
                ActionType action = log.getType();

                if (userId == Bot.getJda().getSelfUser().getIdLong() || userId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                Renamed renamed = new Renamed(roleId, event.getOldValue());
                History history = new History(userId, action, renamed);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                if (!HistoryCache.contains(guildId, userId, renamed)) HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                if (HistoryCache.get(guildId, userId, action).size() <= guildSettings.getRoleRenameLimit()) return;
                Reaction.to(guild, user, action);
                Recovery.from(guildId, userId, action);

                return;
            }
        });
    }
}