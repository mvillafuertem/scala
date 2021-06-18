source "amazon-ebs" "ami_source_definition" {
  ami_name      = "bastion"
  instance_type = "t2.micro"
  region        = "eu-west-1"
  source_ami    = "ami-02ace471"
  ssh_username  = "ec2-user"
}

build {
  sources = [
    "source.amazon-ebs.ami_source_definition"
  ]
  provisioner "ansible" {
    playbook_file = "./playbook.yml"
  }
}

