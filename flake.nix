{
  description = "Flake for Scala Development";

  outputs = { nixpkgs, ... }:
    let
      system = "aarch64-darwin";
      java = "openjdk11";
      jdk = pkgs.${java};
      pkgs = import nixpkgs {
        inherit system;
        overlays = [
          (final: prev: {
            # sbt = prev.sbt.overrideAttrs { postPatch = ''
            sbt = prev.sbt.overrideAttrs {
              patchPhase = ''
                echo -java-home ${jdk} >> conf/sbtopts
              '';
            };
          })
        ];
      };
    in {

      devShells.${system}.default =
        pkgs.mkShell { buildInputs = [ jdk pkgs.sbt pkgs.metals ]; };
    };
}
