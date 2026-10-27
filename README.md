# Watchlog

映画・ドラマの作品情報、視聴できる動画配信サービス、自分の視聴履歴をまとめて管理するWebアプリケーションです。

## 主な機能

- ユーザー登録・ログイン（BCryptによるパスワードハッシュ化、セッション認証）
- 作品情報の登録・一覧・詳細・更新・削除
- 配信サービスの登録・一覧・詳細・更新・削除
- 作品と配信サービスの紐づけ・解除
- ユーザーごとの視聴履歴、視聴状態、評価、感想の管理
- 作品名・ジャンル・監督・作品種別・公開年による検索と絞り込み
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
| 視聴履歴 | GET / POST | `/api/users/{userId}/viewing-histories` |
| 視聴履歴 | GET / PATCH / DELETE | `/api/users/{userId}/viewing-histories/{id}` |

H2 Consoleは開発用に `/h2-console` で利用できます。JDBC URLは `jdbc:h2:file:./data/watchlog`、ユーザー名は `sa`、パスワードは空です。
