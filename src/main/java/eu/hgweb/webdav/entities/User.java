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

package eu.hgweb.webdav.entities;

import java.util.Objects;

public class User {

    private final String name;
    private final String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;

        validateName();
        checkPassword();
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    private void checkPassword() {
        if (password.length() < 8) {
            System.err.printf("Password of user '%s' is weak. It's probably a good idea to improve it's length.%n", name);
        }
    }

    private void validateName() {
        if (!name.matches("[a-zA-Z0-9]+")) {
            throw new IllegalStateException(String.format("Username '%s' must match /[a-zA-Z0-9]+/", name));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
