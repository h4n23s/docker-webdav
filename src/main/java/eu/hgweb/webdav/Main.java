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

import com.google.common.io.Resources;
import eu.hgweb.webdav.entities.Group;
import eu.hgweb.webdav.entities.Mount;
import eu.hgweb.webdav.entities.User;
import eu.hgweb.webdav.enums.Authentication;
import eu.hgweb.webdav.enums.Mode;
import liqp.Template;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.google.common.io.Resources.getResource;
import static java.util.Arrays.stream;

@SuppressWarnings({"UnstableApiUsage"})
public class Main {

    private static final Logger LOG = Logger.getLogger("docker-webdav");

    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException {
        Main main = new Main();
        main.run(args);
    }

    @SuppressWarnings("unchecked")
    private void run(String[] args) throws IOException, ParseException, NoSuchAlgorithmException {

        Security.addProvider(new BouncyCastleProvider());

        // Parse command line options and create POJOs for template rendering.
        Options options = new Options()
                .addOption(Option.builder("cm")
                        .longOpt("create-mount")
                        .hasArgs()
                        .valueSeparator(';')
                        .build())
                .addOption(Option.builder("cg")
                        .longOpt("create-group")
                        .hasArgs()
                        .valueSeparator(';')
                        .build())
                .addOption(Option.builder("cu")
                        .longOpt("create-user")
                        .hasArgs()
                        .valueSeparator(';')
                        .build())
                .addOption(Option.builder("od")
                        .longOpt("override-default")
                        .hasArgs()
                        .valueSeparator('=')
                        .build());

        CommandLine commandLine = new DefaultParser().parse(options, args);

        // Iterate through --create-mount options
        List<Mount> mounts = new LinkedList<>();

        Stream<Option> createMountOptions = stream(commandLine.getOptions()).filter(option -> Objects.equals(option.getOpt(), "cm"));
        createMountOptions.forEach(option -> {

            String[] values = option.getValues();
            Mount mount;

            if(values.length == 2) {
                mount = new Mount(values[0], values[1]);
            } else if(values.length == 3) {
                mount = new Mount(values[0], values[1], values[2]);
            } else if(values.length == 4) {
                Authentication authentication = Authentication.valueOf(values[3].toLowerCase());
                mount = new Mount(values[0], values[1], values[2], authentication);
            } else if(values.length == 5) {
                Authentication authentication = Authentication.valueOf(values[3].toLowerCase());
                mount = new Mount(values[0], values[1], values[2], authentication, values[4]);
            } else {
                throw new IllegalStateException("There were too much or too less arguments specified.");
            }

            mounts.add(mount);
            LOG.info(String.format("Successfully added mount '%s'", mount.getName()));

        });

        // Iterate through --create-group options
        Stream<Option> createGroupOptions = stream(commandLine.getOptions()).filter(option -> Objects.equals(option.getOpt(), "cg"));
        createGroupOptions.forEach(option -> {

            String[] values = option.getValues();
            Group group;

            if(values.length == 2) {
                group = new Group(values[0]);
            } else if(values.length == 3) {
                group = new Group(values[0], Mode.valueOf(values[2]));
            } else {
                throw new IllegalStateException("There were too much or too less arguments supplied.");
            }

            // Add group to corresponding mount points
            Set<String> mountNames = Set.of(values[1].split(","));

            for(Mount mount : mounts) {
                if(mountNames.contains(mount.getName())) {
                    mount.getGroups().add(group);
                    LOG.info(String.format("Successfully added group '%s' to mount '%s'", group.getName(), mount.getName()));
                }
            }

        });

        // Iterate through --create-user options
        Stream<Option> createUserOptions = stream(commandLine.getOptions()).filter(option -> Objects.equals(option.getOpt(), "cu"));
        createUserOptions.forEach(option -> {

            String[] values = option.getValues();
            User user;

            if(values.length == 3) {
                user = new User(values[0], values[1]);
            } else {
                throw new IllegalStateException("There were too much or too less arguments supplied.");
            }

            // Add user to corresponding groups
            Set<String> groupNames = Set.of(values[2].split(","));

            for(Mount mount : mounts) {
                for(Group group : mount.getGroups()) {
                    if(groupNames.contains(group.getName())) {
                        group.getUsers().add(user);
                        LOG.info(String.format("Successfully added user '%s' to group '%s'", user.getName(), group.getName()));
                    }
                }
            }

        });

        // Iterate through --override-default options
        Stream<Option> overrideDefaultOptions = stream(commandLine.getOptions()).filter(option -> Objects.equals(option.getOpt(), "od"));
        overrideDefaultOptions.forEach(option -> {

            String[] values = option.getValues();

            if(values.length != 2) {
                throw new IllegalStateException("There were too much or too less arguments supplied.");
            }

            Defaults.getInstance().put(values[0].split("\\."), values[1]);

        });

        // Render template and save generated 'dav.conf' in current directory.
        Map<String, Object> templateVariables = Map.of(
                "mounts", mounts,
                "defaults", Defaults.getInstance()
        );

        String renderedConfig = Template.parse(Resources
                        .toString(getResource("eu/hgweb/webdav/httpd.conf.liquid"), StandardCharsets.UTF_8))
                .render(templateVariables);

        File configFile = new File(System.getProperty("user.dir"), "httpd.conf");

        String renderedEntrypoint = Template.parse(Resources
                        .toString(getResource("eu/hgweb/webdav/setup.sh.liquid"), StandardCharsets.UTF_8))
                .render(templateVariables);

        File entrypointFile = new File(System.getProperty("user.dir"), "setup.sh");

        String renderedHealthcheck = Template.parse(Resources
                        .toString(getResource("eu/hgweb/webdav/healthcheck.sh.liquid"), StandardCharsets.UTF_8))
                .render(templateVariables);

        File healthcheckFile = new File(System.getProperty("user.dir"), "healthcheck.sh");

        FileUtils.writeStringToFile(configFile, renderedConfig, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(entrypointFile, renderedEntrypoint, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(healthcheckFile, renderedHealthcheck, StandardCharsets.UTF_8);

        // Now generate passwords file for each mount
        for(Mount mount : mounts) {
            StringBuilder stringBuilder = new StringBuilder();

            switch (mount.getAuthentication()) {

                case basic:
                    for(Group group : mount.getGroups()) {
                        for(User user : group.getUsers()) {
                            LOG.info(String.format("Generating password hash for user '%s' in group '%s' for mount '%s'. This may take some time.", user.getName(), group.getName(), mount.getName()));

                            byte[] salt = new byte[16];
                            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
                            secureRandom.nextBytes(salt);

                            int hashingCost = Integer.parseInt(Defaults.getInstance().get("server", "authentication", "basic", "hashing_cost"));
                            String hashedPassword = OpenBSDBCrypt.generate("2y", user.getPassword().toCharArray(), salt, hashingCost);

                            // When logging in, the user has to authenticate with a username in the following format:
                            // <group_name>/<username>
                            stringBuilder.append(String.join("/", group.getName(), user.getName())).append(":")
                                    .append(hashedPassword)
                                    .append(System.lineSeparator());
                        }
                    }
                    break;

                case digest:
                    for(Group group : mount.getGroups()) {
                        for (User user : group.getUsers()) {
                            LOG.info(String.format("Generating digest for user '%s' in group '%s' for mount '%s'", user.getName(), group.getName(), mount.getName()));

                            // Derivative of https://httpd.apache.org/docs/2.4/misc/password_encryptions.html#digest
                            byte[] digest = MessageDigest.getInstance("MD5").digest(
                                    String.join(":", user.getName(), mount.getRealm(), user.getPassword()).getBytes(StandardCharsets.UTF_8)
                            );

                            BigInteger bigInteger = new BigInteger(1, digest);
                            StringBuilder password = new StringBuilder(bigInteger.toString(16));

                            while (password.length() < 32) {
                                password.append("0").append(password);
                            }

                            // When logging in, the user has to authenticate with a username in the following format:
                            // <group_name>/<username>
                            stringBuilder.append(String.join("/", group.getName(), user.getName())).append(":")
                                    .append(mount.getRealm()).append(":")
                                    .append(password)
                                    .append(System.lineSeparator());
                        }
                    }
                    break;
            }

            File passwordsFile = new File(System.getProperty("user.dir") + "/passwords", mount.getName());
            FileUtils.writeStringToFile(passwordsFile, stringBuilder.toString(), StandardCharsets.UTF_8);
        }
    }

}
