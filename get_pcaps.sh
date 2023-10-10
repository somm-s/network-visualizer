#!/bin/bash

# Set hardcoded remote machine address
remote_machine="10.10.0.192"

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <remote_path_on_remote_machine> <local_destination_path>"
    exit 1
fi

# Set variables for remote and local paths
remote_path=$1
local_destination=$2

# Copy the file from the remote machine to the local machine
scp -i /home/lab/.ssh/id_rsa sosi@$remote_machine:/media/sosi/490d065d-ed08-4c6e-abd4-184715f06052/2022/BT03-CHE/pcaps/$remote_path $local_destination

# Check the return status of SCP command
if [ $? -eq 0 ]; then
    echo "File successfully copied from remote machine to local machine."
else
    echo "Error copying file from remote machine to local machine."
fi