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

package flioris.listener;

import flioris.Bot;
import flioris.util.Config;
import flioris.util.DominantColor;
import flioris.util.GuildSettings;
import flioris.util.message.BotMessage;
import flioris.util.message.BotMessageCache;
import flioris.util.reaction.ReactionType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Scanner;

public class MainListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bot");

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        User author = event.getUser();
        GuildSettings settings = Bot.getDb().getGuildSettings(guild.getIdLong());

        if (event.getName().equals("settings")) {
            TextChannel channel = guild.getChannelById(TextChannel.class, settings.getBotChannelId());
            EmbedBuilder embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.settings");
            MessageEmbed messageEmbed = embedBuilder.build();
            HashSet<String> mentions = new HashSet<>();
            for (Long id : settings.getWhitelist()) {
                User user = Bot.getJda().getUserById(id);
                mentions.add(user == null ? String.valueOf(id) : user.getAsMention());
            }

            event.replyEmbeds(embedBuilder
                    .setThumbnail(guild.getIconUrl())
                    .setColor(DominantColor.get(guild.getIconUrl()))
                    .setFooter(messageEmbed.getFooter().getText(), "https://cdn.discordapp.com/attachments/1123930994945310720/1151733865782718474/3.png")
                    .setTitle(messageEmbed.getTitle().replace("{server}", guild.getName()))
                    .setDescription(messageEmbed.getDescription()
                            .replace("{ae}", settings.getAntinukeEnabled() ? ":white_check_mark:" : ":negative_squared_cross_mark:")
                            .replace("{bc}", channel != null ? channel.getAsMention() : ":negative_squared_cross_mark:")
                            .replace("{lg}", settings.getLang())
                            .replace("{pm}", settings.getReactionType().toString())
                            .replace("{cd}", settings.getCooldown().toString())
                            .replace("{wl}", mentions.isEmpty() ? "`NONE`" : String.join(", ", mentions))
                            .replace("{cdl}", settings.getChannelDeleteLimit().toString())
                            .replace("{ccl}", settings.getChannelCreateLimit().toString())
                            .replace("{crl}", settings.getChannelRenameLimit().toString())
                            .replace("{rdl}", settings.getRoleDeleteLimit().toString())
                            .replace("{rcl}", settings.getRoleCreateLimit().toString())
                            .replace("{rrl}", settings.getRoleRenameLimit().toString())
                            .replace("{mrl}", settings.getMemberRemoveLimit().toString())
                            .replace("{mnl}", settings.getMemberRenameLimit().toString())
                    ).build()).queue();
            return;
        }

        if (!settings.getWhitelist().contains(author.getIdLong()) && author.getIdLong() != guild.getOwnerIdLong()) {
            event.replyEmbeds(Config.getMessageEmbed(settings.getLang()+".embeds.noRights").build()).queue();
            return;
        }

        switch (event.getName()) {
            case "antinuke" -> {
                if (event.getSubcommandName().equals("enable")) {
                    GuildChannelUnion channel = event.getOption("channel").getAsChannel();
                    if (!(channel instanceof TextChannel)) {
                        event.replyEmbeds(Config.getMessageEmbed(settings.getLang()+".embeds.nontextChannel")
                                .build()).queue();
                        return;
                    }
                    ReactionType reaction = ReactionType.valueOf(event.getOption("reaction").getAsString());
                    EmbedBuilder embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.antinukeEnabled.true");

                    Bot.getDb().updateGuild(guild.getIdLong(), "antinuke", "1");
                    Bot.getDb().updateGuild(guild.getIdLong(), "bot_channel_id", channel.getId());
                    Bot.getDb().updateGuild(guild.getIdLong(), "reaction_type", reaction.getValue());
                    event.replyEmbeds(embedBuilder
                            .setDescription(embedBuilder.build().getDescription()
                                    .replace("{channel}", channel.getAsMention())
                                    .replace("{punishment}", reaction.toString()))
                            .build()).queue();
                } else {
                    Bot.getDb().updateGuild(guild.getIdLong(), "antinuke", "0");
                    event.replyEmbeds(Config.getMessageEmbed(settings.getLang()+".embeds.antinukeEnabled.false")
                            .build()).queue();
                }
            }
            case "whitelist" -> {
                HashSet<Long> whitelist = settings.getWhitelist();
                User user = event.getOption("user").getAsUser();
                EmbedBuilder embedBuilder;
                if (event.getSubcommandName().equals("add")) {
                    whitelist.add(user.getIdLong());
                    embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.whitelistAdd");
                } else {
                    whitelist.remove(user.getIdLong());
                    embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.whitelistRemove");
                }

                Bot.getDb().updateGuild(guild.getIdLong(), "whitelist",
                        whitelist.toString().replace("[", "").replace("]", "").replace(",", ""));
                event.replyEmbeds(embedBuilder
                        .setDescription(embedBuilder.build().getDescription()
                                .replace("{admin}", author.getAsMention())
                                .replace("{user}", user.getAsMention()))
                        .build()).queue();
            }
            case "limit" -> {
                String action = event.getOption("action").getAsString();
                String actionName = action.replace("_limit", "").toUpperCase();
                Integer limit = event.getOption("limit").getAsInt();
                EmbedBuilder embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.limitSet");
                MessageEmbed messageEmbed = embedBuilder.build();

                Bot.getDb().updateGuild(guild.getIdLong(), action, limit);
                event.replyEmbeds(embedBuilder
                        .setTitle(messageEmbed.getTitle().replace("{action}", actionName))
                        .setDescription(messageEmbed.getDescription()
                                .replace("{action}", actionName)
                                .replace("{limit}", limit.toString()))
                        .build()).queue();
            }
            case "channel" -> {
                GuildChannelUnion channel = event.getOption("channel").getAsChannel();
                if (!(channel instanceof TextChannel)) {
                    event.replyEmbeds(Config.getMessageEmbed(settings.getLang()+".embeds.nontextChannel")
                            .build()).queue();
                    return;
                }
                EmbedBuilder embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.botChannelSet");

                Bot.getDb().updateGuild(guild.getIdLong(), "bot_channel_id", channel.getId());
                event.replyEmbeds(embedBuilder
                        .setDescription(embedBuilder.build().getDescription()
                                .replace("{channel}", channel.getAsMention()))
                        .build()).queue();
            }
            case "cooldown" -> {
                Integer seconds = event.getOption("seconds").getAsInt();
                EmbedBuilder embedBuilder = Config.getMessageEmbed(settings.getLang()+".embeds.cooldownSet");

                Bot.getDb().updateGuild(guild.getIdLong(), "cooldown", seconds);
                event.replyEmbeds(embedBuilder
                        .setDescription(embedBuilder.build().getDescription()
                                .replace("{seconds}", seconds.toString()))
                        .build()).queue();
            }
            case "reaction" -> {
                ReactionType reactionType = ReactionType.valueOf(event.getOption("reaction").getAsString());

                Bot.getDb().updateGuild(guild.getIdLong(), "reaction_type", reactionType.getValue());
                event.replyEmbeds(Config.getMessageEmbed(settings.getLang()+".embeds.reactionSet."+reactionType)
                        .build()).queue();
            }
            case "lang" -> {
                String lang = event.getOption("lang").getAsString();

                Bot.getDb().updateGuild(guild.getIdLong(), "lang", lang);
                event.replyEmbeds(Config.getMessageEmbed(lang+".embeds.langSet")
                        .build()).queue();
            }
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Guild guild = event.getGuild();
        GuildSettings guildSettings = Bot.getDb().getGuildSettings(guild.getIdLong());
        MessageChannelUnion channel = event.getChannel();

        if (!guildSettings.getAntinukeEnabled() || channel.getIdLong()!=guildSettings.getBotChannelId()
            || !BotMessageCache.contains(event.getMessageIdLong())) return;

        String whoDeleted = guild.retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).limit(1).complete()
                .get(0).getUser().getAsMention();
        BotMessage message = BotMessageCache.get(event.getMessageIdLong());
        channel.sendMessageEmbeds(message.getEmbed()).setContent(message.getText())
                .queue(m -> new BotMessage(m.getIdLong(), m.getContentRaw(), m.getEmbeds().get(0)).cache());
        EmbedBuilder embedBuilder = Config.getMessageEmbed(guildSettings.getLang()+".embeds.botMessageDeleted");

        channel.sendMessageEmbeds(embedBuilder
                        .setDescription(embedBuilder.build().getDescription()
                                .replace("{user}", whoDeleted))
                        .build())
                .queue(m -> new BotMessage(m.getIdLong(), m.getContentRaw(), m.getEmbeds().get(0)).cache());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        HashSet<Long> guildIds = new HashSet<>();
        long addedGuilds = Bot.getJda().getGuilds().stream()
                .peek(guild -> guildIds.add(guild.getIdLong()))
                .filter(guild -> !Bot.getDb().guildsContain(guild.getIdLong()))
                .peek(guild -> Bot.getDb().addGuild(guild.getIdLong()))
                .count();
        long removedGuilds = Bot.getDb().getGuilds().stream()
                .filter(guildId -> !guildIds.contains(guildId))
                .peek(Bot.getDb()::removeGuild)
                .count();

        LOGGER.info("I'm ready! Added " + addedGuilds + " guilds. Removed " + removedGuilds + " guilds.");
        LOGGER.info("Type 'help' for help.");

        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String[] args = scanner.nextLine().split(" ");

                switch (args[0].toLowerCase()) {
                    case "help" -> System.out.println("""
                            bladd [text] - add the name or ID of any user or bot to the blacklist. Users and bots from the
                            blacklist will not be able to access servers with Protogenchik if AntiNuke is enabled there.
                            blrem [text] - remove the name or ID of any user or bot from the blacklist.
                            help - get help.
                            reload - reload config.""");
                    case "bladd" -> {
                        if (args.length == 2) {
                            Bot.getDb().blacklistAdd(args[1]);
                            System.out.println("You blacklisted " + args[1]);
                        } else System.out.println("Invalid command. Usage: bladd [text].");
                    }
                    case "blrem" -> {
                        if (args.length == 2) {
                            Bot.getDb().blacklistRemove(args[1]);
                            System.out.println("You unblacklisted " + args[1]);
                        } else System.out.println("Invalid command. Usage: blrem [text].");
                    }
                    case "reload" -> {
                        Config.load();
                        System.out.println("config reloaded.");
                    }
                    default -> System.out.println("Command not found. Type 'help' for help.");
                }
            }
        });
        thread.start();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (!Bot.getDb().guildsContain(guildId)) Bot.getDb().addGuild(guildId);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (Bot.getDb().guildsContain(guildId)) Bot.getDb().removeGuild(guildId);
    }
}