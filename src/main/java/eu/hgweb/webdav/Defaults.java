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

import java.util.Dictionary;
import java.util.Hashtable;

public class Defaults extends Hashtable<String, Object> {

    private static volatile Defaults defaults;

    {
        put("server", new Hashtable<String, Object>() {{

            put("user", "daemon");
            put("group", "daemon");
            put("port", "80");
            put("name", "");

            put("path", new Hashtable<String, Object>() {{

                put("root", "/usr/local/apache2");
                put("passwords", "/usr/local/apache2/var/passwords");
                put("davlock", "/usr/local/apache2/var");

            }});

            put("authentication", new Hashtable<String, Object>() {{

                put("basic", new Hashtable<>() {{
                    put("algorithm", new Hashtable<>() {{
                        put("type", "bcrypt");
                        put("complexity", "6");
                    }});
                }});

            }});

            put("loglevel", "warn");
            // Some clients rely on the server signature to judge whether to enable some non-standard features.
            put("signature", "on");

        }});

    }

    @SuppressWarnings("unchecked")
    public String get(String... keys) {
        Dictionary<String, Object> currentDictionary = this;

        for(String key : keys) {
            Object value = currentDictionary.get(key);

            if(value instanceof Dictionary) {
                currentDictionary = (Dictionary<String, Object>) value;
            } else if(value instanceof String) {
                return (String) value;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public void put(String[] keys, String newValue) {
        Dictionary<String, Object> currentDictionary = this;

        for(String key : keys) {
            Object value = currentDictionary.get(key);

            if(value instanceof Dictionary) {
                currentDictionary = (Dictionary<String, Object>) value;
            } else {
                currentDictionary.put(key, newValue);
                break;
            }
        }
    }

    public static Defaults defaults() {
        if(defaults == null) {
            synchronized (Defaults.class) {
                if(defaults == null) {
                    defaults = new Defaults();
                }
            }
        }

        return defaults;
    }

}
