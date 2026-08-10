# Intentional IaC misconfigurations for Checkov / tfsec / kics
# (Security Code + Compliance Code sheets)

terraform {
  required_version = ">= 1.0"
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_security_group" "open_to_world" {
  name        = "testable-open-sg"
  description = "Intentionally open security group for scanner findings"

  ingress {
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_s3_bucket" "public_bucket" {
  bucket = "testable-sample-public-bucket-do-not-use"
  acl    = "public-read"
}

resource "aws_db_instance" "unencrypted" {
  identifier          = "testable-unencrypted-db"
  engine              = "mysql"
  instance_class      = "db.t3.micro"
  allocated_storage   = 20
  username            = "admin"
  password            = "HardcodedDbPassword123!"
  skip_final_snapshot = true
  storage_encrypted   = false
  publicly_accessible = true
}
