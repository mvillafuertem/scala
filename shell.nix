{ java ? "jdk17_headless" }:

let
  jdk = pkgs.${java};

  config = {
    packageOverrides = p: rec {
      sbt = p.sbt.overrideAttrs (old: rec {
        patchPhase = ''
          echo -java-home ${jdk} >> conf/sbtopts
        '';
      });
    };
  };

  pkgs = import <nixpkgs> { inherit config; };

in pkgs.mkShell {

  buildInputs = [ jdk pkgs.sbt pkgs.grpcurl pkgs.metals pkgs.figlet ];

  shellHook = ''
    [ ! -f /tmp/figlet/Shadow.flf ] &&\
    mkdir -p /tmp/figlet &&\
    curl -L https://raw.githubusercontent.com/xero/figlet-fonts/master/ANSI%20Shadow.flf > /tmp/figlet/Shadow.flf
    echo -e "\033[36m$(figlet -f "/tmp/figlet/Shadow.flf" "scala")\033[0m"
    mkdir -p ~/.cache/nvim/nvim-metals  # Asegúrate de que la carpeta existe
    ln -snf ${pkgs.metals}/bin/metals ~/.cache/nvim/nvim-metals/metals  # Crea el enlace simbólico
    echo "Enlace simbólico creado: ~/.cache/nvim/nvim-metals/metals"
  '';

}
