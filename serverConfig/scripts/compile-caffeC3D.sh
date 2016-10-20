#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/helpers.sh"

## C3D Caffe
subinfo "Installing C3D Caffe..."
cd $BUILD_DIR
### dependencies
sudo apt-get install -y libleveldb-dev libsnappy-dev
### skip normal caffe
moved_caffe=
if [ -d "$TOOL_DIR" ]; then
    moved_caffe=true
    mv $TOOL_DIR/caffe $TOOL_DIR/caffe.skip
    source ~/.bashrc
fi
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
sed -i -r 's#^(CUDA_DIR := ).*$#\1/usr/local/cuda#' Makefile.config
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
sed -i -r 's#^(CUDA_DIR := ).*$#\1/usr/local/cuda#' Makefile.config
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
if [ "${moved_caffe}x" != "x" ]; then
    mv $TOOL_DIR/caffe.skip $TOOL_DIR/caffe
fi
source ~/.bashrc
