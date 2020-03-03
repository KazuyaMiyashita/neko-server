# ねこサーバー

sbtのrootプロジェクトにサーバーアプリケーションのコアな部分、chatサブプロジェクトにチャットのアプリケーションを書いてます

```
$ brew install scala
$ brew install sbt （scalaのパッケージマネージャー）

$ cd gitをクローンしたとこ
$ cd chat
$ docker-compose up （mysqlたてる）

$ export DB_URL="jdbc:mysql://localhost:13306/db"
$ export DB_USER="root"
$ export DB_PASSWORD=""

別のターミナル
$ cd gitをクローンしたとこ
$ sbt
sbt > project chat
chat > run
```

# ねこフロント
```
別のターミナル
$ brew install yarn
$ cd gitをクローンしたとこ
$ cd chat/frontend
$ yarn install
$ yarn serve
ブラウザで localhost:8000
```
