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

variable "DB_USERNAME" {
  type    = string
  default = "swamyuser"
}

variable "DB_PASSWORD" {
  type    = string
  default = "admin"
}

variable "DB_NAME" {
  type    = string
  default = "webapp"
}

variable "DB_URL" {
  type    = string
  default = "jdbc:postgresql://localhost:5432/webapp"
}

source "amazon-ebs" "my-ami" {
  region          = "us-east-1"
  ami_name        = "Swamy_f24_app_${formatdate("YYYY_MM_DD", timestamp())}"
  ami_description = "AMI for CSYE6225 Cloud"
  instance_type   = "t2.small"
  source_ami      = "${var.source_ami}"
  ssh_username    = "${var.ssh_username}"
  subnet_id       = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
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

  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "echo 'Installing JDK-17'",
      "sudo apt-get install -y openjdk-17-jdk",
      "echo 'Installing PostgreSQL 16'",
      "sudo apt-get install -y postgresql-16",
      "echo 'Enabling and starting PostgreSQL'",
      "sudo systemctl enable postgresql",
      "sudo systemctl start postgresql",

      # Configure PostgreSQL
      "sudo -u postgres psql -c \"CREATE DATABASE ${var.DB_NAME};\"",
      "sudo -u postgres psql -c \"CREATE USER ${var.DB_USERNAME} WITH PASSWORD '${var.DB_PASSWORD}';\"",
      "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE ${var.DB_NAME} TO ${var.DB_USERNAME};\"",
      "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON SCHEMA public TO ${var.DB_USERNAME};\"",
      "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${var.DB_USERNAME};\"",
      "sudo -u postgres psql -c \"ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO ${var.DB_USERNAME};\"",



      # Clean up unnecessary files to reduce image size
      "sudo apt-get clean"
    ]
  }

  # Copy Spring Boot JAR file
  provisioner "file" {
    source      = var.jar_file
    destination = "/home/ubuntu/spring-boot-app.jar"
  }

  # Create a systemd service for Spring Boot with environment variables.
  provisioner "shell" {
    inline = [
      "echo '[Unit]' | sudo tee /etc/systemd/system/springbootapp.service",
      "echo 'Description=Spring Boot Application' | sudo tee -a /etc/systemd/system/springbootapp.service",

      "echo '[Service]' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'Environment=DB_URL=${var.DB_URL}' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'Environment=DB_USERNAME=${var.DB_USERNAME}' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'Environment=DB_PASSWORD=${var.DB_PASSWORD}' | sudo tee -a /etc/systemd/system/springbootapp.service",
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
