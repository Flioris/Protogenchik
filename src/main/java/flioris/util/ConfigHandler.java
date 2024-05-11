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

public class ConfigHandler {
    private static final File FILE = new File("config.json");
    private static JSONObject config;

    public static void load() {
        try {
            if (!FILE.exists()) {
                Files.copy(ConfigHandler.class.getClassLoader().getResourceAsStream("config.json"), FILE.toPath());
            }
            config = new JSONObject(new JSONTokener(new FileReader(FILE)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object get(String path) {
        Object json = config;

        for (String key : path.split("\\.")) {
            try {
                json = ((JSONObject) json).get(key);
            } catch (Exception e) {
                return null;
            }
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
        String author = getString(path + ".author");
        String authorUrl = getString(path + ".authorUrl");
        String authorIconUrl = getString(path + ".authorIconUrl");
        String title = getString(path + ".title");
        String description = getString(path + ".description");
        String url = getString(path + ".url");
        String color = getString(path + ".color");
        String image = getString(path + ".image");
        String thumbnail = getString(path + ".thumbnail");
        String footer = getString(path + ".footer");
        String footerUrl = getString(path + ".footerUrl");
        Integer timestamp = getInteger(path + ".timestamp");

        if (author != null) {
            embedBuilder.setAuthor(author, authorUrl, authorIconUrl);
        }
        if (title != null) {
            embedBuilder.setTitle(title);
        }
        if (description != null) {
            embedBuilder.setDescription(description);
        }
        if (url != null) {
            embedBuilder.setUrl(url);
        }
        if (color != null) {
            embedBuilder.setColor(Color.decode(color));
        }
        if (image != null) {
            embedBuilder.setImage(image);
        }
        if (thumbnail != null) {
            embedBuilder.setThumbnail(thumbnail);
        }
        if (footer != null) {
            embedBuilder.setFooter(footer, footerUrl);
        }
        if (timestamp != null) {
            embedBuilder.setTimestamp(Instant.ofEpochMilli(timestamp));
        }

        return embedBuilder;
    }
}