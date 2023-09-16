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
import flioris.util.GuildSettings;
import flioris.util.Renamed;
import flioris.util.history.History;
import flioris.util.history.HistoryCache;
import flioris.util.reaction.Reaction;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashSet;

public class MemberEventListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        User user = event.getUser();
        long userId = user.getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == userId) {
                User remover = log.getUser();
                long removerId = remover.getIdLong();
                ActionType action = log.getType();

                if (removerId == Bot.getJda().getSelfUser().getIdLong() || removerId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                History history = new History(removerId, action, user);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                HashSet<History> historySet = HistoryCache.get(guildId, removerId, action);
                if (historySet.size() <= guildSettings.getMemberRemoveLimit()) return;
                Reaction.to(guild, remover, action);
                for (History h : historySet) HistoryCache.rem(guildId, h);

                return;
            }
        });
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();
        Guild guild = event.getGuild();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guild.getIdLong());
        if (!guildSettings.getAntinukeEnabled()) return;

        if (Bot.getDb().blacklistContains(user.getId()) || Bot.getDb().blacklistContains(user.getName()) &&
                !user.getFlags().contains(User.UserFlag.VERIFIED_BOT)) {
            Reaction.to(guild, user, null);
        }

    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guildId);
        if (!guildSettings.getAntinukeEnabled()) return;
        long userId = event.getUser().getIdLong();

        guild.retrieveAuditLogs().queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetIdLong() == userId) {
                User renamer = log.getUser();
                long renamerId = renamer.getIdLong();
                ActionType action = log.getType();

                if (renamerId == Bot.getJda().getSelfUser().getIdLong() || renamerId == guild.getOwnerIdLong() ||
                        guildSettings.getWhitelist().contains(userId)) return;

                Renamed renamed = new Renamed(userId, event.getOldNickname());
                History history = new History(renamerId, action, renamed);
                if (!HistoryCache.contains(guildId)) HistoryCache.add(guildId, new HashSet<>());
                if (!HistoryCache.contains(guildId, renamerId, renamed)) HistoryCache.add(guildId, history);
                DelayedExecution.start(() -> HistoryCache.rem(guildId, history), guildSettings.getCooldown());

                HashSet<History> historySet = HistoryCache.get(guildId, renamerId, action);
                if (historySet.size() <= guildSettings.getMemberRenameLimit()) return;
                Reaction.to(guild, renamer, action);
                for (History h : historySet) HistoryCache.rem(guildId, h);

                return;
            }
        });
    }
}