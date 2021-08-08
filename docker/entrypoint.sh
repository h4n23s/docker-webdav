#!/usr/bin/env sh

cd /var/local/webdav/ || exit 1

cat << 'EOF'
      _            _                            _         _
     | |          | |                          | |       | |
   __| | ___   ___| | _____ _ __  __      _____| |__   __| | __ ___   __
  / _` |/ _ \ / __| |/ / _ \ '__| \ \ /\ / / _ \ '_ \ / _` |/ _` \ \ / /
 | (_| | (_) | (__|   <  __/ |     \ V  V /  __/ |_) | (_| | (_| |\ V /
  \__,_|\___/ \___|_|\_\___|_|      \_/\_/ \___|_.__/ \__,_|\__,_| \_/

Entering setup...

EOF

# Run setup
chmod +x setup-webdav && ./setup-webdav "$@" && \
 chmod +x setup.sh && ./setup.sh