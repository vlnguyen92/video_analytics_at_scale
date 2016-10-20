#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/helpers.sh"

CUDNN_INSTALLERS=(
    #cudnn-8.0-linux-x64-v5.1.tgz
    cudnn-7.5-linux-x64-v5.1.tgz
#    cudnn-7.0-linux-x64-v4.0-prod.tgz
#    cudnn-7.0-linux-x64-v3.0.8-prod.tgz
)


## cuDNN
subinfo "Intalling cuDNN..."
for cudnn in ${CUDNN_INSTALLERS[@]}; do
    ### Must be downloaded manually (handled by Chameleon object store now)
    if ! [ -f $DOWNLOAD_DIR/$cudnn ]; then
        error "You have to download $cudnn to $DOWNLOAD_DIR before execute this script"
        exit 1
    fi
    ### untar to target
    cd $TOOL_DIR/
    tar xvf $DOWNLOAD_DIR/$cudnn
    verstr=$(echo ${cudnn} | sed -r 's/^.*(v[0-9.]+)(-prod)*.tgz$/\1/')
    mv cuda cudnn-${verstr}
    ### refresh PATH and other things
    source ~/.bashrc
done
