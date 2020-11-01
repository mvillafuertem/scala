package io.github.mvillafuertem.terraform.cdktf

import typings.cdktf.mod.TerraformStack
import typings.cdktfProviderAws.awsProviderMod.AwsProviderConfig
import typings.cdktfProviderAws.instanceMod.InstanceConfig
import typings.cdktfProviderAws.mod.{AwsProvider, Instance}
import typings.constructs.mod.Construct

final class MyStack(scope: Construct, name: String) extends TerraformStack(scope, name) { self: Construct =>
  new AwsProvider(self, "aws", AwsProviderConfig("us-east-1"))
  new Instance(self, "Hello", InstanceConfig("ami-2757f631", "t2.micro"))
}
