STORM=/home/lvnguyen/apache-storm-1.0.2
ZOOKEEPER=/home/lvnguyen/zookeeper-3.4.8

TERMINAL=gnome-terminal
TRM_GEOMETRY='--geometry=50x10+0+0'

OPTION_COMMAND='--command'
OPTION_WORKING_DIR='--working-directory'

#zookeeper
${TERMINAL} ${TRM_GEOMETRY} ${OPTION_WORKING_DIR}=${ZOOKEEPER} ${OPTION_COMMAND}="bash -c './bin/zkServer.sh start'"

#STORM
${TERMINAL} ${TRM_GEOMETRY} ${OPTION_WORKING_DIR}=${STORM} ${OPTION_COMMAND}="bash -c './bin/storm nimbus'"
${TERMINAL} ${TRM_GEOMETRY} ${OPTION_WORKING_DIR}=${STORM} ${OPTION_COMMAND}="bash -c './bin/storm supervisor'"
${TERMINAL} ${TRM_GEOMETRY} ${OPTION_WORKING_DIR}=${STORM} ${OPTION_COMMAND}="bash -c './bin/storm ui'"
