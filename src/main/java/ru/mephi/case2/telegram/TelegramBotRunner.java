package ru.mephi.case2.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import ru.mephi.case2.api.ApiUpdateListener;
import ru.mephi.case2.log.BackendLogger;
import ru.mephi.case2.service.LinkService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TelegramBotRunner {

    private final TelegramBot bot;
    private final LinkService linkService;
    private final ApiUpdateListener updateListener;
    private final Set<Long> allowedUsers;

    public TelegramBotRunner(String token, LinkService linkService, ApiUpdateListener updateListener, Set<Long> allowedUsers) {
        this.bot = new TelegramBot(token);
        this.linkService = linkService;
        this.updateListener = updateListener;
        this.allowedUsers = allowedUsers;
    }

    public void start() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null) {
                    handleMessage(update.message());
                }
                if (update.callbackQuery() != null) {
                    handleCallback(update.callbackQuery());
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleMessage(Message message) {
        Long userId = message.from().id();
        if (!isAllowed(userId)) {
            send(message.chat().id(), "Доступ запрещен.");
            return;
        }

        String text = message.text();
        if (text == null || text.isBlank()) {
            return;
        }

        if (text.startsWith("/add ")) {
            String url = text.substring(5).trim();
            try {
                boolean added = linkService.addLink(userId, url);
                send(message.chat().id(), added ? "Ссылка добавлена." : "Такая ссылка уже была добавлена.");
            } catch (IllegalArgumentException e) {
                send(message.chat().id(), e.getMessage());
            }
            return;
        }

        if (text.equals("/list")) {
            StringBuilder response = new StringBuilder("Ваши ссылки:\n");
            linkService.list(userId).forEach(row -> {
                String state = row.latestStatus() ? "актуально" : "источник недоступен, данные устарели";
                response.append("- ")
                        .append(row.platform())
                        .append(" | ")
                        .append(row.url())
                        .append(" | просмотры: ")
                        .append(row.lastSuccessfulViews() == null ? "нет данных" : row.lastSuccessfulViews())
                        .append(" | ")
                        .append(state)
                        .append("\n");
            });
            send(message.chat().id(), response.toString());
            return;
        }

        if (text.equals("/stats")) {
            var summary = linkService.summary(userId);
            send(message.chat().id(), "Ссылок: " + summary.totalUrls() + "\nСумма просмотров: " + summary.totalViews());
            return;
        }

        if (text.equals("/refresh")) {
            updateListener.triggerUpdateAndSave(linkService.allUrls());
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Обновить статистику").callbackData("refresh")
            );
            bot.execute(new SendMessage(message.chat().id(), "Обновление выполнено.").replyMarkup(keyboard));
            return;
        }

        if (text.equals("/start")) {
            send(message.chat().id(), "Команды: /add <url>, /list, /stats, /refresh");
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.from().id();
        if (!isAllowed(userId)) {
            return;
        }

        if ("refresh".equals(callbackQuery.data())) {
            try {
                updateListener.triggerUpdateAndSave(linkService.allUrls());
                bot.execute(new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Обновлено успешно."));
            } catch (Exception e) {
                bot.execute(new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Ошибка обновления: источник временно недоступен."));
            }
        }
    }

    private boolean isAllowed(Long userId) {
        return allowedUsers.isEmpty() || allowedUsers.contains(userId);
    }

    private void send(Long chatId, String message) {
        bot.execute(new SendMessage(chatId, message));
    }

    public static Set<Long> parseAllowedUsers() {
        String value = System.getenv("TELEGRAM_ALLOWED_USER_IDS");
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<Long> result = new HashSet<>();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .forEach(v -> result.add(Long.parseLong(v)));
        BackendLogger.log("Loaded allowed telegram users count: " + result.size());
        return result;
    }
}
