#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/helpers.sh"

PLATFORM="$1"
PROJECTS="$2"

## Javacpp
info "Building Javacpp"
cd $BUILD_DIR
if [ -d javacpp ]; then
    cd javacpp && git pull
else
    git clone https://github.com/bytedeco/javacpp.git && cd javacpp
fi
mvn install

## Javacpp presets
info "Building Javacpp-presets"
### dependencies
sudo apt-get update && sudo apt-get install -y docker.io
if command -v systemctl >/dev/null 2>&1; then
    sudo systemctl start docker
else
    sudo service docker start
fi
### source code
cd $BUILD_DIR
if [ -d javacpp-presets ]; then
    cd javacpp-presets && git pull
else
    git clone https://github.com/Aetf/javacpp-presets.git && cd javacpp-presets
fi
### install
sudo docker run -i --privileged -v /home/cc/buildbed:/usr/local/buildbed -v /usr/local/cuda:/usr/local/cuda -v /home/cc/.m2:/root/.m2 centos:7 /bin/bash <<EOF
yum -y install epel-release
yum -y install clang gcc-c++ gcc-gfortran java-devel maven python numpy swig git file which wget unzip tar bzip2 gzip xz patch make cmake perl nasm yasm atlas-devel openblas-devel freeglut-devel gtk2-devel libusb-devel libusb1-devel zlib-devel
yum -y install \`rpm -qa | sed s/.x86_64$/.i686/\`
cd /usr/local/buildbed/javacpp-presets
mvn clean install -Djavacpp.platform=$PLATFORM -Djavacpp.platform.dependency=false --projects .,$PROJECTS
EOF
### Fix permisssions
sudo chown -R cc:cc /home/cc/.m2
sudo chown -R cc:cc $BUILD_DIR
