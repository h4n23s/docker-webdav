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

import eu.hgweb.webdav.enums.Mode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Group {

    private final String name;
    private final List<User> users = new LinkedList<>();

    private Mode mode = Mode.read_write;

    public Group(String name) {
        this.name = name;
        validateName();
    }

    public Group(String name, Mode mode) {
        this(name);
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private void validateName() {
        if (!name.matches("[a-zA-Z0-9]+")) {
            throw new IllegalStateException(String.format("Group name '%s' must match /[a-zA-Z0-9]+/", name));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return name.equals(group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
