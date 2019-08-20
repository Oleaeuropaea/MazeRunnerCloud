#!/bin/bash


# 1.SetupExecute following commands

# sudo yum update
# sudo yum install java-1.7.0-openjdk-devel.x86_64
# sudo yum install git
#git clone https://github.com/hfalves94/MazeRunner-Cloud.git

#sudo sed -i -e '$i /home/ec2-user/MazeRunner-Cloud/Script.sh \n' /etc/rc.local
# At the begining of the rc.local script put #|/bin/sh -e
# Allow script execution: chmod+x Script.sh (If needed)
# wget https://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip
# unzip aws-java-sdk.zip
# rm aws-java-sdk.zip
# Change version according to the version downloaded
awsVersion="aws-java-sdk-1.11.322"

# mkdir $HOME/.aws
# touch $HOME/.aws/credentials 
# Edit Credentials
# Edit System utils with region, ami-id,etc

## Project Structure
## -- aws-java-sdk-1.11.301
## -- MazeRunner-Cloud
##     |---BIT
##     |---pt.ulisboa.tecnico.meic.cnv mazerunner
##            |---instrumentation
##            |---maze
##            |---mazerunnercloud

# export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
# export CLASSPATH="$CLASSPATH:$(pwd)/$awsVersion/lib/$awsVersion.jar:$(pwd)/$awsVersion/third-party/lib/*:$(pwd)/MazeRunner-Cloud;"
# export CLASSPATH=$CLASSPATH:$(pwd)/MazeRunner-Cloud/bin:/.;


echo "Clean Project"
rm -rf bin

export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
export CLASSPATH="$CLASSPATH:$(pwd)/:$(pwd)/../$awsVersion/lib/$awsVersion.jar:$(pwd)/../$awsVersion/third-party/lib/*:$(pwd)/bin;"

mkdir bin > /dev/null 2>&1
echo "Compile...."
javac -d bin $(find . -name "*.java")

cp src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/*.maze bin
#cd bin

#Instrument code
#echo "Instrumenting...."

#java  pt/ulisboa/tecnico/meic/cnv/mazerunner/instrumentation/InstrumentMaze $(pwd)/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze $(pwd)/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/*
echo $CLASSPATH


#echo "Running Webserver"
#java   pt/ulisboa/tecnico/meic/cnv/mazerunner/mazerunnercloud/webserver/WebServerMain


echo "Lauching LoadBalancer (AUTOSCALER,Webserver)"
sudo java pt/ulisboa/tecnico/meic/cnv/mazerunner/mazerunnercloud/loadbalancer/LoadBalancerMain