[![Release](https://img.shields.io/github/v/release/Flioris/Protogenchik?label=Release)](https://github.com/Flioris/Protogenchik/releases)
[![discord-shield](https://discord.com/api/guilds/1045660297236582462/widget.png)](https://discord.gg/AZSZ8nhtra)

# Protogenchik
> This is my first serious and large-scale project that I am putting into open access, so I ask you to look at my code
> and, if you find any "oddities", write to me in discord. I also want to say that my English is bad, so you can also
> write translation edits to me in discord. My discord: flioris. If you have any questions or need help, please visit
> the [discord support server](https://discord.gg/AZSZ8nhtra).

## Description
**Anti-nuke bot for setting limits on various actions and protecting servers.** It also has a blacklist to which you can
add nuke-bots and nukers. Server settings and the blacklist are stored in the SQLite database. You can configure bot
messages in the JSON config. You can use this open source bot if you don't trust other bots or as a basis for your own
bot, but don't forget that the bot is GNU GPL licensed.

## How to use
0. *Install Java 21+ if you don't have it.*
1. Create a folder for the bot.
2. Download [Protogenchik.jar](https://github.com/Flioris/Protogenchik/releases) or generate it yourself and place it in
   the created folder.
3. In the folder, create a file "start.bat".
4. Open "start.bat" with a text editor and paste:
```bat
@ECHO OFF
java -jar Protogenchik.jar
PAUSE
```
5. Run "start.bat" and wait until the config is generated and an error appears.
6. Open "config.json" and paste the token of your own bot and save.
7. Run "start.bat".
