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
  //64 bit (x86) ami-0325498274077fac5
  //64 bit (ARM) ami-0fff1b9a61dec8a5f
  // ami-0866a3c8686eaeeba
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
  default = "./build/libs/webapp-0.0.1-SNAPSHOT.jar" # Adjust this if you know the exact JAR filename
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

source "amazon-ebs" "my-ami" {
  region          = "us-east-1"
  ami_name        = "Swamy_f24_app_${formatdate("YYYY_MM_DD", timestamp())}"
  ami_description = "AMI for CSYE6225 Cloud "
  instance_type   = "t2.small"
  // source_ami_filter {
  //   filters = {
  //     name                = "ubuntu/images/*ubuntu-jammy-22.04-amd64-server-*"
  //     root-device-type    = "ebs"
  //     virtualization-type = "hvm"
  //   }
  //   most_recent = true
  //   owners      = ["099720109477"]
  // }
  source_ami   = "${var.source_ami}"
  ssh_username = "${var.ssh_username}"
  subnet_id    = "${var.subnet_id}"

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
  name = "webapp-packer"
  sources = [
    "source.amazon-ebs.my-ami"
  ]

  provisioner "shell" {
    // environment_vars = [
    //   "DB_URL={{user `db_url`}}",
    //   "DB_USERNAME={{user `db_username`}}",
    //   "DB_PASSWORD={{user `db_password`}}",
    //   "DB_NAME={{user `db_name`}}"
    // ]
    inline = [
      "sudo apt-get update",

      "echo 'Installing JDK-17'",
      "sudo apt-get install -y openjdk-17-jdk",

      "echo 'Installing Postgresql 16'",
      "sudo apt-get install -y postgresql-16",

      "echo 'Installing Gradle'",
      "sudo apt-get install -y gradle",

      "echo 'Enabling Postgres'",
      "sudo systemctl enable postgresql",

      "echo 'Starting Postgres'",
      "sudo systemctl start postgresql",

      # Configure PostgreSQL
      "sudo -u postgres psql -c \"CREATE DATABASE ${var.DB_NAME};\"",
      "sudo -u postgres psql -c \"CREATE USER ${var.DB_USERNAME} WITH PASSWORD '${var.DB_PASSWORD}';\"",
      "sudo -u postgres psql -c \"ALTER USER ${var.DB_USERNAME} WITH PASSWORD '${var.DB_PASSWORD}';\"",
      "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE ${var.DB_NAME} TO ${var.DB_USERNAME};\"",
      "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON SCHEMA public TO ${var.DB_USERNAME};\"",
      //       "sudo -u postgres psql -c \"\"",
      //       "sudo -u postgres psql -c \"CREATE USER ${var.DB_USERNAME} WITH PASSWORD '${var.DB_PASSWORD}';\"",
      //       "sudo -u postgres psql -c \"CREATE DATABASE ${var.DB_NAME} OWNER ${var.DB_USERNAME};\"",
      //       "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE ${var.DB_NAME} TO ${var.DB_USERNAME};\"",
      //       "sudo -u postgres psql -c \"FLUSH PRIVILEGES;\"",
      //       CREATE DATABASE webapp;
      // CREATE USER postgres WITH PASSWORD 'admin';
      // ALTER USER postgres WITH PASSWORD 'admin';
      // GRANT ALL PRIVILEGES ON DATABASE webapp TO postgres;
      // GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;

      // "echo 'Installing tomcat9'",
      // "sudo apt-get install -y tomcat9",
      // "sudo systemctl enable tomcat9",
      // "sudo systemctl start tomcat9",
      // "echo 'Installing tomcat9 Completed'",

      # Clean up unnecessary files to reduce image size
      "sudo apt-get clean",
      // "sleep 30",
      // "sudo apt-get update",
      // "sudo apt-get upgrade -y",
      // "sudo apt-get install nginx -y",
      // "sudo apt-get clean",
    ]
  }

  // provisioner "shell" {
  //   inline = [
  //     "echo 'Current working directory:' && pwd", # Print current directory
  //     "echo 'Listing all files:' && ls -la",
  //     "ls -la build/libs", # Check contents of the build/libs directory
  //     "if [ ! -f build/libs/webapp-0.0.1-SNAPSHOT.jar ]; then echo 'JAR file not found! Exiting.'; exit 1; fi"
  //   ]
  // }

  // provisioner "shell" {
  //   inline = [
  //     "echo 'Current working directory:' && pwd",
  //     "echo 'Listing all files:' && ls -la",
  //     "echo 'Checking JAR file:' && ls -la /home/ubuntu/webapp-0.0.1-SNAPSHOT.jar"
  //   ]
  // }

  // provisioner "file" {
  //   source      = var.jar_file
  //   destination = "/home/ubuntu/webapp-0.0.1-SNAPSHOT.jar"
  // }

  provisioner "file" {
    source      = var.jar_file # Ensure the file name is correct or use a wildcard
    destination = "/tmp/ROOT.jar"
  }

  // provisioner "shell" {
  //   inline = [
  //     "sudo mv /tmp/ROOT.jar /var/lib/tomcat9/webapps/ROOT.jar",
  //     "sudo chown tomcat:tomcat /var/lib/tomcat9/webapps/ROOT.jar",
  //     "sudo chmod 644 /var/lib/tomcat9/webapps/ROOT.jar",
  //     "sudo systemctl restart tomcat9"
  //   ]
  // }
  provisioner "shell" {
    inline = [
      "sudo mkdir -p /var/lib/tomcat9/webapps",
      "sudo mv /tmp/ROOT.jar /var/lib/tomcat9/webapps/ROOT.jar",
      // "sudo chown tomcat:tomcat /var/lib/tomcat9/webapps/ROOT.jar",
      // "sudo chmod 644 /var/lib/tomcat9/webapps/ROOT.jar"
    ]
  }
  provisioner "shell" {
    scripts = [
      "./scripts/noLoginUser.sh",
      "./scripts/UpdateApplicationOwnership.sh"
    ]
    inline = [
      // "sudo chmod 644 /var/lib/tomcat9/webapps/ROOT.jar"
    ]
  }

  # Create a systemd service to run Spring Boot at startup
  provisioner "shell" {
    inline = [
      "echo '[Unit]' | sudo tee /etc/systemd/system/springbootapp.service",
      "echo 'Description=Spring Boot Application' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo '[Service]' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'ExecStart=/usr/bin/java -jar /home/ubuntu/spring-boot-app.jar' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'Restart=always' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo '[Install]' | sudo tee -a /etc/systemd/system/springbootapp.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/springbootapp.service",

      # Enable and start the Spring Boot service
      "sudo systemctl enable springbootapp",
      "sudo systemctl start springbootapp",
    ]
  }
  // provisioner "shell" {
  //   inline = [
  //     "echo '[Unit]' | sudo tee /etc/systemd/system/springbootapp.service",
  //     "echo 'Description=Spring Boot Application' | sudo tee -a /etc/systemd/system/springbootapp.service",
  //     "echo '[Service]' | sudo tee -a /etc/systemd/system/springbootapp.service",
  //     "echo 'ExecStart=/usr/bin/java -jar /var/lib/tomcat9/webapps/ROOT.jar' | sudo tee -a /etc/systemd/system/springbootapp.service",
  //     "echo 'Restart=always' | sudo tee -a /etc/systemd/system/springbootapp.service",
  //     "echo '[Install]' | sudo tee -a /etc/systemd/system/springbootapp.service",
  //     "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/springbootapp.service",
  //     "sudo systemctl enable springbootapp",
  //     "sudo systemctl start springbootapp"
  //   ]
  // }
}
