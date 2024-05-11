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

package flioris.util.reaction;

import flioris.Bot;
import flioris.util.ConfigHandler;
import flioris.util.GuildSettings;
import flioris.util.ProtectedGuildsCache;
import flioris.util.message.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class Reaction {

    public static void to(Guild guild, User user, @Nullable ActionType actionType) {
        GuildSettings guildSettings = ProtectedGuildsCache.get(guild.getId());
        String lang = guildSettings.getLang();

        punish(guild, user, guildSettings.getReactionType());

        if (user.isBot() && guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            guild.retrieveAuditLogs().type(ActionType.BOT_ADD).queue(logs -> {
                for (AuditLogEntry log : logs) {
                    if (!log.getTargetId().equals(user.getId())) {
                        continue;
                    }

                    User adder = log.getUser();

                    if (adder == null) {
                        return;
                    }

                    EmbedBuilder warn = ConfigHandler.getMessageEmbed(lang + ".embeds.addedNukeBot");

                    if (!guild.getOwnerId().equals(adder.getId())) {
                        punish(guild, adder, guildSettings.getReactionType());
                    }
                    sendWarn(guild, guildSettings, warn
                            .setDescription(warn.build().getDescription()
                                    .replace("{bot}", user.getAsMention())
                                    .replace("{user}", adder.getAsMention()))
                            .build());

                    return;
                }
            });
        }

        if (actionType == null) {
            return;
        }

        EmbedBuilder warn = ConfigHandler.getMessageEmbed(lang + ".embeds.reaction");

        sendWarn(guild, guildSettings, warn
                .setDescription(warn.build().getDescription()
                        .replace("{action}", actionType.name())
                        .replace("{user}", user.getAsMention()))
                .build());
    }

    private static void punish(Guild guild, User user, ReactionType reaction) {
        Member selfMember = guild.getSelfMember();

        guild.retrieveMember(user).queue(member -> {
            switch (reaction) {
                case KICK -> {
                    if (selfMember.canInteract(member)) {
                        member.kick().queue();
                    }
                }
                case BAN -> {
                    if (selfMember.canInteract(member)) {
                        member.ban(10, TimeUnit.MINUTES).queue();
                    }
                }
                case REMOVE_ROLES -> member.getRoles().forEach(role -> {
                    if (selfMember.canInteract(role)) {
                        guild.removeRoleFromMember(member, role).queue();
                    }
                });
            }
        }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
    }

    private static void sendWarn(Guild guild, GuildSettings guildSettings, MessageEmbed warn) {
        TextChannel channel = guild.getChannelById(TextChannel.class, guildSettings.getBotChannelId());
        Member selfMember = guild.getSelfMember();

        if (channel == null) {
            if (!selfMember.hasPermission(Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                    Permission.MESSAGE_EMBED_LINKS)) {
                return;
            }
            guild.createTextChannel("antinuke").queue(restoredChannel -> {
                EmbedBuilder notification = ConfigHandler.getMessageEmbed(guildSettings.getLang() + ".embeds.botChannelDeleted");
                String restoredChannelId = restoredChannel.getId();
                String guildId = guild.getId();
                Bot.getCore().updateGuild(guildId, "bot_channel_id", restoredChannelId);
                ProtectedGuildsCache.get(guildId).setBotChannelId(restoredChannelId);
                restoredChannel.sendMessageEmbeds(notification
                                .setDescription(notification.build().getDescription()
                                        .replace("{user}", "unknown"))
                                .build())
                        .queue(m -> new BotMessage(m.getId(), m.getContentRaw(), m.getEmbeds().getFirst()).cache());
                restoredChannel.sendMessageEmbeds(warn)
                        .queue(m -> new BotMessage(m.getId(), m.getContentRaw(), m.getEmbeds().getFirst()).cache());
            });
        } else {
            if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                    Permission.MESSAGE_EMBED_LINKS)) {
                return;
            }
            channel.sendMessageEmbeds(warn).queue(m -> new BotMessage(m.getId(), m.getContentRaw(), m.getEmbeds().getFirst()).cache());
        }
    }
}
