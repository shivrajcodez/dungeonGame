#!/bin/bash
# Railway sets $PORT dynamically. Patch Tomcat's server.xml to use it.
PORT="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"${PORT}\"/" \
    /usr/local/tomcat/conf/server.xml
echo "Starting Tomcat on port ${PORT}..."
exec /usr/local/tomcat/bin/catalina.sh run
