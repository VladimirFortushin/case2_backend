# bot_backend

Backend для Telegram-бота со сбором статистики просмотров видео.

## Что реализовано:
- Валидация URL и определение платформы (youTube, rutube, vimeo).
- Запрос статистики по просмотрам видео по расписанию (каждые 2 минуты) по каждому уникальному url.
- Добавление в БД обновленной статистики.
- Возврат последних успешных просмотров при недоступности источника.
- Docker/Docker Compose для быстрого запуска.

## Переменные окружения
- `YOUTUBE_API_URL` - урл api youtube
- `RUTUBE_API_URL` - урл api rutube
- `VIMEO_API_URL` - урл api vimeo
- `YOUTUBE_API_TOKEN` - токен api youtube
- `RUTUBE_API_TOKEN` - токен api rutube
- `VIMEO_API_TOKEN` - токен api vimeo
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `DB_SERVICE` - имя сервиса БД в compose.yml
