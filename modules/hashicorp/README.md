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


Si te interesa usar una versión especifica de terraform

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