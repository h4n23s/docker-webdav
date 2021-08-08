## docker-webdav

A simple yet powerful docker image that features a full-blown ``httpd``-powered WebDAV implementation.

### Usage

**IMPORTANT**  
The final username is composed of the group name and the actual username. They are separated by a slash:
```
<your_group_name>/<your_user_name>
```

#### Examples

_Docker CLI_
```shell
docker run --rm -it \
  --publish 80:8080 \
  --volume /path/to/your/files:/data \
  docker-webdav:1.0.0 \
  --create-mount 'mount1;/data;/dav;basic' \
  --create-group 'group1;mount1;read_write' \
  --create-user 'user1;passwd1;group1' \
  --override-default 'server.port=8080'
```

_Docker compose file_
```yaml
version: '3'

services:
  webdav:
    image: 'docker-webdav:1.0.0'
    restart: unless-stopped
    ports:
      - '80:8080'
    command: >
      --create-mount 'mount1;/data;/dav;basic'
      --create-group 'group1;mount1;read_write'
      --create-user 'user1;passwd1;group1'
      --override-default 'server.port=8080'
    volumes:
      - webdav_data:/data

volumes:
  webdav_data:
```

#### Command line options

```shell
--create-mount (-cm)      'mount1;/data;/dav;basic;Protected Area'
                           |      |     |    |     |
                           |      |     |    |     Realm (optional; default=Protected Area)
                           |      |     |    Authentication method (optional; default=basic; options=basic,digest,none)
                           |      |     Location (optional; default=/dav; example=https://localhost/dav)
                           |      Path to data (required; example=/mnt/my_mounted_drive)
                           Name of mount (required)
                          
--create-group (-cg)      'group1;mount1;read_write'
                           |      |      |
                           |      |      Mode (optional; default=read_write; options=read_only,write_only,read_write)
                           |      Name of corresposing mount (required)
                           Name of group (required)
                          
--create-user  (-cu)      'user1;passwd1;group1'
                           |     |       |
                           |     |       Name of corresposing group (required)
                           |     Password of user (required)
                           Name of user (required)
                      
--override-default  (-od) 'server.port=8080'
                           |           |
                           |           New value
                           Name of default key (required)
```

##### Defaults

| Key name | Default value | Description |
| --- | --- | --- |
| server.**user** | ``daemon`` | Specifies the user under which httpd answers requests. Cannot be ``root``.  |
| server.**group** | ``daemon`` | Specifies the group under which httpd answers requests. Cannot be ``root``. |
| server.**port** | 80 | Specifies the port the server listens on. |
| server.**name** | _no default value; set by httpd at runtime_ | Please see the [official httpd documentation](http://httpd.apache.org/docs/2.4/en/mod/core.html#servername) |
| server.path.**root** | ``/usr/local/apache2`` | Path to server root |
| server.path.**passwords** | ``/usr/local/apache2/var/passwords`` | Path to hashed passwords |
| server.path.**davlock** | ``/usr/local/apache2/var`` | Path to davlock database |
| server.authentication.basic.**hashing_cost** | 6 | BCrypt hashing cost (only applies to basic authentication) |
| server.**loglevel** | ``warn`` | Please see the [official httpd documentation](https://httpd.apache.org/docs/2.4/en/mod/core.html#loglevel) |
| server.**signature** | ``on`` | Please see the [official httpd documentation](http://httpd.apache.org/docs/2.4/en/mod/core.html#serversignature) |


#### Building this image

To build this image you will need to follow these steps:

1. **Clone** this repository and change your directory
2. **Run** ``docker build -t docker-webdav:1.0 .`` and wait. 
3. There is no third step.

Please note that building this image may require a lot of system resources due to native image compilation.

#### What won't be added

- SSL support (Please use a reverse proxy or VPN for secure transport)

#### What may be implemented in the future

- A web interface with some basic statistics and mounts/groups/users overview

### Related projects

- [webdavfs](https://github.com/miquels/webdavfs)