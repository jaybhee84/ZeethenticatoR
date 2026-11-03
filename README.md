# ZeethenticatoR

A free, offline 2FA authenticator for Android. It works with any site or app that offers "authenticator app" two-factor login (GitHub, Google, Microsoft, Discord and others), because they all use the standard TOTP codes (RFC 6238).

- No internet permission: your keys never leave your phone.
- Screenshots are blocked inside the app.
- Tap a code to copy it.

## Install

Download `ZeethenticatoR.apk` from the [latest release](../../releases/latest) on your phone, open it, and allow installing from this source when asked. New builds install as updates and keep your accounts.

## Add an account

On the site's 2FA setup page, choose "can't scan?" or "setup key", then type the key into the app. You can also paste an `otpauth://` link.

**Always save the site's recovery codes**, and use *Backup / restore* in the app. Uninstalling the app or clearing its data deletes your keys.

## Build

`index.html` is the whole app. The Android project in `android/` wraps it in a WebView. Every push to `main` builds a signed APK and publishes it to the `latest` release (`.github/workflows/build.yml`). The workflow needs the repository secrets `KEYSTORE_BASE64` and `KEYSTORE_PASSWORD`.

Local debug build: `cd android && ./gradlew assembleDebug`
