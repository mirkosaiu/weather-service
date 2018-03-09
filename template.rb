CloudFormation {
  AWSTemplateFormatVersion "2010-09-09"

  Description "Babysteps example consisting of 2 EC2 instances behind an ELB"
 
  templateName = "WeatherStackTemplate"

  numberOfInstances = 2




  Parameter("InstanceType") {
    Description "Type of EC2 instance to launch"
    Type "String"
    Default "t2.micro"
  }
  Parameter("KeyName") {
    Description "Name of an existing EC2 key pair to enable SSH access to the new EC2 instance"
    Type "String"
    Default "blogdemo"
  }
  Parameter("SshIp") {
    Description "IP address that should have direct access through SSH"
    Type "String"
    Default "10.42.0.0/24"
  }







  Mapping("AWSRegionArch2AMI", {
            "us-east-1" => { "AMI" => "ami-864d84ee" }
  })
  Mapping("SubnetConfig", {
      "VPC"     => { "CIDR" => "10.42.0.0/16" },
      "Public"  => { "CIDR" => "10.42.0.0/24" }
  })





  Resource("BabyVPC") {
    Type "AWS::EC2::VPC"
    Property("CidrBlock", FnFindInMap("SubnetConfig", "VPC", "CIDR"))
  }
  Resource("PublicSubnet") {
    Type "AWS::EC2::Subnet"
    Property("VpcId", Ref("BabyVPC"))
    Property("CidrBlock", FnFindInMap("SubnetConfig", "Public","CIDR"))
  }
  Resource("InternetGateway") {
      Type "AWS::EC2::InternetGateway"
  }
  Resource("AttachGateway") {
       Type "AWS::EC2::VPCGatewayAttachment"
       Property("VpcId", Ref("BabyVPC"))
       Property("InternetGatewayId", Ref("InternetGateway"))
  }
  Resource("PublicRouteTable") {
    Type "AWS::EC2::RouteTable"
    Property("VpcId", Ref("BabyVPC"))
  }
  Resource("PublicRoute") {
    Type "AWS::EC2::Route"
    DependsOn "AttachGateway"
    Property("RouteTableId", Ref("PublicRouteTable"))
    Property("DestinationCidrBlock", "0.0.0.0/0")
    Property("GatewayId", Ref("InternetGateway"))
  }
  Resource("PublicSubnetRouteTableAssociation") {
    Type "AWS::EC2::SubnetRouteTableAssociation"
    Property("SubnetId", Ref("PublicSubnet"))
    Property("RouteTableId", Ref("PublicRouteTable"))
  }




  ec2SecurityIngres = Array.new

  ec2SecurityIngres.push({
    "IpProtocol" => "tcp",
    "FromPort" => "80",
    "ToPort" => "80",
    "SourceSecurityGroupId" => Ref("ELBSecurityGroup")
  })

  ec2SecurityIngres.push({
    "IpProtocol" => "tcp",
    "FromPort" => "22",
    "ToPort" => "22",
    "CidrIp" => Ref("SshIp")
  })

  port80Open = [{
                  "IpProtocol" => "tcp",
                  "FromPort" => "80",
                  "ToPort" => "80",
                  "CidrIp" => "0.0.0.0/0"
                }]


  Resource("InstanceSecurityGroup") {
    Type "AWS::EC2::SecurityGroup"
    Property("Tags", [{"Key" => "Name", "Value" => "Babysteps EC2"}])
    Property("VpcId", Ref("BabyVPC"))
    Property("GroupDescription" , templateName + " - EC2 instances: HTTP and SSH access")
    Property("SecurityGroupIngress", ec2SecurityIngres)
  }
  Resource("ELBSecurityGroup") {
    Type "AWS::EC2::SecurityGroup"
    Property("Tags", [{"Key" => "Name", "Value" => "Babysteps ELB"}])
    Property("VpcId", Ref("BabyVPC"))
    Property("GroupDescription" , templateName + " - ELB: HTTP access")
    Property("SecurityGroupIngress", port80Open)
    Property("SecurityGroupEgress", port80Open)
  }






  babystepsServerRefs = Array.new

  (1..numberOfInstances).each do |instanceNumber|
    instanceName = "Babysteps#{instanceNumber}"
    Resource(instanceName) {
      Type "AWS::EC2::Instance"
      Property("SubnetId", Ref("PublicSubnet"))
      Property("Tags", [{"Key" => "Name", "Value" => "#{templateName}-#{instanceNumber}"}])
      Property("ImageId", FnFindInMap( "AWSRegionArch2AMI", Ref("AWS::Region"),"AMI"))
      Property("InstanceType", Ref("InstanceType"))
      Property("KeyName", Ref("KeyName"))
      Property("SecurityGroupIds", [Ref("InstanceSecurityGroup")])
      Property("UserData", {
                          "Fn::Base64" =>
                            FnJoin("\n",[
                              "#!/bin/bash",
                              "apt-get install -y apache2"
                              ]
                          )})
    }

    Resource ("BabyIP#{instanceNumber}") {
      Type "AWS::EC2::EIP";
      Property("Domain", "vpc")
      Property("InstanceId", Ref(instanceName))
    }

    babystepsServerRefs.push(Ref(instanceName))

    Output("#{instanceName}IpAddress") {
      Value FnGetAtt(instanceName, "PublicIp")
    }
  end









  Resource("BabystepsLoadBalancer") {
    Type "AWS::ElasticLoadBalancing::LoadBalancer"
    Property("Subnets", [Ref("PublicSubnet")])
    Property("SecurityGroups", [Ref("ELBSecurityGroup")])
    Property("Listeners" , [{
                                "LoadBalancerPort" => "80",
                                "InstancePort" => "80",
                                "Protocol" => "HTTP"
                              }])
    Property("HealthCheck" , {
                "Target" => "HTTP:80/index.html",
                "HealthyThreshold" => "3",
                "UnhealthyThreshold" => "5",
                "Interval" => "30",
                "Timeout" => "5"
              })
    Property("Instances", babystepsServerRefs)
  }








}