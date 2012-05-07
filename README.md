Android Aws Test
==========

AWS(Amazon Web Service) SDK for Android テストアプリ

## 概要

- Android 用 AWS SDK( http://aws.amazon.com/jp/sdkforandroid/ )の技術検証アプリです
  - SDKにはEC2のサンプルが付属しないため具体的な実装を検証しています。
  - ※SDKにはS3、SDB、SNS、SQSのサンプルが付属しています。

## 機能

- EC2インスタンス一覧の取得
- EC2インスタンスの詳細情報表示
- EC2詳細情報のクリップボードへコピー
- EC2インスタンスのTerminate, Reboot, Start, Stop
- EC2インスタンスの起動
- TTS(TextToSpeech)でのテキスト読み上げ

## インストール

- 要Android OS 2.2 以降

- Eclipseを起動して、ファイル > インポート > 一般 > 既存プロジェクトをワークスペースへ を選択
- ルートディレクトリの選択 で sourceフォルダを指定
- コンパイルに失敗する場合は libs/aws-android-sdk-x.x.x-debug.jar を右クリック > ビルド・パス > 追加
- 実行かデバッグでエミュレータが起動します

## 使い方

- 初めにメニュー > 設定からAccess Key, Secret Access Keyを設定して下さい
- TTSを使うためには
  - Android メニュー > 設定 > 音声入出力 > テキスト読み上げの設定 > 音声データをインストール
  - アプリの設定からTTSにチェック

## FAQ

- うまく動かない場合、Androidエミュレータの時刻が合っているか、インターネットに接続できるか確認して下さい。時刻が狂っているとAWS APIが失敗します。

## 既知の問題
