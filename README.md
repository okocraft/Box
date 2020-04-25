
# Box v3.1.x
<!--img src="https://i.gyazo.com/b1beaf67431058f3c72b4d8907cae57d.png" width="200"><br/-->
[![GitHub](https://img.shields.io/github/license/okocraft/Box)](https://github.com/okocraft/Box/blob/master/LICENSE)
[![javadoc](https://img.shields.io/badge/javadoc-here-green.svg)](https://okocraft.github.io/Box/)<br/>

Boxはあらゆるアイテムを仮想的なインベントリへ収納できるようにするプラグインです。<br/>

## 特徴
Boxには、以下のような特徴があります:
* GUIによるアイテムの預け入れ、引き出し、購入、売却、およびクラフト。
* データ保存先をMySQL・SQLiteのどちらかから選択可能。（Bungeecordで利用する場合はMySQLで各鯖に同じ設定を適応します）
* アイテムのカテゴリ、GUIにおける並び順、GUIに表示されるアイテムの表示、販売価格、クラフトレシピなどのすべてを設定可能。
* アイテム名がチャットに表示されるあらゆる場面において、ホバーによってアイテム情報を閲覧可能。
* 拾っただけでBoxにアイテムを収納できる機能。
* オフハンドにBox stickを持ってメインハンドのアイテムを使った場合、Boxから消費される機能。

## インストール
[リリース](https://github.com/okocraft/Box/releases)から最新のリリースへ移動し、Assetsからjarファイルをダウンロードします。<br/>
次に、ダウンロードしたjarファイルをサーバーディレクトリのpluginsフォルダーに投入し、サーバーを再起動します。
再起動後には、
* `config.yml`
* `categories.yml`
* `layout.yml`
* `messages.yml`
* `prices.yml`
* `craftrecipes.yml`

の6つが生成されます。

## 設定
1. データベース<br/>
MySQLを利用する場合は、サーバー起動時に生成されたconfigを見て必要事項を記入し、サーバーを再起動します。<br/>
SQLiteを利用する場合はそのままご利用いただけます。

1. カテゴリ設定<br/>
**初期設定はクリエイティブタブに準拠しており、設定を変更せずともそのまま使えます。**<br/>
設定を変更する場合は`categories.yml`を編集します。<br/>
ここにデフォルトで設定されているアイテムが、初期設定でデータベースに存在しているアイテムのほとんどです（コマンドブロックやバリアブロックを除く）。<br/>
実際にゲーム内で`/box`コマンドを実行してカテゴリを確認しつつ、ymlファイルを変更、`/boxadmin reload`を実行して差分を確認します。<br/>
ゲーム内でカテゴリを追加するには、チェストに視線を向けつつ`/boxadmin addcategory <カテゴリ名> <GUIでの表示名> <使用するアイコン>`コマンドを利用して、自動的にチェストの中のアイテムをカテゴリに追加できます。<br/>
カテゴリに追加せず、アイテムをデータベースに追加する場合は`/boxadmin register`で手に持っているアイテムを登録できます。登録されたアイテムの名前は、登録されたアイテムを持って`/box iteminfo`を使うと名前を確認できます。<br/>
この名前をカテゴリに追加することで、ゲーム内でGUIにアイテムを追加できます。
登録されたアイテムは`Material名:<id>`というフォーマットになりますが、これは`/boxadmin customname <ITEM> <after>`コマンドを使って変更できます。

1. GUIにおける売買の価格設定<br/>
`prices.yml`の設定を変更します。この`prices.yml`で設定されているアイテム名は[ここ](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)に指定されている名前**ではなく**、`categories.yml`に記載されているものを利用します。<br/>
とはいえ、現状デフォルトでデータベースに存在するアイテムはポーション、スプラッシュポーション、リンガリングポーション、効果付き矢、エンチャント本以外は上記Materialリストと同じものです。<br/>
ポーション効果があるものについては<br/>
`<Material名>_<Potionタイプ名>_<EXTENDED>`または`<Material名>_<Potionタイプ名>_<UPGRADED>`<br/>
となります。時間延長はEXTENDED、効果増強はUPGRADEDです。PotionType名は[ここ](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html)に書いてあります。<br/>
エンチャント本については<br/>
`ENCHANTED_BOOK_<Enchant名>`<br/>
となっています。Enchant名は[ここ](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html)で確認できます。

1. GUIにおけるクラフトのレシピ設定<br/>
`craftrecipes.yml`にレシピを追記することで、クラフトGUIで行えるクラフトのレシピを上書きできます。<br/>
標準で設定されるレシピはマインクラフトで使用されるデフォルトのレシピとなっています。標準のレシピがおかしい場合は`craftrecipes.yml`に追記して対処できます。書き方はデフォルトで書かれていることを参考にします。

1. その他<br/>
メッセージ設定 → `messages.yml`を変更します。<br/>
GUIの表示設定 → `layout.yml`を変更し、アイテムの表示を変更します。<br/>

## コマンド
`/box autostore <ITEM | ALL> [true | false]`: 自動収納の設定を変更します。<br/>
`/box autostorelist <page>`: 自動収納の設定を表示します。<br/>
`/box give <player> <ITEM> [amount]`: 誰かにアイテムを与えます。<br/>
`/box stick`: オフハンドに持つとアイテム消費先がBoxになる杖を手に入れます。<br/>
`/box help [page]`: ヘルプを表示します。<br/>
`/box version`: バージョンを表示します。<br/>
`/box <iteminfo | iinfo> `: アイテム情報が見れるホバーを表示します。<br/>
`/box <deposit | d> [<amount> | <ITEM> [amount] | ALL]`: 引数無しで手持ちのアイテムを預けます。ALLを指定すると手持ちのアイテムで預けられるものをすべて預けられます。アイテム名を指定すると、インベントリからそのアイテムを預けます。<br/>
`/box <withdraw | w> <ITEM> [amount]`: アイテムを引き出します。<br/>

`/boxadmin autostore <player> < <ITEM> [true|false] | ALL <true|false> >`: 自動収納の設定を変更します。<br/>
`/boxadmin autostorelist <player> <page>`: 自動収納の設定を表示します。<br/>
`/boxadmin reload`: 設定を再読込します。<br/>
`/boxadmin customname <ITEM> <after>`: アイテムの内部名前を設定します。この名前はGUI上でも使われます。<br/>
`/boxadmin addcategory <カテゴリ名> <GUI上の表示名> <ICONになるアイテム>`: チェストをクリックして、中にはいっているアイテムをもとにカテゴリを追加します。<br/>
`/boxadmin give <player> <ITEM> [amount]`: 誰かにアイテムを与えます。<br/>
`/boxadmin take <player> <ITEM> [amount]`: 誰かのアイテムを減らします。<br/>
`/boxadmin set <player> <ITEM> <amount>`: 誰かのアイテム量をセットします。<br/>
`/boxadmin help <page>`: ヘルプを表示します。<br/>
`/boxadmin register`: 手に持っているアイテムをデータベースに登録します。<br/>

## 権限
* `boxadmin.*`:
  * 説明: `/boxadmin <args...>`コマンドのすべてを使うための権限
  * デフォルト権限: `op`
  * 子権限:
    * `boxadmin.addcategory`: `true`
    * `boxadmin.autostore`: `true`
    * `boxadmin.autostorelist`: `true`
    * `boxadmin.customname`: `true`
    * `boxadmin.register`: `true`
    * `boxadmin.give`: `true`
    * `boxadmin.help`: `true`
    * `boxadmin.reload`: `true`
    * `boxadmin.set`: `true`
    * `boxadmin.take`: `true`
    * `boxadmin`: `true`
* `boxadmin`:
  * 説明: `/boxadmin ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.addcategory`:
  * 説明: `/boxadmin addcategory ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.autostore`:
  * 説明: `/boxadmin autostore ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.autostorelist`:
  * 説明: `/boxadmin autostorelist ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.customname`:
  * 説明: `/boxadmin customname ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.register`:
  * 説明: `/boxadmin register` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.give`:
  * 説明: `/boxadmin give ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.help`:
  * 説明: `/boxadmin help ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.reload`:
  * 説明: `/boxadmin reload` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.set`:
  * 説明: `/boxadmin set ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.take`:
  * 説明: `/boxadmin take ...` コマンドを使うための権限
  * デフォルト権限: `op`
* `boxadmin.creative`:
  * 説明: GUIでアイテムを取引してもストックに影響がなくなる権限。取り出し放題になる。
  * デフォルト権限: `false`
* `box.*`:
  * 説明: `/box <args...>` のすべてを使えるようになる権限
  * デフォルト権限: `op`
  * 子権限:
    * `box.autostore`: `true`
    * `box.autostorelist`: `true`
    * `box.withdraw`: `true`
    * `box.deposit`: `true`
    * `box.give`: `true`
    * `box.help`: `true`
    * `box.version`: `true`
    * `box.stick.*`: `true`
* `box.autostore`:
  * 説明: `/box autostore ...` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.autostorelist`:
  * 説明: `/box autostorelist ...` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.withdraw`:
  * 説明: `/box withdraw ...` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.deposit`:
  * 説明: `/box deposit ...` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.give`:
  * 説明: `/box give ...` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.help`:
  * 説明: `/box help ...` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.version`:
  * 説明: `/box version` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.stick.*`:
  * 説明: box stickに関するすべての権限
  * デフォルト権限: `true`
  * 子権限:
    * `box.stick`: `true`
    * `box.stick.block`: `true`
    * `box.stick.food`: `true`
    * `box.stick.potion`: `true`
    * `box.stick.tool`: `true`
* `box.stick`:
  * 説明: `/box stick` コマンドを使うための権限
  * デフォルト権限: `true`
* `box.stick.block`:
  * 説明: box stickでブロックをデータベースから消費する権限
  * デフォルト権限: `true`
* `box.stick.food`:
  * 説明: box stickで食べ物をデータベースから消費する権限
  * デフォルト権限: `true`
* `box.stick.potion`:
  * 説明: box stickでポーションをデータベースから消費する権限
  * デフォルト権限: `true`
* `box.stick.tool`:
  * 説明: box stickで武具・道具をデータベースから消費する権限
  * デフォルト権限: `true`
