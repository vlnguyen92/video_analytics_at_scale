#! /usr/bin/env bash
set -o nounset
set -o errexit
set -o pipefail

CORE_NUMBER=$(grep -c ^processor /proc/cpuinfo)

## Helper functions
download() {
    if [ "$#" -eq 2 ]; then
        if [ -f "$2/$1" ]; then
            return
        fi
        cd "$2" && { curl -OJL "$1" ; cd -; }
    elif [ "$#" -eq 1 ]; then
        mkdir -p $DOWNLOAD_DIR
        download "$1" $DOWNLOAD_DIR
    else
        error "Invalid argument to download"
        exit 1
    fi
}

info() {
    local bold=$(tput bold)
    local green=$(tput setaf 2)
    local reset=$(tput sgr0)
    echo -en "${green}${bold}==> ${reset}"
    echo -e "${bold}$@${reset}"
}

subinfo() {
    local bold=$(tput bold)
    local green=$(tput setaf 2)
    local reset=$(tput sgr0)
    echo -en "${green}${bold}====> ${reset}"
    echo -e "${bold}$@${reset}"
}

error() {
    local bold=$(tput bold)
    local red=$(tput setaf 1)
    local reset=$(tput sgr0)
    echo -en "${red}${bold}==> ${reset}"
    echo >&2 -e "${bold}$@${reset}"
    exit 1
}

prompt() {
    local bold=$(tput bold)
    local yellow=$(tput setaf 1)
    local reset=$(tput sgr0)
    local waitingforanswer=true
    while ${waitingforanswer}; do
        echo -en "${yellow}${bold}==> ${reset}"
        echo >&2 -e "${bold}$@${reset}"
        read -n 1 ynanswer
        case ${ynanswer} in
            [Yy] )
                waitingforanswer=false;
                echo ""
                return 0
                ;;
            [Nn] )
                echo "";
                info "Opeartion cancelled by user"
                return 1
                ;;
            *    )
                echo "";
                echo -en "${yellow}${bold}==> ${reset}"
                echo >&2 -e "${bold}Please answer either yes (y/Y) or no (n/N).${reset}";;
        esac
    done
}

function join_by {
    local IFS="$1"; shift; echo "$*";
}
