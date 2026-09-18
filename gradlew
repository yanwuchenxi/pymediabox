#!/bin/sh
# Gradle wrapper start-up script (POSIX). Auto-generated for CI.
APP_HOME="$(cd "$(dirname "$0")" && pwd)"

# 解析 distributionUrl
DIST_URL=""
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.properties" ]; then
  DIST_URL=$(grep "^distributionUrl=" "$APP_HOME/gradle/wrapper/gradle-wrapper.properties" | cut -d= -f2 | sed 's/\\:/:/g')
fi

# 下载并缓存
DIST_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists"
DIST_NAME=$(echo "$DIST_URL" | sed -E 's|.*/(gradle-[^-]+-bin\.zip)$|\1|')
DIST_VER=$(echo "$DIST_NAME" | sed -E 's/gradle-(.+)-bin\.zip/\1/')
DIST_HOME="$DIST_DIR/$DIST_NAME/dists-extracted/gradle-$DIST_VER"

if [ ! -d "$DIST_HOME" ] || [ ! -f "$DIST_HOME/bin/gradle" ]; then
  echo "Downloading Gradle $DIST_VER ..."
  mkdir -p "$DIST_DIR/$DIST_NAME/dists-extracted"
  TMP="$DIST_DIR/$DIST_NAME/gradle-$DIST_VER-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -L -f -o "$TMP" "$DIST_URL"
  else
    wget -O "$TMP" "$DIST_URL"
  fi
  # 解压：支持 unzip / busybox
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$TMP" -d "$DIST_DIR/$DIST_NAME/dists-extracted"
  else
    python3 -c "import zipfile;zipfile.ZipFile('$TMP').extractall('$DIST_DIR/$DIST_NAME/dists-extracted')"
  fi
fi

exec "$DIST_HOME/bin/gradle" "$@"
