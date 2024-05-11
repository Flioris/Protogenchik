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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayedExecution {

    public static void start(Runnable runnable, int seconds) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        executor.schedule(runnable, seconds, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
