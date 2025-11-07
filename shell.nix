{
  pkgs ? import <nixpkgs> { },
}:

pkgs.mkShell {
  packages = with pkgs; [
    jetbrains.jdk
    gradle
  ];
  shellHook = ''
    export LD_LIBRARY_PATH=${
      pkgs.lib.makeLibraryPath [
        pkgs.glib
        pkgs.xorg.libXtst
        pkgs.xorg.libXxf86vm
        pkgs.libGL
      ]
    }
    export GIO_EXTRA_MODULES=${pkgs.dconf}/lib/gio/modules
    export XDG_DATA_DIRS=${pkgs.gsettings-desktop-schemas}/share/gsettings-schemas/${pkgs.gsettings-desktop-schemas.name}:${pkgs.gtk3}/share/gsettings-schemas/${pkgs.gtk3.name}:$XDG_DATA_DIRS
  '';
}
