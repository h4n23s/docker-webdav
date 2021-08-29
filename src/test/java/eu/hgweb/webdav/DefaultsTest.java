/*
 * Copyright (C) 2021  Hannes Gehrold
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.hgweb.webdav;

import org.junit.jupiter.api.Test;

import static eu.hgweb.webdav.Defaults.defaults;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultsTest {

    @Test
    public void testGet() {
        assertEquals("daemon", defaults().get("server", "user"));
    }

    @Test
    public void testPut() {
        defaults().put(new String[]{"server", "user"}, "testUser");
        assertEquals("testUser", defaults().get("server", "user"));
    }
}
