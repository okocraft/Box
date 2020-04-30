
# Box v3.1.x
<!--img src="https://i.gyazo.com/b1beaf67431058f3c72b4d8907cae57d.png" width="200"><br/-->
[![GitHub](https://img.shields.io/github/license/okocraft/Box)](https://github.com/okocraft/Box/blob/master/LICENSE)
[![javadoc](https://img.shields.io/badge/javadoc-here-green.svg)](https://okocraft.github.io/Box/)

Box はあらゆるアイテムを仮想的なインベントリへ収納できるようにするプラグインです。

## 特徴

Box には以下のような特徴があります。

* GUI によって直感的にアイテムの預け入れ・引き出し・売買・クラフトができます。
* データ保存先として MySQL・SQLite のどちらかを選択できます。
  * SQLite の場合は設定なしにすぐ使えます。
  * Bungeecord で運用するときは MySQL によりサーバ間で設定を共有できます。
* すべての機能（カテゴリの入れ替え・売買価格）の設定を自由に書き換えられます。
* アイテム名がチャットに表示されるあらゆる場面において、ホバーによってアイテム情報を閲覧できます。
* アイテムを拾ったとき自動的に Box に収納できます（自動収納の項参照）。
* アイテムを消費するとき自動的に Box から補充できます（自動消費の項参照）。

## インストール

[リリース](https://github.com/okocraft/Box/releases)から最新のリリースへ移動し、Assets から JAR ファイルをダウンロードします。
そしてダウンロードした JAR ファイルをサーバーの `plugins/` ディレクトリに投入しサーバーを再起動します。

再起動後に、

* `config.yml`
* `categories.yml`
* `layout.yml`
* `messages.yml`
* `prices.yml`
* `craftrecipes.yml`

が生成されます。

## 機能

### 自動収納

`/box autostore all true`（全アイテムを Box に収納するコマンド）を実行して拾ったアイテムをすべて Box に収納できます。
必要に応じて特定のアイテムのみ収納したり、逆に収納しないように設定できます。

### 自動消費

`/box stick` を実行すると *Box Stick* と呼ばれる棒が手に入ります。
これをオフハンドに持つことで、メインハンドにあるアイテムを消費したとき自動的に Box からそのアイテムを補充できます。

## 設定

### データベース

#### MySQL

インストール時に生成された設定を参照して適量書き換えます。その後サーバーを再起動します。

#### SQLite

追加の設定は必要ありません。

### カテゴリ

※ **初期設定はクリエイティブタブに準拠しており設定を変更せずともそのまま使えます。**

#### ファイルによる設定

カテゴリの設定は `categories.yml` に定義されています。
データベースに登録されているアイテムのほとんどがこのファイルに定義されています（コマンドブロックやバリアブロックを除く）。

`/box` コマンドを**ゲーム内**で実行してカテゴリを確認しつつ定義を書き換えます。
その後 `/boxadmin reload` を実行して差分を確認します。

#### コマンド（ゲーム内）による設定

コマンドでカテゴリを追加するには、チェストに視線を向けつつ `/boxadmin addcategory <カテゴリ名> <GUIでの表示名> <使用するアイコン>` を実行します。これによりチェストの中身を自動的にカテゴリとして追加できます。

カテゴリに追加せずアイテムを直接データベースに追加するには、対象のアイテムを手に持ち `/boxadmin register` を実行します。
名前は登録されたアイテムを持ち `/box iteminfo` を実行することで参照できます。
この名前をカテゴリに登録することで GUI からそのアイテムを参照できるようになります。

登録されたアイテムは `<material>:<id>` というフォーマットです。
これは `/boxadmin customname <アイテム> <新規名>` を実行することで変更できます。

### 売買価格

Box の GUI による売買価格は `prices.yml` に定義されています。
アイテム名は `categories.yml` に記載されているものを使用してください。

なお、現状（**v3.1.x**）デフォルトでデータベースに存在するアイテムはほとんど Spigot の [`Material` 列挙型](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)と同じものです。
ポーション、スプラッシュポーション、リンガリングポーション、効果付き矢、エンチャント本以外は除きます（後述）。

ポーション効果のあるものは `<アイテム名>_<効果名>[_<EXTENDED | UPGRADED>]` です。
時間延長には `EXTENDED`、効果増強には `UPGRADED` を付加してください。
効果名は Spigot の[`PotionType` 列挙型](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html)を参照してください。

エンチャント本の名前は `ENCHANTED_BOOK_<エンチャント名>` です。エンチャント名は Spigot の [`Enchantment` クラス](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html)を参照してください。

### GUI 経由のクラフトレシピ

Box の GUI によるクラフトレシピは `craftrecipes.yml` に定義されています。

標準で設定されるクラフトレシピはマインクラフトで使用されるデフォルトのレシピとなっています。
標準のクラフトレシピがおかしい場合は `craftrecipes.yml` に追記して対応できます。記述はデフォルトで書かれていることを参考にします。

### その他

* メッセージ設定： `messages.yml`を変更します。
* GUI の表示設定： `layout.yml`を変更します。

## コマンド

* `/box autostore <ITEM | ALL> [true | false]`: 自動収納の設定を変更します。
* `/box autostorelist <page>`: 自動収納の設定を表示します。
* `/box give <player> <ITEM> [amount]`: 誰かにアイテムを与えます。
* `/box stick`: オフハンドに持つとアイテム消費先が Box になる杖を手に入れます。
* `/box help [page]`: ヘルプを表示します。
* `/box version`: バージョンを表示します。
* `/box <iteminfo | iinfo>`: アイテム情報が見れるホバーを表示します。
* `/box <deposit | d> [<amount> | <ITEM> [amount] | ALL]`: 引数無しで手持ちのアイテムを預けます。ALL を指定すると手持ちのアイテムで* 預けられるものをすべて預けられます。アイテム名を指定すると、インベントリからそのアイテムを預けます。
* `/box <withdraw | w> <ITEM> [amount]`: アイテムを引き出します。

* `/boxadmin autostore <player> < <ITEM> [true|false] | ALL <true|false> >`: 自動収納の設定を変更します。
* `/boxadmin autostorelist <player> <page>`: 自動収納の設定を表示します。
* `/boxadmin reload`: 設定を再読込します。
* `/boxadmin customname <ITEM> <after>`: アイテムの内部名前を設定します。この名前は GUI 上でも使われます。
* `/boxadmin addcategory <カテゴリ名> <GUI上の表示名> <ICONになるアイテム>`: チェストをクリックして、中にはいっているアイテムをもとにカ* テゴリを追加します。
* `/boxadmin give <player> <ITEM> [amount]`: 誰かにアイテムを与えます。
* `/boxadmin take <player> <ITEM> [amount]`: 誰かのアイテムを減らします。
* `/boxadmin set <player> <ITEM> <amount>`: 誰かのアイテム量をセットします。
* `/boxadmin help <page>`: ヘルプを表示します。
* `/boxadmin register`: 手に持っているアイテムをデータベースに登録します。

## 権限

### `/boxadmin`

* `boxadmin.*`:
  * 説明： `/boxadmin <args...>`コマンドのすべてを使うための権限
  * デフォルト権限： `op`
* `boxadmin`:
  * 説明： `/boxadmin ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.addcategory`:
  * 説明： `/boxadmin addcategory ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.autostore`:
  * 説明： `/boxadmin autostore ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.autostorelist`:
  * 説明： `/boxadmin autostorelist ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.customname`:
  * 説明： `/boxadmin customname ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.register`:
  * 説明： `/boxadmin register` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.give`:
  * 説明： `/boxadmin give ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.help`:
  * 説明： `/boxadmin help ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.reload`:
  * 説明： `/boxadmin reload` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.set`:
  * 説明： `/boxadmin set ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.take`:
  * 説明： `/boxadmin take ...` コマンドを使うための権限
  * デフォルト権限： `op`
* `boxadmin.creative`:
  * 説明： GUI でアイテムを取引してもストックに影響がなくなる権限。取り出し放題になる。
  * デフォルト権限： `false`

### `/box`

* `box.*`:
  * 説明： `/box <args...>` のすべてを使えるようになる権限
  * デフォルト権限： `op`
* `box.autostore`:
  * 説明： `/box autostore ...` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.autostorelist`:
  * 説明： `/box autostorelist ...` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.withdraw`:
  * 説明： `/box withdraw ...` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.deposit`:
  * 説明： `/box deposit ...` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.give`:
  * 説明： `/box give ...` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.help`:
  * 説明： `/box help ...` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.version`:
  * 説明： `/box version` コマンドを使うための権限
  * デフォルト権限： `true`

### `/box stick`

* `box.stick.*`:
  * 説明： `/box stick` に関するすべての権限
  * デフォルト権限： `true`
* `box.stick`:
  * 説明： `/box stick` コマンドを使うための権限
  * デフォルト権限： `true`
* `box.stick.block`:
  * 説明： `/box stick` でブロックをデータベースから消費する権限
  * デフォルト権限： `true`
* `box.stick.food`:
  * 説明： `/box stick` で食べ物をデータベースから消費する権限
  * デフォルト権限： `true`
* `box.stick.potion`:
  * 説明： `/box stick` でポーションをデータベースから消費する権限
  * デフォルト権限： `true`
* `box.stick.tool`:
  * 説明： `/box stick` で武具・道具をデータベースから消費する権限
  * デフォルト権限： `true`
