package flioris.listener;

import flioris.Bot;
import flioris.db.Core;
import flioris.util.ConfigHandler;
import flioris.util.DominantColor;
import flioris.util.GuildSettings;
import flioris.util.ProtectedGuildsCache;
import flioris.util.reaction.ReactionType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {
    private static final Core core = Bot.getCore();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        Guild guild = event.getGuild();
        GuildSettings settings = core.getGuildSettings(guild.getId());

        switch (event.getName()) {
            case "settings" -> processSettingsCommand(event, guild, settings);
            case "antinuke" -> processAntinukeCommand(event, guild, settings);
            case "whitelist" -> processWhitelistCommand(event, guild, settings);
            case "limit" -> processLimitCommand(event, guild, settings);
            case "channel" -> processChannelCommand(event, guild, settings);
            case "cooldown" -> processCooldownCommand(event, guild, settings);
            case "reaction" -> processReactionCommand(event, guild, settings);
            case "lang" -> processLangCommand(event, guild, settings);
        }
    }

    private static boolean hasPermissions(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        String userId = event.getUser().getId();

        if (settings.getWhitelist().contains(userId) || userId.equals(guild.getOwnerId())) {
            return true;
        }

        event.replyEmbeds(ConfigHandler.getMessageEmbed(settings.getLang() + ".embeds.userWithoutRights").build()).queue();
        return false;
    }

    private static void processLangCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        String guildId = guild.getId();
        String lang = event.getOption("lang").getAsString();

        core.updateGuild(guildId, "lang", lang);
        if (ProtectedGuildsCache.contains(guildId)) {
            ProtectedGuildsCache.get(guildId).setLang(lang);
        }
        event.replyEmbeds(ConfigHandler.getMessageEmbed(lang + ".embeds.langSet").build()).queue();
    }

    private static void processReactionCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        ReactionType reactionType = ReactionType.valueOf(event.getOption("reaction").getAsString());
        Permission permission = switch (reactionType) {
            case NONE -> null;
            case KICK -> Permission.KICK_MEMBERS;
            case BAN -> Permission.BAN_MEMBERS;
            case REMOVE_ROLES -> Permission.MANAGE_ROLES;
        };

        if (!guild.getSelfMember().hasPermission(permission)) {
            EmbedBuilder answer = ConfigHandler.getMessageEmbed(settings.getLang() + ".embeds.botWithoutRights");
            event.replyEmbeds(answer
                    .setDescription(answer.build().getDescription()
                            .replace("{permissions}", permission.getName()))
                    .build()).queue();
            return;
        }

        String guildId = guild.getId();

        core.updateGuild(guildId, "reaction_type", reactionType.getValue());
        if (ProtectedGuildsCache.contains(guildId)) {
            ProtectedGuildsCache.get(guildId).setReactionType(reactionType);
        }
        event.replyEmbeds(ConfigHandler.getMessageEmbed(settings.getLang() + ".embeds.reactionSet." + reactionType).build()).queue();
    }

    private static void processCooldownCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        String guildId = guild.getId();
        int seconds = event.getOption("seconds").getAsInt();
        EmbedBuilder answer = ConfigHandler.getMessageEmbed(settings.getLang() + ".embeds.cooldownSet");

        core.updateGuild(guildId, "cooldown", seconds);
        if (ProtectedGuildsCache.contains(guildId)) {
            ProtectedGuildsCache.get(guildId).setCooldown((short) seconds);
        }
        event.replyEmbeds(answer
                .setDescription(answer.build().getDescription()
                        .replace("{seconds}", String.valueOf(seconds)))
                .build()).queue();
    }

    private static void processChannelCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        String lang = settings.getLang();
        GuildChannelUnion channel = event.getOption("channel").getAsChannel();

        if (!(channel instanceof TextChannel)) {
            event.replyEmbeds(ConfigHandler.getMessageEmbed(lang + ".embeds.nontextChannel").build()).queue();
            return;
        }

        List<Permission> permissions = List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                Permission.MESSAGE_EMBED_LINKS);

        if (!guild.getSelfMember().hasPermission(channel, permissions)) {
            EmbedBuilder answer = ConfigHandler.getMessageEmbed(lang + ".embeds.botWithoutRights");
            event.replyEmbeds(answer
                    .setDescription(answer.build().getDescription()
                            .replace("{permissions}", permissions.stream().map(Permission::getName)
                                    .collect(Collectors.joining(", "))))
                    .build()).queue();
            return;
        }

        String guildId = guild.getId();
        String channelId = channel.getId();
        EmbedBuilder answer = ConfigHandler.getMessageEmbed(lang + ".embeds.botChannelSet");

        core.updateGuild(guildId, "bot_channel_id", channelId);
        if (ProtectedGuildsCache.contains(guildId)) {
            ProtectedGuildsCache.get(guildId).setBotChannelId(channelId);
        }
        event.replyEmbeds(answer
                .setDescription(answer.build().getDescription()
                        .replace("{channel}", channel.getAsMention()))
                .build()).queue();
    }

    private static void processLimitCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        String guildId = guild.getId();
        String action = event.getOption("action").getAsString();
        String actionName = action.replace("_limit", "").toUpperCase();
        byte limit = (byte) event.getOption("limit").getAsInt();
        EmbedBuilder answer = ConfigHandler.getMessageEmbed(settings.getLang() + ".embeds.limitSet");
        MessageEmbed temp = answer.build();

        core.updateGuild(guildId, action, limit);
        if (ProtectedGuildsCache.contains(guildId)) {
            GuildSettings s = ProtectedGuildsCache.get(guildId);
            switch (action) {
                case "channel_delete_limit" -> s.setChannelDeleteLimit(limit);
                case "channel_create_limit" -> s.setChannelCreateLimit(limit);
                case "channel_rename_limit" -> s.setChannelRenameLimit(limit);
                case "role_delete_limit" -> s.setRoleDeleteLimit(limit);
                case "role_create_limit" -> s.setRoleCreateLimit(limit);
                case "role_rename_limit" -> s.setRoleRenameLimit(limit);
                case "member_remove_limit" -> s.setMemberRemoveLimit(limit);
                case "member_rename_limit" -> s.setMemberRenameLimit(limit);
            }
        }
        event.replyEmbeds(answer
                .setTitle(temp.getTitle().replace("{action}", actionName))
                .setDescription(temp.getDescription()
                        .replace("{action}", actionName)
                        .replace("{limit}", String.valueOf(limit)))
                .build()).queue();
    }

    private static void processWhitelistCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        String lang = settings.getLang();
        Set<String> whitelist = settings.getWhitelist();
        User user = event.getOption("user").getAsUser();
        String userId = user.getId();
        EmbedBuilder answer;

        if (event.getSubcommandName().equals("add")) {
            if (whitelist.size() >= 10) {
                event.replyEmbeds(ConfigHandler.getMessageEmbed(lang + ".embeds.whitelistFull").build()).queue();
                return;
            }
            whitelist.add(userId);
            answer = ConfigHandler.getMessageEmbed(lang + ".embeds.whitelistAdd");
        } else {
            whitelist.remove(userId);
            answer = ConfigHandler.getMessageEmbed(lang + ".embeds.whitelistRemove");
        }

        String guildId = guild.getId();

        if (ProtectedGuildsCache.contains(guildId)) {
            ProtectedGuildsCache.get(guildId).setWhitelist(whitelist);
        }
        core.updateGuild(guildId, "whitelist", whitelist.isEmpty() ? null : String.join(" ", whitelist));
        event.replyEmbeds(answer
                .setDescription(answer.build().getDescription()
                        .replace("{admin}", event.getUser().getAsMention())
                        .replace("{user}", user.getAsMention()))
                .build()).queue();
    }

    private static void processAntinukeCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        if (!hasPermissions(event, guild, settings)) {
            return;
        }

        String guildId = guild.getId();
        String lang = settings.getLang();

        if (event.getSubcommandName().equals("enable")) {
            GuildChannelUnion channel = event.getOption("channel").getAsChannel();

            if (!(channel instanceof TextChannel)) {
                event.replyEmbeds(ConfigHandler.getMessageEmbed(lang + ".embeds.nontextChannel").build()).queue();
                return;
            }

            ReactionType reaction = ReactionType.valueOf(event.getOption("reaction").getAsString());
            List<Permission> permissions = new ArrayList<>(List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                    Permission.MESSAGE_EMBED_LINKS));

            switch (reaction) {
                case KICK -> permissions.add(Permission.KICK_MEMBERS);
                case BAN -> permissions.add(Permission.BAN_MEMBERS);
                case REMOVE_ROLES -> permissions.add(Permission.MANAGE_ROLES);
            }

            if (!guild.getSelfMember().hasPermission(permissions)) {
                EmbedBuilder answer = ConfigHandler.getMessageEmbed(lang + ".embeds.botWithoutRights");
                event.replyEmbeds(answer
                        .setDescription(answer.build().getDescription()
                                .replace("{permissions}", permissions.stream().map(Permission::getName)
                                        .collect(Collectors.joining(", "))))
                        .build()).queue();
                return;
            }

            EmbedBuilder embedBuilder = ConfigHandler.getMessageEmbed(lang + ".embeds.antinukeEnabled.true");
            String channelId = channel.getId();

            core.updateGuild(guildId, "antinuke", true);
            core.updateGuild(guildId, "bot_channel_id", channelId);
            core.updateGuild(guildId, "reaction_type", reaction.getValue());
            settings.setAntinukeEnabled(true);
            settings.setBotChannelId(channelId);
            settings.setReactionType(reaction);
            ProtectedGuildsCache.put(guildId, settings);
            event.replyEmbeds(embedBuilder
                    .setDescription(embedBuilder.build().getDescription()
                            .replace("{channel}", channel.getAsMention())
                            .replace("{punishment}", reaction.toString()))
                    .build()).queue();
        } else {
            core.updateGuild(guildId, "antinuke", false);
            ProtectedGuildsCache.remove(guildId);
            event.replyEmbeds(ConfigHandler.getMessageEmbed(lang + ".embeds.antinukeEnabled.false").build()).queue();
        }
    }

    private static void processSettingsCommand(SlashCommandInteractionEvent event, Guild guild, GuildSettings settings) {
        EmbedBuilder answer = ConfigHandler.getMessageEmbed(settings.getLang() + ".embeds.settings");
        MessageEmbed temp = answer.build();
        Set<String> whitelist = settings.getWhitelist();
        TextChannel channel = null;

        if (settings.getBotChannelId() != null) {
            channel = guild.getChannelById(TextChannel.class, settings.getBotChannelId());
        }

        event.replyEmbeds(answer
                .setThumbnail(guild.getIconUrl())
                .setColor(DominantColor.get(guild.getIconUrl()))
                .setFooter(temp.getFooter().getText(), "https://cdn.discordapp.com/attachments/1123930994945310720/1151733865782718474/3.png")
                .setTitle(temp.getTitle().replace("{server}", guild.getName()))
                .setDescription(temp.getDescription()
                        .replace("{ae}", settings.isAntinukeEnabled() ? ":white_check_mark:" : ":negative_squared_cross_mark:")
                        .replace("{bc}", channel != null ? channel.getAsMention() : "`NONE`")
                        .replace("{lg}", settings.getLang())
                        .replace("{pm}", settings.getReactionType().toString())
                        .replace("{cd}", String.valueOf(settings.getCooldown()))
                        .replace("{wl}", whitelist.isEmpty() ? "`NONE`" : String.join(", ", whitelist))
                        .replace("{cdl}", String.valueOf(settings.getChannelDeleteLimit()))
                        .replace("{ccl}", String.valueOf(settings.getChannelCreateLimit()))
                        .replace("{crl}", String.valueOf(settings.getChannelRenameLimit()))
                        .replace("{rdl}", String.valueOf(settings.getRoleDeleteLimit()))
                        .replace("{rcl}", String.valueOf(settings.getRoleCreateLimit()))
                        .replace("{rrl}", String.valueOf(settings.getRoleRenameLimit()))
                        .replace("{mrl}", String.valueOf(settings.getMemberRemoveLimit()))
                        .replace("{mnl}", String.valueOf(settings.getMemberRenameLimit()))
                ).build()).queue();
    }
}
