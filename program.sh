#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=8 \
    -X:Ripley Ripley.core/process \
    :main-ns Dr-Pershing.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m Dr-Pershing.main
}

tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  git tag "$COMMIT_COUNT-$COMMIT_HASH" $COMMIT_HASH
  git tag -l
}

jar(){

  rm -rf out/*.jar
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -X:Genie Genie.core/process \
    :main-ns Dr-Pershing.main \
    :filename "\"out/Dr-Pershing-$COMMIT_COUNT-$COMMIT_HASH.jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"