package ru.catssoftware;

import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import ru.catssoftware.gameserver.model.actor.instance.L2PcInstance;

import java.io.File;
import java.util.Map;

public class Message {
	public static enum MessageId {
		
		MSG_CHAR_SUCCESS_BAN("Персонаж %s успешно заблокирован."),
		MSG_CHAR_SUCCESS_UNBAN("Персонаж %s успешно разблокирован."),
		MSG_ACC_RECIVE_GM_ACCESS("Аккаунт %s получил права GM'a."),
		MSG_CHAT_BLOCK_FOR("Заблокирован чат игроку %s."),
		MSG_YOUR_CHAT_BLOCK_FOR("Вам заблокирован чат %s."),
		MSG_CHAT_UNBLOCK("Снята блокировка чата игроку: %s."),
		MSG_YOUR_CHAT_UNBLOCK("Вам снята блокировка игрового чата."),
		MSG_ALL_CHAT_BLOCK("Все чаты отключены. Таймер: %s минут."),
		MSG_ALL_CHAT_UNBLOCK("Работа чатов возобновлена."),
		MSG_BAN_ACC_BREAK_RULE("Забанен аккаунт %s. Нарушение правил."),
		MSG_UNBAN_ACC("Аккаунт %s успешно разбанен."),
		MSG_SEND_IN_JAIL("Отправлен в тюрьму игрок %s."),
		MSG_ADMIN_PUT_YOU_IN_JAIL("Вы отправлены в тюрьму %s."),
		MSG_SET_ALL_ARG("Аргументы заданы не верно."),
		MSG_CHAR_NOT_FOUND_IN_BASE("Игрок не найден в базе."),
		MSG_CHAR_NOT_FOUND("Игрок не найден."),
		MSG_RELEASE_FROM_JAIL("Игрок %s освобожден из тюрьмы."),
		MSG_YOU_RELEASED_FROM_JAIL("Вы освобождены из тюрьмы."),
		MSG_TARGET_NOT_FOUND("Цель не определена."),
		MSG_NOT_ALLOWED_AT_THE_MOMENT("Недоступно в данный момент."),
		MSG_CANNOT_FIND_RECIPIENT("Невозможно найти %s, поэтому он не получит письмо."),
		MSG_INCOMING_MAILBOX_FULL("%s,входящая почта переполнена."),
		MSG_TARGET_IS_IN_JAIL("Игрок в тюрьме."),
		MSG_TARGET_CHAT_BLOCK("Чат игрока заблокирован."),
		MSG_YOU_ARE_IN_JAIL("Вы находитесь в тюрьме."),
		MSG_TARGET_IS_AWAY("%s отошел, попробуйте позже."),
		MSG_NO_SPAWN_FOUND("Подобный спаун не обнаружен."),
		MSG_INSUFFICIENT_RIGHT("Недостаточно прав, доступ запрещен."),
		MSG_GM_COMMAND_NOT_EXIST("Команда: '%s' не существует."),
		MSG_NO_RIGHTS_USE_COMMAND("Команда: '%s' для Вас запрещена."),
		MSG_LOST_GM_RIGHTS("Вы потеряли права администратора."),
		MSG_EARN_GM_RIGHTS("Ваши права GM'a обновлены."),
		MSG_ERROR_TRY_LATER("Произошла ошибка, попробуйте снова."),
		MSG_HERO_CHAT_ONCE_PER_TEN_SEC("Запрещено. Герой может писать в чат Героя только 1 раз в 10 секунд."),
		MSG_CHAT_FLOOD_PROTECT("Защита от флуда: сообщение заблокировано."),
		MSG_CHAR_BUSY_TRY_LATER("Игрок занят. Попробуйте позже."),
		MSG_CURRENT_IN_COMBAT("Вы заняты сражением."),
		MSG_YOU_OPEN("Вы открытли %s."),
		MSG_YOU_FAILD_OPEN("Вы не открыли %s."),
		MSG_DOOR_OPEN("Вы открыли дверь!"),
		MSG_ACTION_NOT_ALLOWED_DURING_SHUTDOWN("Это действие запрещенно во время рестарта."),
		MSG_MERCHANT_FUNC_ONLY_IN_CASTLE("Торговец предлагает услуги только в замке %s."),
		MSG_WRONG_PUMPKIN("Вы неможете поливать чужие тыквы."),
		MSG_SQUASH_SUCCESS_GROW("Вы успешно вырастили тыкву."),
		MSG_RECIPE_ADD_DWARV("Добавлен рецепт '%s' в книгу рецептов гномов."),
		MSG_RECIPE_ADD_COMMON("Добавлен реуепт '%s' в общую книгу рецептов."),
		MSG_CANNOT_USE("Вы не можете использовать %s."),
		MSG_MISER_CONSUME("СА в оружии позволило использовать только %d соулшутов."),
		MSG_CANNOT_GROW_PUMPKIN("Вы неможете вырастить тыкву."),
		MSG_CANNOT_OPEN_PORTAL("Здесь запрещено открывать проход."),
		MSG_USE_BLOCK_BUFF("Используется блокировка бафа."),
		MSG_BLOCK_BUFF("Блокировка баффов %s"),
		MSG_CANNOT_REMOVE_WEAPON("Вы не можете снять оружие во время каста."),
		MSG_FISHING_IS_NOT_ALLOWED("Рыбалка отключена."),
		MSG_YOU_CANNOT_ESCAPE("Вы не можете убежать."),
		MSG_YOU_USE_ESCAPE("Вы используете Escape: %s."),
		MSG_RESTART_POINT_AT("Точка старта в %s"),
		MSG_NO_PARTY("Вы не в группе."),
		MSG_PARTY_MEMBERS_COUNT("Участники: %s/9"),
		MSG_NOT_ACCESSABLE("Недоступно."),
		MSG_HWID_REC("Привязка аккаунта по HWID: %s"),
		MSG_IP_REC("Привязка аккаунта по IP: %s"),
		MSG_FORBIDEN_BY_ADMIN("Запрещено администратором."),
		MSG_ONLY_IN_PEACE_ZONE("Разрешено только в мирной зоне."),
		MSG_CURRENT_LANG("Текущий язык: %s"),
		MSG_MAMMON_SEARCH_OFF("В данный период Семи Печатей поиск отключен."),
		MSG_MAMMON_TRADER("Маммон Торговец: %s"),
		MSG_MAMMON_BLACKSMITH("Маммон Кузнец: %s"),
		MSG_SERVICE_NEED_TO_ACTIVE("Для включения сервиса необходимо:"),
		MSG_OFFLINE_TRADE_NEED("- находится в режиме торговли."),
		MSG_OFFLINE_CRAFT_NEED("- находится в режиме производства."),
		MSG_NO_PREMIUM("Ваш аккаунт не имеет Премиум Статуса."),
		MSG_NO_PARTNER("Вы не имеете партнера."),
		MSG_PARTNER_ASK_DIVORCE("Ваш партнер подал на развод."),
		MSG_HAVE_PARTNER("Вы уже обручены."),
		MSG_YOU_DIVORCED("Вы разведены."),
		MSG_TARGET_IS_MARIED("Игрок уже обручен."),
		MSG_PARTNER_MUST_BE_FRIEND("Игрок не найден в вашем списке друзей."),
		MSG_ASK_YOU_ENGAGE(" делает вам предложение обручится. Вы согласны?"),
		MSG_ERROR_CONTACT_GM("Ошибка! Обратитесь к GM."),
		MSG_PARTNER_NOT_AVAILABLE("Партнер сейчас не доступен."),
		MSG_NOT_REAR_FLAG("Вы находитесь не у флага, ваше участие в осаде невозможно!"),
		MSG_WRONG_CLAN_LEVEL("Только кланы достигшие %s-го уровня и выше могут принимать участие в осаде."),
		MSG_CLAN_AUTO_REGISTERED("Клан владеющий Холлом Клана автоматически регистрируеться на осаду."),
		MSG_RAINBOW_GO_TO_ARENA("Ваш Клан принимает участие в соревновании. Пройдите на арену."),
		MSG_NO_QUEST_ITEMS("Недостаточно квестовых предметов."),
		MSG_RAINBOW_GIVE_DAMMAGE("Вы нанесли урон тыкве."),
		MSG_RAINBOW_HEAl("Вы восстановили здоровье своей тыкве."),
		MSG_RAINBOW_DEBAFF("На вашего противника наложен отридцательный эффект."),
		MSG_RAINBOW_HEAL_OTHER("Вы восстановили здоровье тыквы противника."),
		MSG_FISHCHAMP_BETTER_RESULT("Вы улучшили свой результат в Королевском Турнире рыболовов."),
		MSG_FISHCHAMP_WINER_LIST("Вы попали в список призеров Королевского Турнира рыболовов."),
		MSG_BAD_CONDITIONS("Вы не соответствуете требованиям"),
		MSG_LIT_NO_ENTRY("Вход сейчас не возможен."),
		MSG_LIT_OTHER_GROUP_INSIDE("Другая група уже находится внутри Imperial Tomb."),
		MSG_LIT_NOT_COMMANDER_OR_NO_SCROOL("Вы не командир командного канала или у вас нету \"Frintezza's Magic Force Field Removal Scroll\"."),
		MSG_LIT_UNREGISTERED("Предупреждение: Вы были сняты с регистрации."),
		MSG_LIT_NO_SCROOL("У вас нету \"Frintezza's Magic Force Field Removal Scroll"),
		MSG_ALREADY_REGISTERD("Вы уже зарегистрированы."),
		MSG_CONDITIONS_NOT_MET("Условия не соблюдены."),
		MSG_PETITION_REJECTED("Петиция отклонена, попробуйте позже."),
		MSG_BYSE_BECOUSE_SIEGE("Невозможно во время осады."),
		MSG_AUCTION_ERROR("Ошибка аукциона!"),
		MSG_AUCTION_TIME_END("Время аукциона истекло!"),
		MSG_LOW_CLAN_LEVEL("Уровень вашего клана должен быть %s или выше."),
		MSG_NO_MORE_BID("Вы неможете делать более 1-й ставки."),
		MSG_CANCEL_AUCTION("Аукцион отменен."),
		MSG_NO_RIGHTS_TO_CONTROLL_CASTLE("У Вас нет прав на управление замком."),
		MSG_WRONG_SKILL_LEVEL_CONTACT_ADMINISTRATOR("Недействительный уровень навыка, свяжитесь с Администратором!"),
		MSG_CANT_CHANGE_SIEGE_TIME("Изменение времени осады запрещено."),
		MSG_HERE_TRAP("Здесь ловушка."),
		MSG_NO_RIGHTS_TO_CONTROLL_HALL("У Вас нет прав на управление кланхолом."),
		MSG_CURSED_WEAPON_NOT_ALLOW("Запрещено с проклятым оружием."),
		MSG_NO_REGISTER_WHILE_EVENT("Вы неможете зарегистрироваться во время проведения эвента."),
		MSG_CANT_REGISTER_SCORE_WHILE_EVENT("Вы неможете зарегистрировать результат во время проведения фестиваля."),
		MSG_NO_BLOOD_OFFERINGS("У Вас нет Жертвенной крови чтобы сделать взнос."),
		MSG_NO_PRISE_TIL_EVENT_ACTIVE("Вы неможете получить приз в период проведения соревнований."),
		MSG_CANT_LEAVE_FESTIVAL("Только лидер группы может покинуть фестиваль, если соблюдено требование минимального количества участников."),
		MSG_NO_RIGHTS_TO_CONTROLL_FORT("У Вас нет прав на управление фортом."),
		MSG_REGISTERD_ON_FORT_SIEGE("Зарегистрированны для осады форта %s."),
		MSG_PET_UNSUMMON_IN_MINUTES("Питомец покинет Вас через %s мин."),
		MSG_FEED_TIME_END("Время использования питомца истекло."),
		MSG_JAIL_POINTS_RESET("Ваши очки штрафа были сброшены."),
		MSG_GO_AWAY("Уйдите, вам тут не рады!"),
		MSG_SKILLS_LEARN("Вы выучили %s скилов."),
		MSG_USE_BACK_COMMAND("Для отключение афк режима введите команду .back!"),
		MSG_CANT_TARGET_WHEN_TARGET_IN_SIEGE("Игрок участвует в осаде, выбор цели невозможен."),
		MSG_EARN_PVP_BONUS("Вы получили награду за убийство в PVP."),
		MSG_SKILL_REMOVED_ADMIN_INFORMED("Удалено скилов (%s). GM проинформирован!"),
		MSG_WRONG_CLASS("Неверный класс!"),
		MSG_REMOVE_FROM_PARTY_BIG_LVL_DIF("Вы удалены из группы из-за большой разницы в уровне."),
		MSG_ENTER_IN_INVUL_MODE("Вход в мир неуязвимым."),
		MSG_ENTER_IN_INVIS_MODE("Вход в мир невидимым."),
		MSG_ENTER_IN_REFUS_MODE("Вход в мир в режиме отказа сообщений."),
		MSG_CANT_EXIT("Нельзя покинуть игру."),
		MSG_REQUEST_OK("Ваше предложение принято."),
		MSG_REQUEST_CANCELED("Ваше предложение отвергнуто."),
		MSG_YOU_JAILED_FOR_X_MINUTES("Вы отправлены в тюрьму на %s минут."),
		MSG_YOU_JAIL_StATUS_UPDATED("Ваш срок заключение обновлен на %s минут."),
		MSG_OFFLINE_MODE_ON("Offline режим: включен!"),
		MSG_FAME_RECIVED("Вы получили очки репутации."),
		MSG_NO_NEW_MESSAGE("У вас нет непрочитанных сообщений."),
		MSG_YOU_ARE_BANNED("Администратор заблокировал вашего персонажа."),
		MSG_PET_IS_TO_HUNGRY("Питомец не может этого сделать т.к. очень голоден."),
		MSG_NOT_IN_SIEGE_ZONE("Сумон удален, т.к. это не осадная зона."),
		MSG_WAIT_CHANGE_MODE_END("Дождитесь окончания смены осадного режима."),
		MSG_CHANGE_MODE_CANCEL("Смена осадного режима отменена."),
		MSG_CHANGE_MODE("Смена осадного режима начата."),
		MSG_CHANGE_MODE_END("Осадный режим изменен."),
		MSG_SPAWN_PROTECTION("You have %s seconds till spawn protection ends."),
		MSG_YOU_IN_OLYMPIAD("Вы зарегистрированы на олимпиаде"),
		MSG_UNSUMMON_YOUR_PET("Отзовите вашего питомца."),
		MSG_MAX_SUB_CLASS("Достигнут лимит суб-классов."),
		MSG_NO_SUB_CLASS("Нет доступных суб-классов."),
		MSG_NEED_ITEM("Вы должны иметь %s в инвентаре."),
		MSG_CANT_ADD_SUB("Суб-класс не может быть добавлен."),
		MSG_UNRIDE_PET("Слезте с вашего питомца."),
		MSG_CANT_GIVE_TO_ACADEM("Нельзя передать право лидерства в академию."),
		MSG_NOT_FOUND_IN_CLAN("%s не найден в вашем клане!"),
		MSG_NOW_YOU_MARIED("Поздравляем, вы состоите в браке!"),
		MSG_PARTNER_DECLINE("Ваш партнер отказался."),
		MSG_WRONG_STRIDER_LEVEL("Страйдер не достиг нужного уровня."),
		MSG_BAD_COORDINATS("Ошибка координат, свяжитесь с ГМ!"),
		MSG_ACTION_PET_LEVEL_UP("Уровень питомца увеличен."),
		MSG_ALREADY_IGNORED("%s уже игнорируется."),
		MSG_NOT_IN_IGNOR_LIST("%s отсутствует в списке игнорируемых."),
		MSG_CANT_PICK_UP_WHILE_RIDING("Вы не можете поднять итем пока находитесь верхом."),
		MSG_YOU_OUT_BIDDED("Вашу ставку перебили."),
		MSG_YOU_WIN_AUCTION("Поздравляем! Вы выиграли аукцион на Холл клана!"),
		MSG_CASTLE_TAX_RANGE("Налог может быть изменен с 0 до %s."),
		MSG_CASTLE_CHANGE_TAX("Налог будет изменен на %s %%."),
		MSG_DUEL_CANCELED_DUE_PVP("Дуэль отменена т.к. противник вошел в PVP режим."),
		MSG_EVENT_CTF_TAKE_FLAG_TO_BASE("Отнесите флаг на свою базу."),
		MSG_EVENT_REGISTERED("Вы зарегистрированы на эвенте %s."),
		MSG_EVENT_CANT_REGISTERED("Ваше участие на эвенте невозможно"),
		MSG_EVENT_CANCEL_REG("Ваше участие на эвенте %s отменено."),
		MSG_EVENT_NOT_REGISTERED("Вы не участник эвента."),
		MSG_EVENT_WAIT_FOR_RES("Вы убиты, дождитесь воскрешения."),
		MSG_EVENT_YOU_KILL_TEAM_MEMBER("Вы убили члена своей команды."),
		MSG_EVENT_YOU_KILL_REGULAR_PLAYER("Вы убили простого игрока."),
		MSG_EVENT_CANT_USE_ITEM("Вы не можете использовать этот предемет на эвенте."),
		MSG_EVENT_SKILL_NOT_ALOWED("Это умение запрещено на эвенте."),
		MSG_OLY_SKILL_NOT_ALOWED("Это умение запрещено на олимпиаде."),
		MSG_EVENT_NOT_ALLOWED("Извините, эвент не доступен."),
		MSG_EVENT_ALREADY_REGISTERED("Вы уже зарегистрированы на эвент."),
		MSG_EVENT_HWID_ALREADY_REGISTERED("Игрок с вашего компьютера уже зарегистрирован."),
		MSG_EVENT_FULL("Все места на эвент заняты."),
		MSG_EVENT_WRONG_LEVEL("Ваш уровень не подходит для участия."),
		MSG_EVENT_RECIVE_KILL_BONUS("Вы получили награду за убийство."),
		MSG_NO_QUEST("<html><body>Вы не соответствуете минимальным требованиям NPC или Квеста. Возможно вы ошиблись, когда обратились к данному NPC.</body></html>"), 
		MSG_OLY_END("Период Великой Олимпиады завершен."), 
		MSG_OLY_NEW_PERIOD("Новый период Великой Олимпиады запущен."), 
		MSG_POINTS_REQUIRE("Вы не можете принять участие, необходимо иметь %d очка."), 
		MSG_YOU_CANT_DOIT_AT_THIS_TIME("В настоящее время это сделать невозможно."), 
		MSG_RELEASE_PET("Освободите слугу или питомца"), 
		MSG_OLY_PREPARE("Подготовка"), 
		MSG_OLY_SAME_IP("Матч прерван, т.к. у обоих участников совпадает IP-адресс."), 
		MSG_OLY_INGAME("В игре "),
		MSG_OLY_WAIT("Ожидание "), 
		MSG_REG_FAIL("Ваша регистрация отменена из-за непредвиденной ошибки."),
		MSG_FORT_ATTACKED("На вашу крепость напали!"),
		MSG_NOT_ENOUGH_CLAN_MEMBERS("Для участия в атаке в клане должно быть %s или больше членов клана."),
		MSG_JAIL_POINT_EARNED("Вы получили %s тюремных очков."),
		MSG_JAIL_POINTS_ALL("Вы получили все очки, поговорите с менеджером."),
		MSG_JAIL_POINT_LOST("Вы потеряли %s тюремных очков после смерти."),
		MSG_CW_YOUR_LVL_LOW("Атака данного игрока разрешена после 20 уровня."),
		MSG_CW_TARGET_LVL_LOW("Атака новичков данным оружием запрещена."),
		MSG_FACTION_CANT_CHANGE_TITLE("Faction System: титул запрещен."),
		MSG_TARGET_RECIVE_SPECIAL_EFFECT("Цель получает специальное умение оружия!"),
		MSG_EVT_1("%s: Открыта регистрация."),
		MSG_EVT_2("%s: Разница уровней %d-%d."),
		MSG_EVT_3("%s: Награды:"),
		MSG_EVT_4("%s: Начало через %d минут"),
		MSG_EVT_5("%s: До конца регистрации %d минут"),
		MSG_EVT_6("%s: До конца регистрации меньше минуты"),
		MSG_EVT_7("%s: Победитель - команда %s"),
		MSG_EVT_8("%s: Победитель не определен"),
		MSG_EVT_9("%s: Недостаточно игроков"),
		MSG_EVT_10("%s: Игра началась!"),
		MSG_EVT_11("%s: Победитель - игрок %s"),
		MSG_EVT_12("Убийств %d"),
		MSG_YOU_CANT_PICKUP_MERCH_IN_PARTY("You cannot pickup mercenaries while in a party."),
		MSG_CANT_CREATE_SUB_UNITS("You can't create any more sub-units of this type."),
		MSG_LEADER_IS_INCORECT("Leader is not correct"),
		MSG_CREATE_ALLIANCE("Alliance %s has been created."),
		MSG_LVL_DIFF_TO_HIGH_TO_INVITE("Level difference too high, you can't invite this player."),
		MSG_LVL_DIFF_TO_HIGH_TO_JOIN("Level difference too high, you can't join this party."),
		MSG_SKILL_NOT_IMPLEMENTED("Skill not implemented. Skill ID: %s"),
		MSG_WEAPON_MAY_USE_WITH("может использоватся с"),
		MSG_YOU_NOT_PET_OWNER("Вы не хозяин этого питомца"),
		MSG_COMMAND_IS_NULL("Command is null..."),
		MSG_MAP_NOT_ALLOWED("Карта недоступна"),
		MSG_NO_CHAT("Вы вошлу в зону, где чат запрещен."),
		MSG_ENTER_TRADE_ZONE("Вы вошли в зону торговли."),
		MSG_CANT_CREATE_INSTANCE("Выбарнный инстанс не может быть создан."),
		MSG_MAP_ON("Карта снова доступна"),
		MSG_EXIT_TRADE_ZONE("Вы вышли из зоны торговли."),
		MSG_PREMIUM_ON("Премиум сервис включен"),
		MSG_YOU_ARE_HERO_NOW("Вы получили статус Героя."),
		MSG_SEX_CHANGE("Ваш пол успешно сменен."),
		MSG_WRONG_TITLE("Неверный титул, попробуйте заново."),
		MSG_NICK_EXIST("Этот ник уже занят"),
		MSG_WRONG_NICK("Недопустимый ник"),
		MSG_NICK_CHANGED("Ваш ник успешно изменен."),
		MSG_ILLEGAL_ACTION_KICK("Вы удалены из игры за нарушение. Администратор проинформирован."),
		MSG_ILLEGAL_ACTION_BAN("Вы удалены из игры и забанены за нарушение. Администратор проинформирован."),
		MSG_ILLEGAL_ACTION_JAIL("Вы осуждены за нарушение, перемещены в место отбывания наказания."),
		MSG_SKILL_CHANS_SUCCES("Шанс прохождения: %s %%, успешно."),
		MSG_SKILL_CHANS("Шанс прохождения: %s %%."),
		MSG_YOU_WILL_MOVED_IN_TOWN("Вы будете перемещены в ближайший город"),
		
		ANNOUNCE_CHAR_BLOCKED("Игроку %s заблокирован доступ на сервер"),
		ANNOUNCE_CHAR_UN_BLOCKED("Игроку %s разрешен доступ на сервер"),
		ANNOUNCE_CHAR_CHAT_BLOCK("Забанен чат игроку %s на %d минут."),
		ANNOUNCE_CHAR_CHAT_UNBLOCK("Игроку %s снята блокировка чата."),
		ANNOUNCE_BAN_REASON("Причина бана: %s."),
		ANNOUNCE_CHAT_BLOCK_WITH_REASON("Игроку %s забанен чат на %d минут, причина: %s."),
		ANNOUNCE_CHAT_BLOCK_FOREVER_WITH_REASON("Игроку %s забанен чат бессрочно, причина: %s."),
		ANNOUNCE_ADMIN_BLOCK_ALL_CHAT("Все чаты отключены администрацией на %s минут."),
		ANNOUNCE_ADMIN_UNBLOCK_ALL_CHAT("Работа всех чатов возобновлена."),
		ANNOUNCE_ACCOUNT_BLOCK("Забанен аккаунт %s. Нарушение правил"),
		ANNOUNCE_ACCOUNT_UNBLOCK("Аккаунт %s разблокирован."),
		MSG_CANT_USE_SUMMON_IN_NONSUMMON_ZONE("Вы не можете вызывать игрока, находясь в зоне с запрещенным суммоном."),
		ANNOUNCE_CHAR_JAIL("Игрок %s отправлен в тюрьму %s"),
		MSG_EVT_DAY1("Эвент L2Day запущен."),
		MSG_EVT_DAY2("Соберите: Lineage II или CHRONICLE или THRONE и получите награду!"),
		MSG_EVT_CHRISTMAS1("Эвент Cristmas запущен."),
		MSG_EVT_CHRISTMAS2("Помогите Санта-Клаусу и получите подарки!"),
		MSG_EVT_MEDALS1("Эвент Медали Бехтеля запущен."),
		MSG_EVT_MEDALS2("Охотьтесь на монстров и собирайте медали! Медали обменивайте на призы!"),
		MSG_EVT_STAR1("Эвент Фестиваль Звездного Света запущен."),
		MSG_EVT_STAR2("Собирайте осколки звезд, охотясь на монстров, и обменивайте их а призы!"),
		MSG_EVT_SQUASH1("Эвент Большие тыквы запущен."),
		MSG_EVT_SQUASH2("Выращивайте тыквы и получайте награду!");

		String _msg;
		MessageId(String msg) {
			_msg = msg;
		}
		public String getText() {
			return _msg;
		}
		
	}
	private static Map<String, Map<MessageId,String>> _messages = new FastMap<String, Map<MessageId,String>>();
	
	public Message() {

	}

	public static void load() {
		File lngDir = new File(Config.DATAPACK_ROOT,"data/html");
		for(File f : lngDir.listFiles())
			if(f.getName().endsWith(".msg")) { // Держим сообщения в en.msg ru.msg и т.д, переделаю на работу с HTMCache
				loadMessageFile(f);
			}
	}

	private static void loadMessageFile(File f) {
		String lang = f.getName().substring(0,2);
		Map<MessageId,String> msgList = _messages.get(lang);
		if(msgList==null) {
			msgList = new FastMap<MessageId, String>();
			_messages.put(lang,msgList);
		}
		try {
			LineIterator it = FileUtils.lineIterator(f, "UTF-8");
			String line;
			while((line = it.nextLine())!=null) {
				if(line.length()==0 || line.startsWith("#"))
					continue;
				String args[] = line.split("=");
				try {
					MessageId msgId = MessageId.valueOf(args[0].trim()); // Обрезаем пробелы с обеих сторон
					msgList.put(msgId, args[1]);
				} catch(Exception e) {
					// Если указана фигня -то скипакем
				}
			}
		} catch(Exception e) { 
			
		}
	}
	
	public static String getMessage(L2PcInstance player, MessageId msgId) {
		String lang = player == null?"en":player.getLang();
		Map<MessageId,String> msgList = _messages.get(lang); 
		if(msgList==null)
			return msgId.getText();
		if(!msgList.containsKey(msgId))
			return msgId.getText();
		return  msgList.get(msgId);
		
	}
}
