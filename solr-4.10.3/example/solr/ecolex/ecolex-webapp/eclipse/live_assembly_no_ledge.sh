#! /bin/sh

# the live assembly script to be run when ecolex is in a workspace
# separate from ledge workspace

`dirname $0`/ledge-web_live_assembly.sh ecolex-webapp "ecolex" "ledge-cyklotron|ledge-forms|cyklotron"

