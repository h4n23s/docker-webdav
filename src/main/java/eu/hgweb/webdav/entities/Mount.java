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

import eu.hgweb.webdav.enums.Authentication;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Mount {

    private final String name;
    private final String path;
    private final List<Group> groups = new LinkedList<>();

    private String location = "/dav";
    private Authentication authentication = Authentication.basic;
    private String realm = "Protected Area";

    public Mount(String name, String path) {
        this.name = name;
        this.path = path;

        validateName();
        validateLocation();
    }

    public Mount(String name, String path, String location) {
        this(name, path);
        this.location = location;
    }

    public Mount(String name, String path, String location, Authentication authentication) {
        this(name, path, location);
        this.authentication = authentication;
    }

    public Mount(String name, String path, String location, Authentication authentication, String realm) {
        this(name, path, location, authentication);
        this.realm = realm;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    private void validateName() {
        if (!name.matches("[a-zA-Z0-9]+")) {
            throw new IllegalStateException(String.format("Mount name '%s' must match /[a-zA-Z0-9]+/", name));
        }
    }

    private void validateLocation() {
        if (!location.matches("^/.+")) {
            throw new IllegalStateException(String.format("Location '%s' must match /^\\/.+/", location));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mount mount = (Mount) o;
        return name.equals(mount.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
