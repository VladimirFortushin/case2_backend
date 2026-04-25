# case2_backend

Backend MVP для Telegram-бота со сбором статистики просмотров видео.

## Что реализовано
- Telegram-команды: `/add`, `/list`, `/stats`, `/refresh`.
- Inline-кнопка `Обновить статистику` с callback-обработчиком.
- Валидация URL и определение платформы (YouTube, RuTube).
- Сохранение ссылок по пользователю Telegram.
- Обновление статистики по расписанию и по команде.
- Возврат последних успешных просмотров при недоступности источника.
- Docker/Docker Compose для быстрого запуска.

## Переменные окружения
- `TELEGRAM_BOT_TOKEN` — токен бота (обязательно для запуска бота).
- `TELEGRAM_ALLOWED_USER_IDS` — CSV из 1-2 ID пользователей, которым разрешен доступ.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- `YOUTUBE_API_TOKEN`.
- `RUTUBE_API_URL` (по умолчанию `https://rutube.ru/api/video`).

## Запуск через Docker
```bash
docker compose up --build
```

## Локальный запуск
```bash
mvn package
java -jar target/case2_backend-1.0.jar
```

## Команды бота
- `/add <url>` — добавить ссылку.
- `/list` — список ссылок + последние просмотры + статус источника.
- `/stats` — число ссылок и сумма просмотров.
- `/refresh` — обновить статистику сразу.
