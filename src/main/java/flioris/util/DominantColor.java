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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class DominantColor {
    
    public static Color get(String url) {
        try {
            BufferedImage image = ImageIO.read(new URL(url));

            int redCount = 0;
            int greenCount = 0;
            int blueCount = 0;

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = new Color(image.getRGB(x, y));

                    redCount += color.getRed();
                    greenCount += color.getGreen();
                    blueCount += color.getBlue();
                }
            }
            int pixelCount = image.getWidth() * image.getHeight();
            int redAverage = redCount / pixelCount;
            int greenAverage = greenCount / pixelCount;
            int blueAverage = blueCount / pixelCount;

            return new Color(redAverage, greenAverage, blueAverage);
        } catch (IOException ignored) {return null;}
    }
}
