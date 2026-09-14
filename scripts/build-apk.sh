#!/usr/bin/env bash
# ============================================================================
# Moxie — 零 Gradle 的 APK 构建脚本
#
# 用法：
#   bash scripts/build-apk.sh yuwen      # 只构建「语文默写」
#   bash scripts/build-apk.sh english    # 只构建「英语默写」
#   bash scripts/build-apk.sh both       # 两个都构建（默认）
#
# 依赖（Debian/Ubuntu）：
#   openjdk-17（或 21）+ Android SDK 的 build-tools 与任一 platforms/android-*
#   CI 里由 .github/workflows/build-apk.yml 自动安装，无需手动处理
#
# 构建链：javac -> dx/d8 -> aapt package -> 注入 classes.dex -> zipalign -> apksigner
#
# 签名（重要）：
#   本地：keystore 放仓库根 .secrets/ 下（已被 .gitignore 忽略）
#   CI  ：通过仓库 Secrets 注入：
#     YW_KEYSTORE_B64 / YW_KEYSTORE_PASS   语文默写签名密钥（base64）
#     EN_KEYSTORE_B64 / EN_KEYSTORE_PASS   英语默写签名密钥（base64）
#   注意：换签名密钥会导致老用户无法覆盖升级，只能卸载重装，请妥善保管。
# ============================================================================

APP="${1:-both}"
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
[ -z "$SDK" ] && SDK=/usr/lib/android-sdk
SECRETS_DIR="$REPO_ROOT/.secrets"
DIST="$REPO_ROOT/dist"

# ---- 自动定位 build-tools / android.jar（不把版本号写死）----
BT=""
for c in "$SDK/build-tools/29.0.3" "$SDK/build-tools/34.0.0" "$SDK/build-tools/33.0.0"; do
  [ -d "$c" ] && BT="$c" && break
done
[ -z "$BT" ] && BT="$(ls -d "$SDK"/build-tools/*/ 2>/dev/null | sort -V | tail -1)"
AJ="$SDK/platforms/android-23/android.jar"
[ -f "$AJ" ] || AJ="$(ls -d "$SDK"/platforms/android-*/ 2>/dev/null | sort -V | tail -1)android.jar"

echo "[env] ANDROID SDK = $SDK"
echo "[env] build-tools = $BT"
echo "[env] android.jar = $AJ"
if [ ! -d "$BT" ] || [ ! -f "$AJ" ]; then
  echo "!! 找不到 Android SDK（build-tools / platforms）。"
  echo "   本地：apt install android-sdk-build-tools android-sdk-platform-23，或设置 ANDROID_HOME。"
  exit 1
fi

mkdir -p "$SECRETS_DIR" "$DIST"

# 本地签名口令从被 git 忽略的 .secrets/keystore.env 读取（CI 用 Secrets 注入同名变量）
if [ -f "$SECRETS_DIR/keystore.env" ]; then
  # shellcheck disable=SC1091
  . "$SECRETS_DIR/keystore.env"
fi

build_one() {
  key="$1"
  case "$key" in
    yuwen)
      src_dir="$REPO_ROOT/android/yuwen-moxie/src/main"; java_root="$src_dir/java"
      web_dir="yuwen-moxie"; label="语文默写"; class_pkg="com/yuwen/moxie"
      ks_file="$SECRETS_DIR/yw.keystore"; ks_alias="yuwen"
      ks_pass="${YW_KEYSTORE_PASS:?签名口令未提供，见 .secrets/keystore.env 或 CI Secret}"; artifact="moxie-yuwen" ;;
    english)
      src_dir="$REPO_ROOT/android/english-moxie/src/main"; java_root="$src_dir/src"
      web_dir="english-moxie"; label="英语默写"; class_pkg="com/english/word"
      ks_file="$SECRETS_DIR/en.keystore"; ks_alias="englishword"
      ks_pass="${EN_KEYSTORE_PASS:?签名口令未提供，见 .secrets/keystore.env 或 CI Secret}"; artifact="moxie-english" ;;
    *) echo "!! 未知目标: $key"; return 1 ;;
  esac

  # CI：从 Secret 还原 keystore（变量名 YW_KEYSTORE_B64 / EN_KEYSTORE_B64）
  b64var="$(echo "$key" | tr 'a-z' 'A-Z')_KEYSTORE_B64"
  b64val="$(eval echo \\$b64var)"
  if [ ! -f "$ks_file" ] && [ -n "$b64val" ]; then
    printf %s "$b64val" | base64 -d > "$ks_file"
    echo "   已从 Secret 还原签名密钥"
  fi

  work="$REPO_ROOT/.build/$key"
  rm -rf "$work"; mkdir -p "$work/obj" "$work/gen"
  [ -f "$src_dir/AndroidManifest.xml" ] || { echo "!! 缺 AndroidManifest: $src_dir"; return 1; }

  # 单一数据源：用 web/ 下最新 HTML 覆盖 APK 内置副本
  if [ -f "$REPO_ROOT/web/$web_dir/index.html" ]; then
    mkdir -p "$src_dir/assets"
    cp "$REPO_ROOT/web/$web_dir/index.html" "$src_dir/assets/index.html"
  fi

  echo "==> [$key] 1/6 javac"
  find "$java_root" -name '*.java' > "$work/srcs.txt"
  javac -nowarn -source 1.8 -target 1.8 -bootclasspath "$AJ" -d "$work/obj" "@$work/srcs.txt" 2>&1 | grep -v '^Note:' | head -30
  [ -f "$work/obj/$class_pkg/MainActivity.class" ] || { echo "!! JAVAC FAILED"; return 1; }

  echo "==> [$key] 2/6 dex"
  ( cd "$work" && jar cf code.jar -C obj . )
  if [ -x "$BT/d8" ]; then
    "$BT/d8" --min-api 21 --release --output "$work/dexout" "$work/code.jar" 2>&1 | tail -3
    mv "$work/dexout/classes.dex" "$work/classes.dex"
  else
    "$BT/dx" --dex --min-sdk-version=21 --output="$work/classes.dex" "$work/code.jar" 2>&1 | tail -3
  fi
  [ -f "$work/classes.dex" ] || { echo "!! DEX FAILED"; return 1; }

  echo "==> [$key] 3/6 aapt package"
  "$BT/aapt" package -f -M "$src_dir/AndroidManifest.xml" -S "$src_dir/res" -A "$src_dir/assets" -I "$AJ" -m -J "$work/gen" -F "$work/base.apk" 2>&1 | tail -10
  [ -f "$work/base.apk" ] || { echo "!! AAPT FAILED"; return 1; }

  echo "==> [$key] 4/6 inject classes.dex"
  python3 -c "import zipfile,sys; zipfile.ZipFile(sys.argv[1],'a',zipfile.ZIP_DEFLATED).write(sys.argv[2],'classes.dex')" "$work/base.apk" "$work/classes.dex" || { echo "!! DEX INJECT FAILED"; return 1; }

  echo "==> [$key] 5/6 zipalign"
  "$BT/zipalign" -f 4 "$work/base.apk" "$work/aligned.apk" || { echo "!! ZIPALIGN FAILED"; return 1; }
  "$BT/zipalign" -c 4 "$work/aligned.apk" && echo "   zipalign check OK"

  echo "==> [$key] 6/6 apksigner"
  if [ ! -f "$ks_file" ]; then
    echo "   未找到签名密钥，生成临时自签密钥（仅供本地测试，请勿用于正式发布）"
    keytool -genkeypair -keystore "$ks_file" -alias "$ks_alias" -keyalg RSA -keysize 2048 -validity 10000 -storepass "$ks_pass" -keypass "$ks_pass" -dname "CN=$label, OU=test, O=Moxie, L=Local, ST=Local, C=CN" >/dev/null 2>&1
  fi
  "$BT/apksigner" sign --ks "$ks_file" --ks-key-alias "$ks_alias" --ks-pass "pass:$ks_pass" --key-pass "pass:$ks_pass" --v1-signing-enabled true --v2-signing-enabled true --out "$DIST/$artifact.apk" "$work/aligned.apk" 2>&1 | tail -3
  [ -f "$DIST/$artifact.apk" ] || { echo "!! SIGN FAILED"; return 1; }
  "$BT/apksigner" verify --print-certs "$DIST/$artifact.apk" 2>&1 | head -3
  cp "$DIST/$artifact.apk" "$DIST/$label.apk"
  ls -lh "$DIST/$artifact.apk"
  echo "   built: dist/$artifact.apk (aka dist/$label.apk)"
}

case "$APP" in
  yuwen)   build_one yuwen ;;
  english) build_one english ;;
  both)    build_one yuwen; build_one english ;;
  *) echo "用法: bash scripts/build-apk.sh [yuwen|english|both]"; exit 1 ;;
esac
rm -rf "$REPO_ROOT/.build"
echo "== done =="
