#!/bin/bash

create_large_file() {
    filename=$1
    size_gb=$2
    # 1 GB = 1024 * 1024 * 1024 bytes
    size_bytes=$((size_gb * 1024 * 1024 * 1024))

    # Create the file with the desired size using dd command
    dd if=/dev/zero of=$filename bs=1G count=$size_gb &> /dev/null
}

# Main script
filename1="large_file.dat"
# filename2="large_file2.dat"
size_gb=10  # Change this to the desired size in gigabytes
create_large_file $filename1 $size_gb
echo "Large file '$filename1' created with size $size_gb GB."

# create_large_file $filename2 $size_gb
# echo "Large file '$filename2' created with size $size_gb GB."
