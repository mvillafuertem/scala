# Packer

```shell

packer init . && \
packer fmt . && \
packer validate .

```

```shell

packer build bastion.pkr.hcl

```

# Terraform


_Si te interesa usar una versión especifica de terraform_

```shell

docker run --rm -it \
--name terraform \
--user=root \
-v `pwd`:/app \
-v ~/.aws/:/root/.aws/ \
-v ~/.ssh/:/root/.ssh \
--entrypoint=/bin/sh \
hashicorp/terraform:0.11.14

```


```shell

terraform init \
-backend=true \
-backend-config="bucket=terraform.dev" \
-backend-config="key=dev/terraform.tfstate" \
-backend-config="region=eu-west-1" \
-verify-plugins=false

```


```shell

terraform plan \
-var-file=../commons.tfvars \
-var-file=../dev.tfvars \
-out=dev.plan.out

```

Puede que tengas problemas al usar tus clave de ssh, prueba con estos comandos

```shell

eval `ssh-agent -s`

```
```shell

ssh-add /root/.ssh/id_rsa

```

# Ansible

_Instalar roles_

```shell

ansible-galaxy install \
--role-file ansible/requirements.yml \
--roles-path ansible/roles \
--force 

```

