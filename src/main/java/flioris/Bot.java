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

package flioris;

import flioris.db.Core;
import flioris.listener.CommandListener;
import flioris.listener.MainListener;
import flioris.listener.nuke.ChannelEventListener;
import flioris.listener.nuke.MemberEventListener;
import flioris.listener.nuke.RoleEventListener;
import flioris.util.ConfigHandler;
import flioris.util.ProtectedGuildsCache;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    @Getter
    private static JDA jda;
    @Getter
    private static Core core;

    public static void main(String[] args) {
        core = new Core();
        ConfigHandler.load();
        jda = JDABuilder.createDefault(ConfigHandler.getString("bot.token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();
        jda.addEventListener(new ChannelEventListener(),
                new MemberEventListener(),
                new RoleEventListener(),
                new CommandListener(),
                new MainListener());
        initCommands();
        ProtectedGuildsCache.putAll(core.getProtectedGuilds());
    }

    private static void initCommands() {
        jda.updateCommands().addCommands(
                Commands.slash("limit", "Set an action limit.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOptions(new OptionData(OptionType.STRING, "action", "Choose an action.", true)
                                        .addChoice("Removing channels", "channel_delete_limit")
                                        .addChoice("Channel creation", "channel_create_limit")
                                        .addChoice("Renaming channels", "channel_rename_limit")
                                        .addChoice("Removing roles", "role_delete_limit")
                                        .addChoice("Creating roles", "role_create_limit")
                                        .addChoice("Renaming roles", "role_rename_limit")
                                        .addChoice("Removing members", "member_remove_limit")
                                        .addChoice("Renaming members", "member_rename_limit"),
                                new OptionData(OptionType.INTEGER, "limit", "Enter a limit.", true)
                                        .setMinValue(1)
                                        .setMaxValue(10)),
                Commands.slash("antinuke", "AntiNuke Actions.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addSubcommands(new SubcommandData("enable", "Enable AntiNuke.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Select a channel for the bot.", true),
                                                new OptionData(OptionType.STRING, "reaction", "Choose a reaction.", true)
                                                        .addChoice("None", "NONE")
                                                        .addChoice("Remove roles", "REMOVE_ROLES")
                                                        .addChoice("Kick", "KICK")
                                                        .addChoice("Ban", "BAN")),
                                new SubcommandData("disable", "Disable AntiNuke.")),
                Commands.slash("whitelist", "Whitelist Actions.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addSubcommands(new SubcommandData("add", "Add user to whitelist.")
                                        .addOptions(new OptionData(OptionType.USER, "user",
                                                "Select a user or enter their ID. The user may not be on the server.", true)),
                                new SubcommandData("remove", "remove user from whitelist.")
                                        .addOptions(new OptionData(OptionType.USER, "user",
                                                "Select a user or enter their ID. The user may not be on the server.", true))),
                Commands.slash("reaction", "Set reaction to nuke.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOptions(new OptionData(OptionType.STRING, "reaction", "Choose a reaction.", true)
                                .addChoice("None", "NONE")
                                .addChoice("Remove roles", "REMOVE_ROLES")
                                .addChoice("Kick", "KICK")
                                .addChoice("Ban", "BAN")),
                Commands.slash("lang", "Set language.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOptions(new OptionData(OptionType.STRING, "lang", "Choose language.", true)
                                .addChoice("en", "en")
                                .addChoice("ru", "ru")),
                Commands.slash("cooldown", "Set a cooldown.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOptions(new OptionData(OptionType.INTEGER, "seconds", "Enter seconds.", true)
                                .setMinValue(10)
                                .setMaxValue(600)),
                Commands.slash("channel", "Set bot channel.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Select a channel for the bot.", true)),
                Commands.slash("settings", "Display server settings.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        ).queue();
    }
}