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

import flioris.util.reaction.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class GuildSettings {
    private String botChannelId;
    private String lang;
    private boolean antinukeEnabled;
    private byte memberRemoveLimit;
    private byte memberRenameLimit;
    private byte channelDeleteLimit;
    private byte channelCreateLimit;
    private byte channelRenameLimit;
    private byte roleDeleteLimit;
    private byte roleCreateLimit;
    private byte roleRenameLimit;
    private Set<String> whitelist;
    private short cooldown;
    private ReactionType reactionType;
}
