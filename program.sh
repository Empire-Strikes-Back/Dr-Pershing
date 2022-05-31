#!/bin/bash


install(){
  npm i --no-package-lock
  mkdir -p out/jar/ui/
  mkdir -p out/jar/src/Dr-Pershing/
  cp src/Dr_Pershing/index.html out/jar/ui/index.html
  cp src/Dr_Pershing/style.css out/jar/ui/style.css
  cp src/Dr_Pershing/schema.edn out/jar/src/Dr-Pershing/schema.edn
  cp package.json out/jar/package.json
}

shadow(){
  clj -A:shadow:main:ui -M -m shadow.cljs.devtools.cli "$@"
}

repl(){
  install
  shadow clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

jar(){

  rm -rf out

  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Dr-Pershing"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  install
  cp out/identicon/icon.png out/jar/icon.png
  shadow release :main :ui
  # COMMIT_HASH=$(git rev-parse --short HEAD)
  # COMMIT_COUNT=$(git rev-list --count HEAD)
  # echo Dr-Pershing-$COMMIT_COUNT-$COMMIT_HASH.zip
  # cd out/jar
  # zip -r ../Dr-Pershing-$COMMIT_COUNT-$COMMIT_HASH.zip ./ && \
  # cd ../../
}

release(){
  jar
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