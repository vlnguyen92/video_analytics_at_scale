#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/helpers.sh"

## OpenCV
subinfo "Intalling OpenCV from git..."
cd $BUILD_DIR
### dependencies
sudo apt-get install cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev
sudo apt-get install python-dev python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libjasper-dev libdc1394-22-dev
### source code and config
git clone https://github.com/opencv/opencv.git
git clone https://github.com/opencv/opencv_contrib.git
cd opencv
mkdir build && cd build
#### disable cudalegacy, which is incompatible with cuda 8.0
cmake -DCMAKE_BUILD_TYPE=RELEASE \
      -DCMAKE_INSTALL_PREFIX=$TOOL_DIR/opencv \
      -DOPENCV_EXTRA_MODULES_PATH=$BUILD_DIR/opencv_contrib/modules \
      ..
### build and install
make -j$CORE_NUMBER install
### refresh PATH and other things
source ~/.bashrc
