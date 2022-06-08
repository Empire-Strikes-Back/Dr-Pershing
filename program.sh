#!/bin/bash


install(){
  npm i --no-package-lock
}

copy(){
  mkdir -p out/jar/ui/
  mkdir -p out/jar/src/Dr_Pershing/
  cp src/Dr_Pershing/index.html out/jar/ui/index.html
  cp src/Dr_Pershing/style.css out/jar/ui/style.css
  cp src/Dr_Pershing/schema.edn out/jar/src/Dr_Pershing/schema.edn
  cp package.json out/jar/package.json

  convert out/identicon/icon.png -define icon:auto-resize=256,64,48,32,16 out/identicon/icon.ico
  png2icns out/identicon/icon.icns out/identicon/icon.png
  cp out/identicon/icon.png out/jar/icon.png
  cp out/identicon/icon.icns out/jar/icon.icns
  cp out/identicon/icon.ico out/jar/icon.ico
}

shadow(){
  clj -A:shadow:main:ui -M -m shadow.cljs.devtools.cli "$@"
}

repl(){
  install
  copy
  shadow clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

identicon(){
  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Dr-Pershing"' \
    :filename '"out/identicon/icon.png"' \
    :size 256
}

compile(){
  shadow release :ui :main
}

out(){
  rm -rf out

  install
  identicon
  copy
}

package(){

  
  # rm -rf out/zip

  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  ln -s ../../node_modules out/jar/node_modules
  npx electron-packager out/jar \
    "Dr-Pershing-$COMMIT_COUNT-$COMMIT_HASH" \
    --overwrite \
    --asar \
    --executable-name=Dr-Pershing \
    --app-name=Dr-Pershing \
    --build-version="$COMMIT_COUNT-$COMMIT_HASH" \
    --icon=out/identicon/icon.png \
    --out=out/zip \
    --platform=darwin,linux,win32 \
    --arch=x64
}

zip_files(){
  rm -rf out/zip/*.zip
  for dir in out/zip/*; do
    echo $dir
    cd $dir
    zip -qr ../../../$dir ./
    cd ../../../
  done
}

release(){
  out
  compile
  package
  zip_files
}

tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  TAG="$COMMIT_COUNT-$COMMIT_HASH"
  git tag $TAG $COMMIT_HASH
  echo $COMMIT_HASH
  echo $TAG
}

"$@"