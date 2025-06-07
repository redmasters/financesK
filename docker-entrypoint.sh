#!/bin/sh
# Importa o certificado se existir
if [ -f "/etc/postgresql-ssl/root.crt" ]; then
    export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    keytool -importcert -noprompt \
        -alias supabase-root \
        -file "/etc/postgresql-ssl/root.crt" \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit
fi

exec java -jar app.jar
