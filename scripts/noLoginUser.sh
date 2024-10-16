#!/bin/bash

# Check if user already exists
if id -u csye6225 >/dev/null 2>&1; then
  echo "User 'csye6225' already exists. Exiting..."
  exit 0
fi

# Create the user with the nologin shell and primary group
sudo adduser csye6225 --shell /usr/sbin/nologin

echo "User 'csye6225' created successfully."
echo "User details: "
id csye6225
cat /etc/passwd | grep csye6225
