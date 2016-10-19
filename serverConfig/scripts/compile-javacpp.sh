#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

CORE_NUMBER=$(grep -c ^processor /proc/cpuinfo)

## Javacpp presets
info "Building Javacpp-presets"
### dependencies
sudo apt-get update && sudo apt-get install -y docker.io
sudo systemctl start docker
### source code
cd $BUILD_DIR
git clone https://github.com/Aetf/javacpp-presets.git
### install
sudo docker run -i --privileged -v /home/cc/buildbed:/usr/local/buildbed -v /usr/local/cuda:/usr/local/cuda -v /home/cc/.m2:/root/.m2 centos:7 /bin/bash <<EOF
yum -y install epel-release
yum -y install clang gcc-c++ gcc-gfortran java-devel maven python numpy swig git file which wget unzip tar bzip2 gzip xz patch make cmake perl nasm yasm atlas-devel openblas-devel freeglut-devel gtk2-devel libusb-devel libusb1-devel zlib-devel
yum install \`rpm -qa | sed s/.x86_64$/.i686/\`
cd /usr/local/buildbed/javacpp-presets
mvn clean install -Djavacpp.platform=linux-x86_64 -Djavacpp.platform.dependency=false --projects .,opencv,ffmpeg,caffe,caffeC3DOverlapLoss,caffeC3DSampleRate
EOF
### Fix permisssions
sudo chown -R cc:cc /home/cc/.m2
sudo chown -R cc:cc $BUILD_DIR
