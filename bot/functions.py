import logging
from config import PATH_TO_BOT_LOG_FILE
import sys
from aiogram.types import ReplyKeyboardMarkup, KeyboardButton
from aiogram.types import InlineKeyboardButton, InlineKeyboardMarkup
from aiogram.utils.keyboard import InlineKeyboardBuilder
import asyncpg
from datetime import datetime
from aiogram.fsm.state import StatesGroup, State

import os
from dotenv import load_dotenv


load_dotenv()

DB_USER = os.getenv('POSTGRES_USER')
DB_PASSWORD = os.getenv('POSTGRES_PASSWORD')
DATABASE = os.getenv('POSTGRES_DB')
DATABASE_HOST = os.getenv('DATABASE_HOST')


def create_logger():
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    file_handler = logging.FileHandler(PATH_TO_BOT_LOG_FILE)
    logger.addHandler(file_handler)

    ## для тестов
    # console_handler = logging.StreamHandler(sys.stdout)
    # logger.addHandler(console_handler)

    logging.getLogger("aiogram").setLevel(logging.ERROR) ## Не выводим лишние логи, иначе предупреждения будут писаться

    return logger
logger = create_logger()


class MyStates(StatesGroup):
    waiting_for_url = State()

def hello_text():
    text = '''
Привет, этот бот покажет статистику по контенту.
    '''
    return text



def get_start_keyboard():
    kb = ReplyKeyboardMarkup(
        keyboard=[
            [KeyboardButton(text="Мой контент"), KeyboardButton(text="Управление")],
            [KeyboardButton(text="Поддержать проект")]
        ],
        resize_keyboard=True,
        input_field_placeholder="Выберите действие…",
        one_time_keyboard=False
    )
    return kb


def donate_keyboard():
    builder = InlineKeyboardBuilder()
    builder.add(InlineKeyboardButton(text=f"Оплатить 100 ⭐️", pay=True))
    return builder.as_markup()

#Асинхронная функция для работы с бд
async def execute_query(query):
    # Подключение к БД
    conn = None
    try:
        conn = await asyncpg.connect(
            user=DB_USER,
            password=DB_PASSWORD,
            database=DATABASE,
            host= DATABASE_HOST,
            timeout=12
        )
        result = await conn.fetch(query)
        await conn.close()
        return result
    except TimeoutError:
        logger.info(f"{str(datetime.now())} ERROR. Could not connect to database due to timeout.")
    finally:
        if conn is not None:
            await conn.close()


#Асинхронная функция для работы с бд ()
async def execute_transactions_query(query):
    # Подключение к БД
    conn = None
    try:
        conn = await asyncpg.connect(
            user=DB_USER,
            password=DB_PASSWORD,
            database=DATABASE,
            host= DATABASE_HOST,
            timeout=12
        )
        result = await conn.execute(query)
        await conn.close()
        return result
    except TimeoutError:
        logger.info(f"{str(datetime.now())} ERROR. Could not connect to database due to timeout.")
    finally:
        if conn is not None:
            await conn.close()

# проверка, есть ли пользователь в таблицу users
async def is_activated(telegram_id):
    query = f"select * from users where telegram_id = '{telegram_id}';"
    res = await execute_query(query)
    if res == []:
        return False
    else:
        return True

def keyboard_for_management():
    builder = InlineKeyboardBuilder()
    builder.add(InlineKeyboardButton(text=f"Добавить видео",
                                     callback_data=f"video_add"))
    builder.add(InlineKeyboardButton(text=f"Удалить видео",
                                     callback_data=f"video_delete"))
    builder.adjust(2)
    return builder.as_markup()



async def get_user_id_from_tg_id(telegram_id):
    query = f"select * from users where telegram_id = '{telegram_id}';"
    res = await execute_query(query)
    user_id = res[0]['id']
    return user_id

async def check_user_url_pair(user_id, url_id):
    query = f"select * from users_urls where user_id = {user_id} and url_id = {url_id};"
    res = await execute_query(query)
    if res == []:
        return False
    else:
        return True

async def check_url_in_video_stats(url_id):
    query = f"select * from video_stats where url_id = {url_id};"
    res = await execute_query(query)
    if res == []:
        return False
    else:
        return True


async def common_stats_text(telegram_id):
    query = f"with t as (select telegram_id, url, platforms.name, video_stats.stats, video_stats.status, video_stats.id, video_stats.created_at from users_urls left join users on users_urls.user_id = users.id left join urls on users_urls.url_id = urls.id left join platforms on platforms.id = platform_id left join video_stats on video_stats.url_id  = users_urls.url_id where telegram_id = {telegram_id}) select name, sum(stats) from (SELECT name, url, stats, RANK() OVER (PARTITION BY name, url ORDER BY created_at DESC) AS rn FROM t) tmp where rn = 1 group by name;"
    query_res = await execute_query(query)
    if query_res != []:
        text = f"<b>Ваша агрегированная статистика:</b>\n\n"
        st = ""
        for rs in query_res:
            logger.info(rs)
            st+=f"<b>{rs['name']}:</b> {rs['sum']} просмотров.\n"
        text+=st
    else: text = 'У вас не добавлено ни одной ссылки.'
    return text

async def get_url_keyboard(telegram_id):
    query = f"with t as (select telegram_id, url, platforms.name, video_stats.stats, video_stats.status, video_stats.id as video_stats_id, video_stats.created_at from users_urls left join users on users_urls.user_id = users.id left join urls on users_urls.url_id = urls.id left join platforms on platforms.id = platform_id left join video_stats on video_stats.url_id  = users_urls.url_id where telegram_id = {telegram_id}) select url, max(video_stats_id) from t group by url;"
    res = await execute_query(query)
    builder = InlineKeyboardBuilder()
    for rs in res:
        builder.add(InlineKeyboardButton(text=f"{rs['url']}",
                                         callback_data=f"url_{rs['max']}")) # получаем id из таблицы статистики по названию, для точности будем пересчитывать при получении статистики
    builder.adjust(1)
    return builder.as_markup()

def depth_stat_text(actual_statistic): # actual_statistic - инфа о видео
    text = f"<b>Углубленная статистика по {actual_statistic['url']}:</b>\n\n<b>Количество просмотров:</b> {actual_statistic['stats']}\n<b>Статус:</b> {actual_statistic['status']}\n<b>Дата актуализации:</b> {actual_statistic['created_at']}"
    return text

async def get_url_keyboard_with_back(telegram_id):
    query = f"with t as (select telegram_id, url, platforms.name, video_stats.stats, video_stats.status, video_stats.id as video_stats_id, video_stats.created_at from users_urls left join users on users_urls.user_id = users.id left join urls on users_urls.url_id = urls.id left join platforms on platforms.id = platform_id left join video_stats on video_stats.url_id  = users_urls.url_id where telegram_id = {telegram_id}) select url, max(video_stats_id) from t group by url;"
    res = await execute_query(query)
    builder = InlineKeyboardBuilder()
    for rs in res:
        builder.add(InlineKeyboardButton(text=f"{rs['url']}",
                                         callback_data=f"url_{rs['max']}")) # получаем id из таблицы статистики по названию, для точности будем пересчитывать при получении статистики
    builder.add(InlineKeyboardButton(text=f"⬅️ Назад", callback_data=f"url_back"))
    builder.adjust(1)
    return builder.as_markup()


async def deleter_keyboard(telegram_id):
    query = f"select users_urls.id, url from users_urls left join users on users.id = users_urls.user_id left join urls on urls.id = users_urls.url_id where users.telegram_id = {telegram_id};"
    res = await execute_query(query)
    builder = InlineKeyboardBuilder()
    for rs in res:
        builder.add(InlineKeyboardButton(text=f"{rs['url']}",
                                         callback_data=f"deleter_{rs['id']}")) # получаем id из таблицы статистики по названию, для точности будем пересчитывать при получении статистики
    builder.adjust(1)
    return builder.as_markup()

async def deleter_keyboard_with_back(telegram_id):
    query = f"select users_urls.id, url from users_urls left join users on users.id = users_urls.user_id left join urls on urls.id = users_urls.url_id where users.telegram_id = {telegram_id};"
    res = await execute_query(query)
    builder = InlineKeyboardBuilder()
    for rs in res:
        builder.add(InlineKeyboardButton(text=f"{rs['url']}",
                                         callback_data=f"deleter_{rs['id']}")) # получаем id из таблицы статистики по названию, для точности будем пересчитывать при получении статистики
    builder.add(InlineKeyboardButton(text=f"⬅️ Назад", callback_data=f"deleter_back"))
    builder.adjust(1)
    return builder.as_markup()