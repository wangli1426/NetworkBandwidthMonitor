#!/bin/sh

OPTIND=1

verbose=0

args=""

while getopts "h?c:vf:" opt; do
    case "$opt" in
    h|\?)
        args="$args -h"
	;;
    c)  config_file=$OPTARG
        args="$args -c $config_file"
        ;;
    esac
done


command="java -cp ../target/NetworkBandwitdhMonitor-1.0-SNAPSHOT-jar-with-dependencies.jar edu.illinois.adsc.resa.network_monitor.daemon.Client $args"

$command
