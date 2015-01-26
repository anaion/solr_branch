#! /bin/sh

# the live assembly script to be run when ecolex is in the same workspace
# as ledge projects

`dirname $0`/ledge-web_live_assembly.sh ecolex-webapp "ecolex|ledge" "ledge-cyklotron|ledge-forms|cyklotron"

