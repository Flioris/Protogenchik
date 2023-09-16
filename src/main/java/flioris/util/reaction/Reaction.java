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

package flioris.util.reaction;

import flioris.Bot;
import flioris.util.Config;
import flioris.util.GuildSettings;
import flioris.util.message.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class Reaction {
    
    public static void to(Guild guild, User user, @Nullable ActionType actionType) {
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guild.getIdLong());

        punish(guild, user, guildSettings.getReactionType());

        if (user.isBot()) guild.retrieveAuditLogs().type(ActionType.BOT_ADD).queue(logs -> {
            for (AuditLogEntry log : logs) if (log.getTargetId().equals(user.getId())) {
                User adder = log.getUser();
                if (guild.getOwnerIdLong() != user.getIdLong()) punish(guild, adder, guildSettings.getReactionType());

                EmbedBuilder embedBuilder = Config.getMessageEmbed(guildSettings.getLang()+".embeds.addedNukeBot");
                sendWarn(guild, guildSettings, embedBuilder
                        .setDescription(embedBuilder.build().getDescription()
                                .replace("{bot}", user.getAsMention())
                                .replace("{user}", adder.getAsMention()))
                        .build());
                break;
            }
        });

        if (actionType == null) return;

        EmbedBuilder embedBuilder = Config.getMessageEmbed(guildSettings.getLang()+".embeds.reaction");
        sendWarn(guild, guildSettings, embedBuilder
                .setDescription(embedBuilder.build().getDescription()
                        .replace("{action}", actionType.name())
                        .replace("{user}", user.getAsMention()))
                .build());
    }

    private static void punish(Guild guild, User user, ReactionType reaction) {
        guild.retrieveMember(user).queue(member -> {
            switch (reaction) {
                case KICK -> member.kick().queue(unused -> {}, error -> {});
                case BAN -> member.ban(0, TimeUnit.SECONDS).queue(unused -> {}, error -> {});
                case REMOVE_ROLES -> member.getRoles().forEach(role -> {
                    if (!role.getTags().isBot()) guild.removeRoleFromMember(member, role).queue(unused -> {}, error -> {});
                });
            }
        }, error -> {});
    }

    private static void sendWarn(Guild guild, GuildSettings guildSettings, MessageEmbed embed) {
        TextChannel textChannel = Bot.getJda().getChannelById(TextChannel.class, guildSettings.getBotChannelId());
        if (textChannel == null) {
            guild.createTextChannel("antinuke").queue(channel -> {
                Bot.getDb().updateGuild(guild.getIdLong(), "bot_channel_id", channel.getId());
                EmbedBuilder embedBuilder = Config.getMessageEmbed(guildSettings.getLang()+".embeds.botChannelDeleted");
                channel.sendMessageEmbeds(embedBuilder
                                .setDescription(embedBuilder.build().getDescription()
                                        .replace("{user}", "unknown"))
                                .build())
                        .queue(m -> new BotMessage(m.getIdLong(), m.getContentRaw(), m.getEmbeds().get(0)).cache());
                channel.sendMessageEmbeds(embed)
                        .queue(m -> new BotMessage(m.getIdLong(), m.getContentRaw(), m.getEmbeds().get(0)).cache());
            });
        } else textChannel.sendMessageEmbeds(embed).queue(m ->
                new BotMessage(m.getIdLong(), m.getContentRaw(), m.getEmbeds().get(0)).cache());
    }
}
