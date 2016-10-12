#! /usr/bin/env bash

DOWNLOAD_DIR=~/downloads
BUILD_DIR=~/buildbed
TOOL_DIR=~/tools

CORE_NUMBER=$(grep -c ^processor /proc/cpuinfo)

CUDA_REPO_DEB="https://develop.nvidia.com/compute/cuda/8.0/prod/local_installers/cuda-repo-ubuntu1604-8-0-local_8.0.44-1_amd64-deb"
CUDNN_INSTALLERS=(
    cudnn-8.0-linux-x64-v5.1.tgz
#    cudnn-7.0-linux-x64-v4.0-prod.tgz
#    cudnn-7.0-linux-x64-v3.0.8-prod.tgz
)

#----------------------------------------------------------------------------------------------------------------------------------

#
# Script initialization
#

set -o nounset
set -o errexit
set -o pipefail

## Helper functions
download() {
    if [ "$#" -eq 2 ]; then
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

info "Script initialization done"

#
# Generic system update
#

# Generic system update
info "Updating system"
sudo apt-get update && sudo apt-get dist-upgrade -y

# Install nvidia driver and cuda
info "Installing nvidia driver and cuda library"
sudo add-apt-repository -y ppa:graphics-drivers/ppa
sudo apt-get update && sudo apt-get dist-upgrade -y
sudo apt-get install -y linux-headers-generic

download $CUDA_REPO_DEB
sudo dpkg -i $DOWNLOAD_DIR/*.deb
sudo apt-get update && sudo apt-get install -y cuda

# Install other build tools
info "Installing other build tools"
sudo apt-get install -y build-essentials cmake maven git gnome-terminal

# Setup bash shell
info "Setting up bash shell"
cd $HOME
for file in bashrc bash_profile; do
    download https://github.com/Aetf/Dotfiles/raw/CC/home/$file
    mv $DOWNLOAD_DIR/$file $HOME/.$file
done
source ~/.bashrc

# Python related
info "Setting up python"
sudo apt-get install -y virtualenv
virtualenv -p python2 --system-site-packages ~/venvs/stormgpu
~/venvs/stormgpu/bin/pip install Cython
~/venvs/stormgpu/bin/pip install scipy

# Java related
info "Setting up java"
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer

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

## cuDNN
subinfo "Intalling cuDNN..."
for cudnn in ${CUDNN_INSTALLERS[@]}; do
    ### Must be downloaded manually
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

## Java bindings for various libraries
wget -P $DOWNLOAD_DIR http://search.maven.org/remotecontent?filepath=org/bytedeco/javacpp-presets/1.2/javacpp-presets-1.2-bin.zip
unzip $DOWNLOAD_DIR/*-javacpp-presets-1.2-bin.zip $TOOL_DIR
mvn install:install-file -Dfile=$TOOL_DIR/javacpp-presets-bin/ffmpeg.jar -DgroupId=org.bytedeco -DartifactId=ffmpeg -Dversion=1.2 -Dpackaging=jar
git clone https://github.com/vlnguyen92/video_analytics_at_scale.git

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

## Zookeeper
subinfo "Installing Zookeeper 3.4.9"
cd $TOOL_DIR
### download release package
download http://www-eu.apache.org/dist/zookeeper/current/zookeeper-3.4.9.tar.gz
tar xvf $DOWNLOAD_DIR/zookeeper-3.4.9.tar.gz
mv zookeeper* zookeeper
### config
cd zookeeper/conf
cp zoo_sample.cfg zoo.cfg

## Storm
subinfo "Installing Apache Storm 1.0.2"
cd $TOOL_DIR
### download release package
download http://www-us.apache.org/dist/storm/apache-storm-1.0.2/apache-storm-1.0.2.tar.gz
tar xvf $DOWNLOAD_DIR/apache-storm-1.0.2.tar.gz
mv apache-storm* storm
### config
cd storm/conf
cat >> storm.yaml <<EOF
storm.local.dir: "/tmp/storm"
java.library.path: "$JAVA_HOME:$LD_LIBRARY_PATH"

storm.zookeeper.port: 2181
storm.zookeeper.servers:
    - "$(hostname)"
    
nimbus.seeds:
    - "$(hostname)"
nimbus.thrift.port: 6627
nimbus.cleanup.inbox.freq.secs: 60
nimbus.inbox.jar.expiration.secs: 20

ui.port: 8772

supervisor.slots.ports:
    - 6700
    - 6701
    - 6702
    - 6703
    - 6704
    - 6705
EOF

## C3D Caffe
subinfo "Installing C3D Caffe..."
cd $BUILD_DIR
### dependencies
sudo apt-get install -y libleveldb-dev libsnappy-dev
### skip normal caffe
mv $TOOL_DIR/caffe $TOOL_DIR/caffe.skip
source ~/.bashrc
### clone and config
if [ -d "$BUILD_DIR/scnn" ]; then
    cd scnn && git pull
    git reset --hard # removes staged and working directory changes
    git clean -fxd :/ # remove untracked and ingored fiels through all repo
else
    git clone https://github.com/zhengshou/scnn.git && cd scnn
fi
#### overlap_loss
cd C3D_overlap_loss
sed -i -r 's#^(CUDA_DIR := ).*$#\1/usr/local/cuda-8.0#' Makefile.config
sed -i -r 's#^(BLAS := ).*$#\1open#' Makefile.config
sed -i -r 's#/usr/local/include/python2\.7#/usr/include/python2.7#' Makefile.config
sed -i -r 's#/usr/local/lib/python2\.7#/usr/lib/python2.7#' Makefile.config
sed -i -r 's#^(DEBUG := ).*$#\10#' Makefile.config
sed -i -r 's#^(INCLUDE_DIRS := ).*$#\1$(PYTHON_INCLUDE) $(subst :, ,$(INCLUDE)) /usr/include/hdf5/serial#' Makefile.config
sed -i -r 's#^(LIBRARY_DIRS := ).*$#\1$(PYTHON_LIB) $(subst :, ,$(LIB)) /usr/lib/x86_64-linux-gnu/hdf5/serial#' Makefile.config
##### fix opencv 3 compatibility
sed -i -r 's!opencv_core opencv_highgui opencv_imgproc!opencv_core opencv_highgui opencv_imgproc opencv_imgcodecs opencv_videoio!' Makefile
##### build and install
make -j$CORE_NUMBER
install -d $TOOL_DIR/caffe_c3d/lib
install -m755 build/lib/libcaffe.so $TOOL_DIR/caffe_c3d/lib/libc3d_overlap_loss.so
#### sample_rate
cd ../C3D_sample_rate
sed -i -r 's#^(CUDA_DIR := ).*$#\1/usr/local/cuda-8.0#' Makefile.config
sed -i -r 's#^(BLAS := ).*$#\1open#' Makefile.config
sed -i -r 's#/usr/local/include/python2\.7#/usr/include/python2.7#' Makefile.config
sed -i -r 's#/usr/local/lib/python2\.7#/usr/lib/python2.7#' Makefile.config
sed -i -r 's#^(DEBUG := ).*$#\10#' Makefile.config
sed -i -r 's#^(INCLUDE_DIRS := ).*$#\1$(PYTHON_INCLUDE) $(subst :, ,$(INCLUDE)) /usr/include/hdf5/serial#' Makefile.config
sed -i -r 's#^(LIBRARY_DIRS := ).*$#\1$(PYTHON_LIB) $(subst :, ,$(LIB)) /usr/lib/x86_64-linux-gnu/hdf5/serial#' Makefile.config
##### fix opencv 3 compatibility
sed -i -r 's!opencv_core opencv_highgui opencv_imgproc!opencv_core opencv_highgui opencv_imgproc opencv_imgcodecs opencv_videoio!' Makefile
##### build and install
make -j$CORE_NUMBER
install -d $TOOL_DIR/caffe_c3d/lib
install -m755 build/lib/libcaffe.so $TOOL_DIR/caffe_c3d/lib/libc3d_sample_rate.so
### restore normal caffe
mv $TOOL_DIR/caffe.skip $TOOL_DIR/caffe
source ~/.bashrc