#! /usr/bin/env bash

DOWNLOAD_DIR=~/downloads
BUILD_DIR=~/buildbed
TOOL_DIR=~/tools

GPU_AVAILABLE=$(lsmod | grep nouveau)

#CUDA_REPO_DEB="http://developer.download.nvidia.com/compute/cuda/7.5/Prod/local_installers/cuda-repo-ubuntu1504-7-5-local_7.5-18_amd64.deb"
CUDA_REPO_DEB="http://developer.download.nvidia.com/compute/cuda/7.5/Prod/local_installers/cuda-repo-ubuntu1404-7-5-local_7.5-18_amd64.deb"

#----------------------------------------------------------------------------------------------------------------------------------

#
# Script initialization
#
# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/scripts/helpers.sh"

info "Script initialization done"

# Fix configuration
info "Fixing system configuration"
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts
sudo cp "$__dir"/cloud.cfg /etc/cloud/cloud.cfg

# Generic system update
info "Updating system"
sudo apt-get update && sudo apt-get -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" dist-upgrade -y

# Setup Chameleon Object Store
if [ -f "$DOWNLOAD_DIR/CH-818207-openrc.sh"]; then
    info "Authenticating against Chameleon"
    source "$DOWNLOAD_DIR/CH-818207-openrc.sh"
    if [ "${GPU_AVAILABLE}x" != "x" ]; then
        info "Download files from object store"
        cd $DOWNLOAD_DIR
        swift download storm_files cuda_7.5.18_linux.run || true
        swift download storm_files cudnn-7.5-linux-x64-v5.1.tgz || true
    fi
fi


if [ "${GPU_AVAILABLE}x" != "x" ]; then
    # Install nvidia driver and cuda
    info "Installing nvidia driver and cuda library"
    sudo add-apt-repository -y ppa:graphics-drivers/ppa
    sudo apt-get update && sudo apt-get dist-upgrade -y
    sudo apt-get install -y linux-headers-generic ubuntu-drivers-common
    # seems not necessary as cuda will pull in driver automatically
    #sudo ubuntu-drivers autoinstall

    download $CUDA_REPO_DEB
    sudo dpkg -i $DOWNLOAD_DIR/cuda-repo-ubuntu1404-7-5-local_7.5-18_amd64.deb
    sudo apt-get update && sudo apt-get install -y cuda
    #download http://developer.download.nvidia.com/compute/cuda/7.5/Prod/local_installers/cuda_7.5.18_linux.run
    #sudo bash $DOWNLOAD_DIR/cuda_7.5.18_linux.run --silent --toolkit
    #echo '/usr/local/cuda-7.5/lib64' | sudo tee /etc/ld.so.conf.d/cuda.conf
    #sudo ldconfig
fi

# Install other build tools
info "Installing other build tools"
sudo apt-get install -y build-essential cmake maven git gnome-terminal vim
curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash
sudo add-apt-repository -y ppa:mc3man/trusty-media && sudo apt-get update
sudo apt-get install -y ffmpeg


# Setup bash shell
info "Setting up bash shell"
cd $HOME
for file in bashrc bash_profile; do
    download https://github.com/Aetf/Dotfiles/raw/CC/home/$file
    mv $DOWNLOAD_DIR/$file $HOME/.$file
done
source ~/.bashrc

# Python related
#info "Setting up python"
#sudo apt-get install -y virtualenv
#virtualenv -p python2 --system-site-packages ~/venvs/stormgpu
#~/venvs/stormgpu/bin/pip install Cython
#~/venvs/stormgpu/bin/pip install scipy

# Java related
info "Setting up java"
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer oracle-java8-set-default

# Install third party tools
info "Installing third party tools"
if [ -d "$BUILD_DIR" ]; then
    if prompt "Build directory $BUILD_DIR already exists, remove?"; then
        rm -rf $BUILD_DIR
    fi
fi
if [ -d "$TOOL_DIR" ]; then
    if prompt "Tool directory $TOOL_DIR already exists, remove?"; then
        rm -rf $TOOL_DIR
    fi
fi
mkdir -p $TOOL_DIR $BUILD_DIR

if [ "${GPU_AVAILABLE}x" != "x" ]; then
    scripts/install-cudnn.sh
fi

scripts/install-storm.sh

#scripts/compile-caffe.sh

#scripts/compile-opencv.sh

#scripts/compile-caffeC3D.sh

## Our project
cd $HOME
if [ -d video_analytics_at_scale ]; then
    cd video_analytics_at_scale && git lfs init && git pull && git checkout -f HEAD
else
    git clone https://github.com/vlnguyen92/video_analytics_at_scale.git
    cd video_analytics_at_scale && git lfs init
fi

if prompt "Basic setup done, proceed to javacpp compilation?"; then
    scripts/compile-javacpp.sh linux-x86_64 "opencv,caffe,caffeC3DSampleRate,caffeC3DOverlapLoss"
fi

## Everything done
info "Everything done successfully. Remember to reboot if GPU driver installed."
