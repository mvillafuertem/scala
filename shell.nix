{ java ? "jdk17_headless" }:

let
  jdk = pkgs.${java};

  config.packageOverrides = pkgs: rec {
    sbt = pkgs.sbt.override { jre = jdk; };
    metals = pkgs.metals.override { jre = jdk; };
  };

  pkgs = import <nixpkgs> { inherit config; };

in pkgs.mkShell {

  buildInputs = [ jdk pkgs.sbt pkgs.grpcurl pkgs.metals pkgs.figlet ];

  shellHook = ''
    [ ! -f /tmp/figlet/Shadow.flf ] &&\
    mkdir -p /tmp/figlet &&\
    curl -L https://raw.githubusercontent.com/xero/figlet-fonts/master/ANSI%20Shadow.flf > /tmp/figlet/Shadow.flf
    echo -e "\033[36m$(figlet -w 130 -f "/tmp/figlet/Shadow.flf" "events-manager")\033[0m"
    echo "remember add useGlobalExecutable in metals"
  '';

}
