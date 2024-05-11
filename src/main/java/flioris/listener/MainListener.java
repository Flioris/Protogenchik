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

package flioris.listener;

import flioris.Bot;
import flioris.db.Core;
import flioris.util.ConfigHandler;
import flioris.util.GuildSettings;
import flioris.util.ProtectedGuildsCache;
import flioris.util.message.BotMessage;
import flioris.util.message.BotMessageCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MainListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bot");
    private static final Core core = Bot.getCore();

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (!event.isFromGuild() || !(event.getChannel() instanceof TextChannel channel)) {
            return;
        }

        Guild guild = event.getGuild();
        GuildSettings guildSettings = ProtectedGuildsCache.get(guild.getId());
        Member selfMember = guild.getSelfMember();

        if (guildSettings == null || !channel.getId().equals(guildSettings.getBotChannelId()) ||
                !BotMessageCache.contains(event.getMessageId()) || !selfMember.hasPermission(channel,
                Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)) {
            return;
        }

        String whoDeleted = "unknown";
        BotMessage oldMessage = BotMessageCache.get(event.getMessageId());
        EmbedBuilder notification = ConfigHandler.getMessageEmbed(guildSettings.getLang() + ".embeds.botMessageDeleted");

        if (selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            AuditLogEntry log = guild.retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).limit(1).complete().getFirst();
            User user = log.getUser();
            if (user != null) {
                whoDeleted = user.getAsMention();
            }
        }

        channel.sendMessageEmbeds(oldMessage.getEmbed()).setContent(oldMessage.getText())
                .queue(m -> new BotMessage(m.getId(), m.getContentRaw(), m.getEmbeds().getFirst()).cache());
        channel.sendMessageEmbeds(notification
                        .setDescription(notification.build().getDescription()
                                .replace("{user}", whoDeleted))
                        .build())
                .queue(m -> new BotMessage(m.getId(), m.getContentRaw(), m.getEmbeds().getFirst()).cache());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Set<String> guildIds = new HashSet<>();
        List<Guild> guilds = Bot.getJda().getGuilds();
        long addedGuilds = guilds.stream()
                .peek(guild -> guildIds.add(guild.getId()))
                .filter(guild -> !core.guildsContain(guild.getId()))
                .peek(guild -> core.addGuild(guild.getId()))
                .count();
        long removedGuilds = core.getGuilds().stream()
                .filter(guildId -> !guildIds.contains(guildId))
                .peek(core::removeGuild)
                .count();

        LOGGER.info("I'm ready! Has {} guilds. Added {} guilds. Removed {} guilds.", guilds.size(), addedGuilds, removedGuilds);
        LOGGER.info("Type 'help' for help.");

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String[] args = scanner.nextLine().split(" ");
                switch (args[0].toLowerCase()) {
                    case "help" -> LOGGER.info("""
                            HELP:
                            - bladd [id/nameId] - add the name or ID of any user or bot to the blacklist. Users and bots from the
                            - blacklist will not be able to access servers with Protogenchik if AntiNuke is enabled there.
                            - blrem [id/nameId] - remove the name or ID of any user or bot from the blacklist.
                            - help - get help.
                            - reload - reload config.""");
                    case "bladd" -> {
                        if (args.length == 2) {
                            core.blacklistAdd(args[1]);
                            LOGGER.info("You blacklisted {}", args[1]);
                        } else {
                            LOGGER.info("Invalid command. Usage: bladd [id/nameId].");
                        }
                    }
                    case "blrem" -> {
                        if (args.length == 2) {
                            core.blacklistRemove(args[1]);
                            LOGGER.info("You unblacklisted {}", args[1]);
                        } else {
                            LOGGER.info("Invalid command. Usage: blrem [id/nameId].");
                        }
                    }
                    case "reload" -> {
                        ConfigHandler.load();
                        LOGGER.info("config reloaded.");
                    }
                    default -> LOGGER.info("Command not found. Type 'help' for help.");
                }
            }
        }, "CMD").start();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        String guildId = event.getGuild().getId();

        if (!core.guildsContain(guildId)) {
            core.addGuild(guildId);
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        String guildId = event.getGuild().getId();

        if (core.guildsContain(guildId)) {
            core.removeGuild(guildId);
        }
    }
}