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

package flioris.util;

import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

public class Config {
    private static JSONObject config;
    private static final File FILE = new File("config.json");

    public static void load() {
        try {
            if (!FILE.exists()) Files.copy(Config.class.getClassLoader().getResourceAsStream("config.json"), FILE.toPath());
            config = new JSONObject(new JSONTokener(new FileReader(FILE)));
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    public static Object get(String path) {
        Object json = config;

        for (String key : path.split("\\.")) {
            try {
                json = ((JSONObject) json).get(key);
            } catch (Exception e) {return null;}
        }
        return json;
    }

    public static String getString(String path) {
        return get(path) instanceof String str ? str : null;
    }

    public static Integer getInteger(String path) {
        return get(path) instanceof Integer integer ? integer : null;
    }

    public static EmbedBuilder getMessageEmbed(String path) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        String author = getString(path+".author");
        String authorUrl = getString(path+".authorUrl");
        String authorIconUrl = getString(path+".authorIconUrl");
        if (author != null) embedBuilder.setAuthor(author, authorUrl, authorIconUrl);

        String title = getString(path+".title");
        if (title != null) embedBuilder.setTitle(title);

        String description = getString(path+".description");
        if (description != null) embedBuilder.setDescription(description);

        String url = getString(path+".url");
        if (url != null) embedBuilder.setUrl(url);

        String color = getString(path+".color");
        if (color != null) embedBuilder.setColor(Color.decode(color));

        String image = getString(path+".image");
        if (image != null) embedBuilder.setImage(image);

        String thumbnail = getString(path+".thumbnail");
        if (thumbnail != null) embedBuilder.setThumbnail(thumbnail);

        String footer = getString(path+".footer");
        String footerUrl = getString(path+".footerUrl");
        if (footer != null) embedBuilder.setFooter(footer, footerUrl);

        Integer timestamp = getInteger(path+".timestamp");
        if (timestamp != null) embedBuilder.setTimestamp(Instant.ofEpochMilli(timestamp));

        return embedBuilder;
    }
}