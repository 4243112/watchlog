# Watchlog

映画・ドラマの作品情報、視聴できる動画配信サービス、自分の視聴履歴をまとめて管理するWebアプリケーションです。

## 主な機能

- ユーザー登録・ログイン（BCryptによるパスワードハッシュ化、セッション認証）
- 作品情報の登録・一覧・詳細・更新・削除
- 配信サービスの登録・一覧・詳細・更新・削除
- 作品と配信サービスの紐づけ・解除
- ユーザーごとの視聴履歴、視聴状態、評価、感想の管理
- 作品名・ジャンル・監督・作品種別（映画・ドラマ・アニメ）・公開年による検索と絞り込み
- TMDBからの作品検索、ポスター画像、ジャンル、監督・制作者の取り込み
- TMDB/JustWatchデータによる日本向け配信サービスの自動紐づけ
- API全体で統一したエラーレスポンス

## 技術構成

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC / Spring Data JPA / Spring Security / Bean Validation
- H2 Database（ファイル永続化）
- Vanilla JavaScript / HTML / CSS

## 起動方法

Windows:

```powershell
./mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

起動後、ブラウザで [http://localhost:8080](http://localhost:8080) を開いてください。初回起動時はサンプル作品と配信サービスが自動登録されます。

## TMDB連携

TMDBのアカウント設定から「API Read Access Token」を取得し、環境変数として設定します。トークンは設定ファイルやGitへ保存しないでください。

Windows PowerShell:

```powershell
$env:TMDB_READ_ACCESS_TOKEN="取得したRead Access Token"
./mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
TMDB_READ_ACCESS_TOKEN="取得したRead Access Token" ./mvnw spring-boot:run
```

起動後、「TMDBから追加」から映画・ドラマ・アニメを検索できます。作品を追加すると、ポスターと日本向け配信情報も保存されます。配信プロバイダー情報はJustWatch由来です。

## テスト

```powershell
./mvnw.cmd test
```

## API

| 対象 | メソッド | URL |
| --- | --- | --- |
| 認証 | POST | `/api/auth/register` |
| 認証 | POST | `/api/auth/login` |
| 認証 | GET | `/api/auth/me` |
| 認証 | POST | `/api/auth/logout` |
| 作品 | GET / POST | `/api/works` |
| 作品 | GET / PUT / DELETE | `/api/works/{id}` |
| 配信サービス | GET / POST | `/api/streaming-services` |
| 配信サービス | GET / PUT / DELETE | `/api/streaming-services/{id}` |
| 作品の配信情報 | POST | `/api/works/{workId}/streaming-services` |
| 作品の配信情報 | PUT / DELETE | `/api/works/{workId}/streaming-services/{availabilityId}` |
| TMDB連携状態 | GET | `/api/external/tmdb/status` |
| TMDB作品検索 | GET | `/api/external/tmdb/search?query={作品名}&type={種別}` |
| TMDB作品取込 | POST | `/api/external/tmdb/import` |
| 視聴履歴 | GET / POST | `/api/users/{userId}/viewing-histories` |
| 視聴履歴 | GET / PATCH / DELETE | `/api/users/{userId}/viewing-histories/{id}` |

H2 Consoleは開発用に `/h2-console` で利用できます。JDBC URLは `jdbc:h2:file:./data/watchlog`、ユーザー名は `sa`、パスワードは空です。

## データ提供元

This product uses the TMDB API but is not endorsed or certified by TMDB.

配信情報はJustWatch提供データを利用しています。TMDB APIの利用条件や商用ライセンスについては公式ドキュメントを確認してください。
