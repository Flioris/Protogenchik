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
import flioris.db.Core;
import flioris.util.DelayedExecution;
import flioris.util.GuildSettings;
import flioris.util.ProtectedGuildsCache;
import flioris.util.Renamed;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import flioris.util.reaction.Reaction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Set;

public class MemberEventListener extends ListenerAdapter {
    private static final JDA jda = Bot.getJda();
    private static final Core core = Bot.getCore();

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();

        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            return;
        }

        String guildId = guild.getId();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guildId);

        if (guildSettings == null) {
            return;
        }

        User user = event.getUser();
        String userId = user.getId();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) {
                if (!log.getTargetId().equals(userId)) {
                    continue;
                }

                User remover = log.getUser();

                if (remover == null) {
                    return;
                }

                String removerId = remover.getId();

                if (removerId.equals(jda.getSelfUser().getId()) || removerId.equals(guild.getOwnerId()) ||
                        guildSettings.getWhitelist().contains(removerId)) {
                    return;
                }


                ActionType action = log.getType();
                History history = new History(action, user);

                HistoryCache.put(guildId, removerId, history);
                DelayedExecution.start(() -> HistoryCache.remove(guildId, removerId, history), guildSettings.getCooldown());

                Set<History> historySet = HistoryCache.get(guildId, removerId, action);

                if (historySet.size() <= guildSettings.getMemberRemoveLimit()) {
                    return;
                }

                Reaction.to(guild, remover, action);
                for (History h : historySet) {
                    HistoryCache.remove(guildId, removerId, h);
                }

                return;
            }
        });
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();
        Guild guild = event.getGuild();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guild.getId());

        if (guildSettings == null) {
            return;
        }

        if (core.blacklistContains(user.getId()) || core.blacklistContains(user.getName()) &&
                !user.getFlags().contains(User.UserFlag.VERIFIED_BOT)) {
            Reaction.to(guild, user, null);
        }

    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        Guild guild = event.getGuild();

        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            return;
        }

        String guildId = guild.getId();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guildId);

        if (guildSettings == null) {
            return;
        }

        String userId = event.getUser().getId();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) {
                if (!log.getTargetId().equals(userId)) {
                    continue;
                }

                User renamer = log.getUser();

                if (renamer == null) {
                    return;
                }

                String renamerId = renamer.getId();

                if (renamerId.equals(jda.getSelfUser().getId()) || renamerId.equals(guild.getOwnerId()) ||
                        guildSettings.getWhitelist().contains(renamerId)) {
                    return;
                }

                ActionType action = log.getType();
                Renamed renamed = new Renamed(userId, event.getOldNickname());
                History history = new History(action, renamed);

                if (!HistoryCache.contains(guildId, renamerId, renamed)) {
                    HistoryCache.put(guildId, renamerId, history);
                }
                DelayedExecution.start(() -> HistoryCache.remove(guildId, renamerId, history), guildSettings.getCooldown());

                Set<History> historySet = HistoryCache.get(guildId, renamerId, action);

                if (historySet.size() <= guildSettings.getMemberRenameLimit()) {
                    return;
                }

                Reaction.to(guild, renamer, action);
                for (History oldHistory : historySet) {
                    HistoryCache.remove(guildId, renamerId, oldHistory);
                }

                return;
            }
        });
    }
}