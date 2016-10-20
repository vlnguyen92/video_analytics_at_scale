#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/helpers.sh"

## Caffe
subinfo "Intalling Caffe from git..."
cd $BUILD_DIR
### dependencies
sudo apt-get install -y libprotobuf-dev libhdf5-serial-dev protobuf-compiler
sudo apt-get install -y --no-install-recommends libboost-all-dev
sudo apt-get install -y libgflags-dev libgoogle-glog-dev libopenblas-dev
sudo apt-get install -y python-dev python-numpy
### clone code and config
git clone https://github.com/BVLC/caffe.git
cd caffe && mkdir build && cd build
cmake -DCMAKE_INSTALL_PREFIX=$TOOL_DIR/caffe \
      -DUSE_LMDB=OFF \
      -DUSE_LEVELDB=OFF \
      -DUSE_OPENCV=OFF \
      -DBLAS=open \
      ..
### build and install
make -j$CORE_NUMBER install
### refresh PATH and other things
source ~/.bashrc
