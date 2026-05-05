from aiogram import Bot, Dispatcher, types, F
from aiogram.filters.command import Command
from aiogram.types import LabeledPrice
import asyncio
from datetime import timedelta
from copy import copy
from aiogram.fsm.context import FSMContext
from functions import logger
from functions import hello_text
from functions import get_start_keyboard
from functions import donate_keyboard
from functions import is_activated
from functions import execute_transactions_query
from functions import keyboard_for_management
from functions import MyStates
from functions import execute_query
from functions import get_user_id_from_tg_id
from functions import check_user_url_pair
from functions import check_url_in_video_stats
from functions import common_stats_text
from functions import get_url_keyboard
from functions import depth_stat_text
from functions import get_url_keyboard_with_back
from functions import deleter_keyboard
from functions import deleter_keyboard_with_back


from datetime import datetime
import time

import aiogram
from aiogram.exceptions import TelegramBadRequest

import os
from dotenv import load_dotenv


load_dotenv()

API_TOKEN = os.getenv('BOT_TOKEN')

# Инициализация бота и диспетчера
bot = Bot(token=API_TOKEN)
dp = Dispatcher(bot=bot)

# Обработчик команды /start
@dp.message(Command('start'))
async def send_start(message: types.Message, state: FSMContext):
    args = message.text.split()[1] if len(message.text.split()) > 1 else 'default'
    logger.info(f"{str(datetime.now())[0:len(str(datetime.now())) - 7]}  {message.from_user.username} ({message.from_user.id}) executes command /start with args {args}")
    # проверим, есть ли пользователь в таблице users
    is_user_activated = await is_activated(message.from_user.id)
    if not is_user_activated:
        query = f"INSERT INTO users (telegram_id) VALUES ({message.from_user.id});"
        rs = await execute_transactions_query(query)

    text = hello_text()
    kb = get_start_keyboard()
    await message.answer(text=text, reply_markup=kb, parse_mode="HTML")

    await state.set_state(MyStates.waiting_for_url)


@dp.message(F.text == "Мой контент")
async def send_spot_ai_analyze(message: types.Message):
    current_time = str(datetime.now())
    logger.info(f"{current_time[0:len(str(datetime.now())) - 7]}  {message.from_user.username} ({message.from_user.id}) presses content")
    # Тут будет вывод общей статистики в разрезе платформ и кнопки для просмотра статистики по каждому из видео
    text = await common_stats_text(message.from_user.id)
    kb = await get_url_keyboard(message.from_user.id)
    await message.answer(text=text, reply_markup=kb, parse_mode="HTML")

@dp.callback_query(lambda c: "url" in c.data)
async def ask_for_input(callback: types.CallbackQuery, state: FSMContext):
    video_stats_id = callback.data.split('_')[1]
    if video_stats_id != 'back':
        query = f"select urls.url, stats, status, created_at from video_stats left join urls on urls.id = video_stats.url_id where video_stats.id = (select max(id) from video_stats where url_id = (select urls.id from video_stats left join urls on urls.id = video_stats.url_id where video_stats.id = {video_stats_id}));"
        actual_statistic = await execute_query(query)
        actual_statistic = actual_statistic[0]
        text = depth_stat_text(actual_statistic)
        kb = await get_url_keyboard_with_back(callback.from_user.id)
        await callback.message.edit_text(text=text, parse_mode="HTML")
        await callback.message.edit_reply_markup(reply_markup=kb)
    else:
        text = await common_stats_text(callback.from_user.id)
        kb = await get_url_keyboard(callback.from_user.id)
        await callback.message.edit_text(text=text, parse_mode="HTML")
        await callback.message.edit_reply_markup(reply_markup=kb)

@dp.message(F.text == "Управление")
async def send_spot_ai_analyze(message: types.Message):
    current_time = str(datetime.now())
    logger.info(f"{current_time[0:len(str(datetime.now())) - 7]}  {message.from_user.username} ({message.from_user.id}) presses management")
    kb = keyboard_for_management()
    await message.answer(text="Что хотите сделать?", reply_markup=kb, parse_mode="HTML")


@dp.callback_query(lambda c: "video" in c.data)
async def ask_for_input(callback: types.CallbackQuery, state: FSMContext):
    action = callback.data.split('_')[1]
    if action == "add":
        await callback.message.answer("Пришлите ссылку на видео:")
        # Устанавливаем состояние ожидания
        await state.set_state(MyStates.waiting_for_url)
        await callback.answer()
    else:
        kb = await deleter_keyboard_with_back(callback.from_user.id)
        await callback.message.edit_text(text="Нажмите на ссылку для удаления")
        await callback.message.edit_reply_markup(reply_markup=kb)


@dp.callback_query(lambda c: "deleter" in c.data)
async def ask_for_input(callback: types.CallbackQuery, state: FSMContext):
    for_delete_id = callback.data.split('_')[1]
    if for_delete_id != 'back':
        query = f"delete from users_urls where id = {for_delete_id};"
        await execute_transactions_query(query)
        kb = await deleter_keyboard_with_back(callback.from_user.id)
        await callback.message.edit_text(text="Выберите видео для удаления")
        await callback.message.edit_reply_markup(reply_markup=kb)
    else:
        kb = keyboard_for_management()
        await callback.message.edit_text(text="Что хотите сделать?", parse_mode="HTML")
        await callback.message.edit_reply_markup(reply_markup=kb)

@dp.message(MyStates.waiting_for_url, F.text.regexp(r'^https?://'))
async def process_name(message: types.Message, state: FSMContext):
    url = message.text.replace(' ', '') #убираем пробелы в ссылке

    # Проверяем, подходит ли нам url по формату
    if "https://youtu.be" in url or "https://youtube.com" in url or "https://www.youtube.com" in url:
        is_url_correct = True
        platform = 'youtube'
    elif "https://vimeo.com" in url:
        platform = 'vimeo'
        is_url_correct = True
    elif "https://vkvideo.ru" in url:
        is_url_correct = True
        platform = 'vk'
    elif "https://dzen.ru" in url:
        is_url_correct = True
        platform = 'dzen'
    elif "https://rutube.ru" in url:
        is_url_correct = True
        platform = 'rutube'
    else:
        is_url_correct = False
        platform = "not supported"
    if is_url_correct:
        # проверяем, есть ли url в таблице urls

        logger.info(platform)
        logger.info(is_url_correct)

        is_url_in_urls_query = f"select * from urls where url = '{url}';"
        is_url_in_urls_result = await execute_query(is_url_in_urls_query)
        if is_url_in_urls_result == []: # видео не было добавлено ранее
            insert_url_in_urls_query = f"insert into urls (url, platform_id) values ('{url}', (SELECT id FROM platforms WHERE name = '{platform}'));"
            insert_url_in_urls_res = await execute_transactions_query(insert_url_in_urls_query)

            # находим id ссылки
            is_url_in_urls_query = f"select * from urls where url = '{url}';"
            is_url_in_urls_result = await execute_query(is_url_in_urls_query)
            url_id = is_url_in_urls_result[0]['id']

        else:
            url_id = is_url_in_urls_result[0]['id']

        # Получаем user_id из telegram_id
        user_id = await get_user_id_from_tg_id(message.from_user.id)

        # Смотрим, есть ли в users_urls пара (user_id, url_id)
        is_user_added_video_early = await check_user_url_pair(user_id, url_id)

        if is_user_added_video_early:
            await message.answer("Это видео было добавлено ранее")
        else:
            insert_user_url_query = f"insert into users_urls (user_id, url_id) values ({user_id}, {url_id});"
            insert_user_url_res = await execute_transactions_query(insert_user_url_query)
            await message.answer("Ссылка успешно добавлена")

        # Проверим, есть ли url в таблице статистики
        is_url_in_stats = await check_url_in_video_stats(url_id)
        if not is_url_in_stats:
            insert_url_in_stat_query = f"insert into video_stats (url_id) values ({url_id});"
            insert_url_in_stat_res = await execute_transactions_query(insert_url_in_stat_query)
    else:
        await(message.answer(text="Неверный формат ссылки."))

    # await state.clear() # Сбрасываем состояние

@dp.message(F.text == "Поддержать проект")
async def send_spot_ai_analyze(message: types.Message):
    current_time = str(datetime.now())
    logger.info(f"{current_time[0:len(str(datetime.now())) - 7]}  {message.from_user.username} ({message.from_user.id}) presses support")
    # Тут будет вывод общей статистики в разрезе платформ и кнопки для просмотра статистики по каждому из видео
    keyboard = donate_keyboard()
    prices = [LabeledPrice(label="XTR", amount=100)]
    await message.answer_invoice(
        title="Поддержка проекта",
        description="Поддержать проект на 100 звезд",
        prices=prices,
        provider_token="",
        payload="donate",
        currency="XTR",
        reply_markup=keyboard
    )

@dp.pre_checkout_query()
async def process_precheckout(pre_checkout_query: types.PreCheckoutQuery):
    payload = pre_checkout_query.invoice_payload
    if payload == "donate":
        await bot.answer_pre_checkout_query(
            pre_checkout_query_id=pre_checkout_query.id,
            ok=True
        )
# Обработчик успешного платежа (вызывается после answer_pre_checkout_query(ok=True))
@dp.message(lambda message: message.successful_payment is not None)
async def successful_payment_handler(message: types.Message):
    if message.successful_payment.invoice_payload == 'donate':
        await message.answer("😉 Спасибо за поддержку нашего бота.")

async def main(dp):
    await dp.start_polling(bot)


# Запуск бота
if __name__ == '__main__':
    asyncio.run(main(dp))