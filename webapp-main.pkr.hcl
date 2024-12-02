packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "vpc_id" {
  type    = string
  default = "vpc-0957dd325698a8933"
}

variable "source_ami" {
  type    = string
  default = "ami-0866a3c8686eaeeba"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = "subnet-00977bb91387b3651"
}

variable "jar_file" {
  type    = string
  default = "ROOT.jar"
}

// variable "DB_USERNAME" {
//   type = string
//   // default = "postgres"
// }

// variable "DB_PASSWORD" {
//   type = string
//   // default = "admin"
// }

// variable "DB_NAME" {
//   type = string
//   // default = "webapp"
// }

// variable "DB_URL" {
//   type = string
//   // default = "jdbc:postgresql://localhost:5432/webapp"
// }

source "amazon-ebs" "my-ami" {
  region          = "us-east-1"
  ami_name        = "Swamy_Webapp-${formatdate("YYYY_MM_DD-HHmmss", timestamp())}"
  ami_description = "AMI for CSYE6225 Cloud"
  instance_type   = "t2.small"
  source_ami      = "${var.source_ami}"
  ssh_username    = "${var.ssh_username}"
  subnet_id       = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
  }

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }
}

build {
  name    = "webapp-packer"
  sources = ["source.amazon-ebs.my-ami"]

  # First provisioner: Check if git is installed, and remove it if found
  provisioner "shell" {
    inline = [
      "if which git >/dev/null; then",
      "  echo 'Git is installed, removing it...'",
      "  sudo apt-get remove -y git",
      "  sudo apt-get autoremove -y",
      "  echo 'Git has been removed.'",
      "else",
      "  echo 'Git is not installed, proceeding.'",
      "fi"
    ]
  }

  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "echo 'Installing JDK-17'",
      "sudo apt-get install -y openjdk-17-jdk",

      "sudo apt-get update",
      "sudo apt-get install -y wget",
      "wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i -E ./amazon-cloudwatch-agent.deb",
      "sudo rm ./amazon-cloudwatch-agent.deb",

      # Create user csye6225 and group
      "echo 'csye6225 groud added'",
      "sudo groupadd csye6225",
      "echo 'csye6225 groud got added'",
      "echo 'csye6225 user added as nologin'",
      "sudo useradd -r -g csye6225 -s /usr/sbin/nologin csye6225",

      # Clean up unnecessary files to reduce image size
      "sudo apt-get clean"
    ]
  }

  # Configure CloudWatch Agent with JSON settings
  provisioner "shell" {
    inline = [
      "cat <<EOT | sudo tee /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json > /dev/null",
      "{",
      "  \"agent\": {",
      "    \"run_as_user\": \"root\"",
      "  },",
      "  \"logs\": {",
      "    \"logs_collected\": {",
      "      \"files\": {",
      "        \"collect_list\": [",
      "          {",
      "            \"file_path\": \"/var/log/syslog\",",
      "            \"log_group_name\": \"csye6225-webapp-logs\",",
      "            \"log_stream_name\": \"{instance_id}-syslog\"",
      "          },",
      "          {",
      "            \"file_path\": \"/var/log/springboot-app.log\",",
      "            \"log_group_name\": \"csye6225-webapp-logs\",",
      "            \"log_stream_name\": \"{instance_id}-application\"",
      "          }",
      "        ]",
      "      }",
      "    }",
      "  },",
      "  \"metrics\": {",
      "    \"namespace\": \"CustomMetrics\",",
      "    \"metrics_collected\": {",
      "      \"disk\": {",
      "        \"measurement\": [\"used_percent\"],",
      "        \"metrics_collection_interval\": 60",
      "      },",
      "      \"mem\": {",
      "        \"measurement\": [\"mem_used_percent\"],",
      "        \"metrics_collection_interval\": 60",
      "      },",
      "      \"statsd\": {",
      "        \"service_address\": \":8125\",",
      "        \"metrics_collection_interval\": 60,",
      "        \"metrics_aggregation_interval\": 60",
      "      }",
      "    }",
      "  }",
      "}",
      "EOT",
      "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s"
    ]
  }

  # Copy Spring Boot JAR file
  provisioner "file" {
    source      = var.jar_file
    destination = "/home/ubuntu/spring-boot-app.jar"
  }

  # Set permissions on the JAR file
  # Check if user exists, then update ownership
  provisioner "shell" {
    inline = [
      "if id -u csye6225 >/dev/null 2>&1; then",
      "  echo \"User 'csye6225' exists, updating application permissions....\"",
      "  sudo chown -R csye6225:csye6225 /home/ubuntu/spring-boot-app.jar",
      "  echo \"Setting permissions so only csye6225 user and group can access the JAR file...\"",
      "  sudo chmod 740 /home/ubuntu/spring-boot-app.jar", # Permissions: User (read, write, execute), Group (read-only), Others (no access)
      "else",
      "  echo \"User doesn't exist, ownership cannot be updated...exiting\"",
      "  exit 1",
      "fi"
    ]
  }

  # Check if git is installed
  provisioner "shell" {
    inline = [
      "if which git >/dev/null; then",
      "  echo 'Error: git is installed in the AMI.'",
      "  exit 1",
      "else",
      "  echo 'git is not installed, proceeding.'",
      "fi"
    ]
  }

  # Create a systemd service for Spring Boot with environment variables.
  provisioner "shell" {
    inline = [
      "echo '[Unit]' | sudo tee /etc/systemd/system/springbootapp.service",
      "echo 'Description=Spring Boot Application' | sudo tee -a /etc/systemd/system/springbootapp.service",

      "echo '[Service]' | sudo tee -a /etc/systemd/system/springbootapp.service",
      # Log the environment variables to a file for validation
      "echo 'EnvironmentFile=/etc/environment' | sudo tee -a /etc/systemd/system/springbootapp.service", # Load env variables from /etc/environment
      "echo 'ExecStartPre=/bin/bash -c \"env > /var/log/springboot-env.log\"' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'ExecStart=/usr/bin/java -jar /home/ubuntu/spring-boot-app.jar' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'Restart=always' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo '[Install]' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/springbootapp.service",

      # Enable and start the Spring Boot service
      "sudo systemctl enable springbootapp",
      "sudo systemctl start springbootapp"
    ]
  }
}
