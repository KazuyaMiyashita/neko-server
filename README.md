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

別のターミナル
$ cd gitをクローンしたとこ
$ cd chat/frontend
$ python -m http.server 8000 (https://qiita.com/okhrn/items/4d3c74563154f191ba16 あたり参考)
ブラウザで localhost:8000
```

