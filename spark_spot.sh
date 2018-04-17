#!/bin/bash
# POSIX

die() {
    printf '%s\n' "$1" >&2
    exit 1
}

show_help() {
    echo "$usage"
}

print_command() {
    echo "The actual action performed is the following:"
    echo $command
}

# Initialize all the option variables.
# This ensures we are not contaminated by variables from the environment.
spot=1
park=fd00::c30c:0:0:
user=
credit=
free=-1
observe=0
address=
usage="$(basename "$0") [ACTION] [OPTIONS] -- SPARK Basic User Interface
Usage:
 [ACTION]:
    -h, --help                      show this help text and exit
    -f, --free                      frees a parking spot
    -o, --occupy                    occupies a parking spot
    -g, --get                       retrieves the value of a parking spot
    -O, --observe                   oberves the status of a spot over time

 [OPTIONS]:
    -p, --park [=$park]   IPv6 base address of a park, missing the spot information
    -s, --spot [=$spot]                 number of the target park spot

 [OPTIONS REQUIRED BY THE --occupy ACTION]
    -u, --user                      the username of the user that occupies the spot
    -c, --credit                    the credit information of the user that occupies the spot
"

case $1 in
    -o|--occupy)
        free=2
        ;;
    -f|--free)
        free=1
        ;;
    -g|--get)
        free=-1
        ;;
    -O|--observe)
        observe=1
        ;;
    -h|-\?|--help)
        show_help
        exit
        ;;
    *)
        show_help
        exit
        ;;
esac

shift

while :; do
    case $1 in
        -p|--park)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                park=$2
                shift
            else
                die 'ERROR: "--park" requires a non-empty option argument.'
            fi
            ;;

        -s|--spot)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                spot=$2
                shift
            else
                die 'ERROR: "--spot" requires a non-empty option argument.'
            fi
            ;;

        -u|--user)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                user=$2
                shift
            else
                die 'ERROR: "--user" requires a non-empty option argument.'
            fi
            ;;

        -c|--credit)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                credit=$2
                shift
            else
                die 'ERROR: "--credit" requires a non-empty option argument.'
            fi
            ;;
        --)              # End of all options.
            shift
            break
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            ;;
        *)               # Default case: No more options, so break out of the loop.
            break
    esac

    shift
done

spot=$((spot+1))
spot=$( printf "%x" $spot ) ;
address="coap://[$park$spot]:5683/park_spot"

case $free in
    -1)
        if [ $observe -eq "1" ]; then
            echo "Starting to observe the required spot."
            command="coap get -o $address"
            print_command
            eval $command
            exit
        else
            echo "Requiring the state of a spot."
            command="coap get $address"
            print_command
            eval $command
            exit
        fi
        ;;
    1)
        echo "Freeing the required spot."
        command="echo \"free=1\" | coap post $address"
        print_command
        eval $command
        ;;
    2)
        # Gotta test whether the user provided the required information
        if [ -z "$user" ]; then
            die 'ERROR: You must provide a content to the "--user" option.'
        fi

        if [ -z "$credit" ]; then
            die 'ERROR: You must provide a content to the "--credit" option.'
        fi
        echo "Occupying the required spot."
        command="echo \"free=2&user=$user&credit=$credit\" | coap post $address"
        print_command
        eval $command
        ;;
esac

