#!/usr/bin/sh

MEMORY_RESERVATION=$(cat /sys/fs/cgroup/memory/memory.soft_limit_in_bytes)

# Cap memory reservation to 6GB
if [ $MEMORY_RESERVATION -gt 10000000000 ]; then
  MEMORY_RESERVATION=6000000000
fi

# XMS to 50% of reserved memory, in MiB
XMS=$((($MEMORY_RESERVATION / 1024 / 1024) * 50 / 100))

# XMX to 80% of reserved memory, in MiB (jvm k, m, and g prefixes are referred to as kilo/mega/gigabytes in docs, but refer to mebi,gibi, etc. Powers of 1024)
XMX=$((($MEMORY_RESERVATION / 1024 / 1024) * 80 / 100))

export JAVA_OPTS="$JAVA_OPTS -Xms${XMS}m -Xmx${XMX}m"

java ${JAVA_OPTS} -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -jar waypoint-authorization-api.jar