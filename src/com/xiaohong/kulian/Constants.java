/*
 * Copyright (C) 2016 Shanghai Xiaohong.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaohong.kulian;

/**
 * 全局常量
 * 
 * @author andrew.wang
 * @date 2011-4-20
 * @since Version 0.7.0
 * 
 */
public interface Constants {

    /** Google统计ID */
    static final String GOOGLE_UID = "UA-17683102-4";

    /** Gfan统计格式 */
    static final String STATISTICS_FORMAT = "%s - %s";

    /** 机锋市场根目录 */
    static final String ROOT_DIR = "kulian";

    /** 机锋市场缓存目录 */
    static final String IMAGE_CACHE_DIR = ROOT_DIR + "/.cache";
    static final String IMAGE_LOADER_CACHE_DIR = ROOT_DIR + "/img";


    /** 截屏图片缓存目录 */
    static final String IMAGE_SNAPSHOT_DIR = ROOT_DIR + "/.snapshot";

    /** 推荐分类 */
    static final String CATEGORY_RCMD = "rcmd";
    /** 任务分类 */
    static final String CATEGORY_TASK = "task";
    /** 应用分类 */
    static final String CATEGORY_APP = "app";
    /** 游戏分类 */
    static final String CATEGORY_GAME = "game";
    /** 主题分类 */
    static final String CATEGORY_THEME = "theme";
    /** 电子书分类 */
    static final String CATEGORY_EBOOK = "ebook";
    /** 增长最快分类 */
    static final String CATEGORY_GROW = "grow";
    /** 谷歌市场产品 */
    static final String SOURCE_TYPE_GOOGLE = "1";
    /** 机锋市场产品 */
    static final String SOURCE_TYPE_KULIAN = "0";
    /** 产品收费的类型：免费 */
    static final int PAY_TYPE_FREE = 1;
    /** 产品收费的类型：收费 */
    static final int PAY_TYPE_PAID = 2;
    /** 排序的类型：下载次数 */
    static final int ORDER_TYPE_DOWNLOAD = 1;
    /** 排序的类型：发布时间 */
    static final int ORDER_TYPE_TIME = 2;
    /** 排序的类型：装机量 */
    static final int ORDER_TYPE_INSTALLED_NUM = 3;

    /** 状态 -- 未下载 */
    static final int STATUS_NORMAL = 0;
    /** 状态 -- 准备开始下载 */
    static final int STATUS_PENDING = 1;
    /** 状态 -- 已安装 */
    static final int STATUS_INSTALLED = 11;
    /** 状态 -- 已下载未安装 */
    static final int STATUS_DOWNLOADED = 9;
    /** 状态 -- 可更新 */
    static final int STATUS_UPDATE = 10;

    /** 搜索结果 -- 市场部分 */
    static final int SEARCH_TYPE_MARKET = 0;
    /** 搜索结果 -- 社区部分 */
    static final int SEARCH_TYPE_BBS = 1;
    /** 购买产品 */
    static final String IS_BUY = "is_buy";

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>> Intent Extra Message Field
    /** 分类信息 */
    static final String EXTRA_CATEGORY = "extra.category";
    /** 包名信息 */
    static final String EXTRA_PACKAGE_NAME = "extra.key.package.name";
    /** 产品ID信息 */
    static final String EXTRA_PRODUCT_ID = "extra.key.pid";
    /** 产品来源信息（机锋市场/谷歌市场） */
    static final String EXTRA_SOURCE_TYPE = "extra.key.source.type";
    /** 产品详细信息 */
    static final String EXTRA_PRDUCT_DETAIL = "extra.product.detail";
    /** 产品截图ID信息 */
    static final String EXTRA_SCREENSHOT_ID = "extra.screenshot.id";
    /** 产品排序信息 （按时间或者下载量） */
    static final String EXTRA_SORT_TYPE = "extra.order";
    /** 产品分类ID */
    static final String EXTRA_CATEGORY_ID = "extra.category.id";
    /** 搜索结果页分类 */
    static final String EXTRA_SEARCH_TYPE = "extra.search.type";
    /** 首页预加载内容 */
    static final String EXTRA_HOME_DATA = "extra.home.data";
    /** 首页预加载内容（顶部） */
    static final String EXTRA_HOME_DATA_TOP = "extra.home.data.top";
    /** 首页预加载内容（底部） */
    static final String EXTRA_HOME_DATA_BOTTOM = "extra.home.data.bottom";
    /** 加载最大值 */
    static final String EXTRA_MAX_ITEMS = "extra.max.items";
    
    /** app奖励的金币数目 */
    static final String EXTRA_COIN_NUM = "extra.coin.num";
    
    static final String EXTRA_TASK_BEAN = "extra.task.bean";

    static final int INFO_UPDATE = 0;
    static final int INFO_REFRESH = 1;

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    /** SubTitle Keyword */
    static final String KEY_PLACEHOLDER = "place_holder";

    /*
     * Product Item Information
     */
    static final String KEY_PRODUCT_ID = "p_id";

    static final String KEY_PRODUCT_NAME = "name";

    static final String KEY_PRODUCT_TYPE = "product_type";

    static final String KEY_PRODUCT_SUB_CATEGORY = "sub_category";

    static final String KEY_PRODUCT_IS_STAR = "is_star";

    static final String KEY_PRODUCT_CATEGORY = "product_category";

    static final String KEY_PRODUCT_DESCRIPTION = "product_description";

    static final String KEY_PRODUCT_PRICE = "price";

    static final String KEY_PRODUCT_SIZE = "app_size";

    static final String KEY_PRODUCT_RATING = "rating";

    static final String KEY_PRODUCT_DOWNLOAD_URI = "url";

    static final String KEY_PRODUCT_PAY_TYPE = "pay_category";

    static final String KEY_PRODUCT_SHORT_DESCRIPTION = "short_description";

    static final String KEY_PRODUCT_SOURCE_TYPE = "source_type";

    static final String KEY_PRODUCT_AUTHOR = "author_name";

    static final String KEY_PRODUCT_DOWNLOAD = "product_download";

    static final String KEY_PRODUCT_ICON_URL = "icon_url";

    static final String KEY_PRODUCT_ICON_URL_LDPI = "ldpi_icon_url";

    static final String KEY_PRODUCT_APK_URL = "apk_url";
    
    static final String KEY_PRODUCT_PACKAGE_NAME = "packagename";

    static final String KEY_PRODUCT_VERSION_NAME = "version_name";

    static final String KEY_PRODUCT_VERSION_CODE = "version_code";

    static final String KEY_PRODUCT_COMMENTS_COUNT = "comments_count";

    static final String KEY_PRODUCT_RATING_COUNT = "ratings_count";

    static final String KEY_PRODUCT_DOWNLOAD_COUNT = "download_count";

    static final String KEY_PRODUCT_LONG_DESCRIPTION = "long_description";

    static final String KEY_PRODUCT_PUBLISH_TIME = "publish_time";

    static final String KEY_PRODUCT_MD5 = "filemd5";

    static final String KEY_PRODUCT_UP_REASON = "up_reason";

    static final String KEY_PRODUCT_UP_TIME = "up_time";

    static final String KEY_PRODUCT_PERMISSIONS = "uses_permission";

    static final String KEY_PRODUCT_IS_INSTALLED = "is_installed";

    static final String KEY_PRODUCT_STATUS = "status";

    static final String KEY_PRODUCT_IS_RECOMMEND = "product_is_recommend";

    static final String KEY_PRODUCT_ICON = "icon_url";

    static final String KEY_PRODUCT_INFO = "info";

    static final String KEY_PRODUCT_PUBLIC_TIME = "public_time";

    static final String KEY_PRODUCT_SCREENSHOT_1 = "screenshot_1";
    static final String KEY_PRODUCT_SCREENSHOT_2 = "screenshot_2";
    static final String KEY_PRODUCT_SCREENSHOT_3 = "screenshot_3";
    static final String KEY_PRODUCT_SCREENSHOT_4 = "screenshot_4";
    static final String KEY_PRODUCT_SCREENSHOT_5 = "screenshot_5";

    static final String KEY_PRODUCT_SCREENSHOT_LDPI_1 = "screenshot_1";
    static final String KEY_PRODUCT_SCREENSHOT_LDPI_2 = "screenshot_2";
    static final String KEY_PRODUCT_SCREENSHOT_LDPI_3 = "screenshot_3";
    static final String KEY_PRODUCT_SCREENSHOT_LDPI_4 = "screenshot_4";
    static final String KEY_PRODUCT_SCREENSHOT_LDPI_5 = "screenshot_5";

    // 个人中心页
    static final String HEADER_ITEM = "header";
    static final String ACCOUNT_ICON = "icon";
    static final String ACCOUNT_TITLE = "title";
    static final String ACCOUNT_DESC = "desc";
    static final String ACCOUNT_TIME = "time";
    static final String ACCOUNT_DOWNLOAD = "download";
    static final String ACCOUNT_TYPE = "account_type";
    static final String ACCOUNT_ARROW = "arrow";
    static final int FLAG_GROUP_ITEM = 8;
    static final int FLAG_HEADER_ITEM = 9;
    // 没有支付记录
    static final int FLAG_NO_PAY_LOG_ITEM = 10;

    static final String KEY_PRODUCT_LIST = "product_list";

    static final String KEY_PRODUCT = "product";

    static final String KEY_PRODUCTS = "products";

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    static final String KEY_CATEGORY_ID = "category_id";

    static final String KEY_CATEGORY_NAME = "category_name";

    static final String KEY_ID = "id";

    static final String KEY_SUBJECT = "subject";

    static final String KEY_TOTAL_SIZE = "total_size";

    static final String KEY_END_POSITION = "end_position";

    static final String KEY_JK_LIST = "bbsAttJkVOList";

    static final String KEY_FILE_LIST = "bbsAttJkFileVOList";

    static final String KEY_FILE_NAME = "fileName";

    static final String KEY_DOWN_URL = "downloadUrl";

    static final String KEY_SUB_LIST = "sub";
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    static final String KEY_CATEGORY = "category";

    static final String KEY_CATEGORYS = "categorys";

    static final String KEY_SUB_CATEGORY = "sub_category";

    static final String KEY_APP_COUNT = "app_count";

    static final String KEY_CATEGORY_ICON_URL = "icon_url";

    static final String KEY_TOP_APP = "top_app";

    static final String KEY_APP_1 = "app_1";

    static final String KEY_APP_2 = "app_2";

    static final String KEY_APP_3 = "app_3";

    static final String KEY_RECOMMEND_TYPE = "top_recommend_type";

    static final String KEY_RECOMMEND_ICON = "pic";

    static final String KEY_RECOMMEND_ID = "id";

    static final String KEY_RECOMMEND_TITLE = "reason";

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    static final String KEY_KEYLIST = "keys";

    static final String KEY_TEXT = "text";

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    static final String KEY_DOWNLOAD_INFO = "download_info";

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static final String KEY_USER_NAME = "name";

    static final String KEY_USER_UID = "uid";
    static final String KEY_COIN_NUM = "coin_num";
    static final String KEY_ADD_COIN_NUM = "add_coin_num";
    static final String KEY_USER_EMAIL = "email";
    static final String KEY_SIGN_IN_TODAY = "sign_in_today";
    static final String KEY_TOKEN = "token";
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static final String KEY_COMMENTS = "comments";
    static final String KEY_COMMENT = "comment";
    static final String KEY_COMMENT_BODY = "comment";
    static final String KEY_COMMENT_ID = "comment_id";
    static final String KEY_COMMENT_AUTHOR = "author";
    static final String KEY_COMMENT_DATE = "date";
    static final String KEY_COMMENT_LIST = "comment_list";

    static final String KEY_VALUE = "value";

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    static final String KEY_TOPICS = "topics";
    static final String KEY_TOPIC = "topic";
    static final String KEY_TOPIC_ID = "topic_id";
    static final String KEY_TOPIC_ICON = "app_icon_url";
    static final String KEY_TOPIC_ICON_LDPI = "ldpi_app_icon_url";
    static final String KEY_TOPIC_NAME = "name";

    static final String KEY_REQUIRED_CATEGORY = "req_category";
    static final String KEY_REQUIRED_CATEGORY_NAME = "name";

    static final String KEY_PAY_CONSUME = "consume";
    static final String KEY_PAY_CHARGE = "charge";
    static final String KEY_PAY_BUY_APP = "buy_app";
    static final String KEY_PAY_LOGS = "logs";
    static final String KEY_PAY_FLAG = "flag";
    static final String KEY_PAY_ORDER_ID = "order_id";
    static final String KEY_PAY_DESCRIPTION = "description";
    static final String KEY_PAY_TIME = "create_time";
    static final String KEY_PAY_MONEY = "money";
    static final String KEY_PAY_STATUS = "status";

    // 支付页
    static final String REMOTE_VERSION = "remote_version";
    static final String RESULT = "result";
    static final String PAY_CARD = "card";
    static final String PAY_TYPE = "pay_type";
    static final String ACCOUNT_LEN = "account_len";
    static final String PASSWORD_LEN = "password_len";
    static final String PAY_CREDIT = "credit";

    static final String PAY_RESULT = "pay_result";

    // 下载文件的状态
    static final int STATUS_DOWNLOADING = 1;
    static final int STATUS_PAUSE = 2;

    static final int SUGGEST_UPDATE = 1;
    static final int FORCE_UPDATE = 2;

    /** 点击下载列表 */
    static final String BROADCAST_CLICK_INTENT = "com.xiaohong.kulian.download.intent";

    // 检查是否有新splash图片
    static final String BROADCAST_SPLASH_CHECK_UPGRADE = "com.xiaohong.market.broadcast.splash.CHECK_UPGRADE";

    // 提示更新的广播
    static final String BROADCAST_NOTICE_UPGRADE = "com.xiaohong.market.broadcast.notice.upgrade";
    static final String BROADCAST_CHECK_UPGRADE = "com.xiaohong.market.broadcast.check.upgrade";

    static final String BROADCAST_FORCE_EXIT = "com.xiaohong.kulian.broadcast.FORCE_EXIT";
    static final String BROADCAST_REMIND_LATTER = "com.xiaohong.kulian.broadcast.REMIND_LATTER";
    static final String BROADCAST_DOWNLOAD_OPT = "com.xiaohong.kulian.broadcast.DOWNLOAD_OPT";
    static final String BROADCAST_DOWNLOAD = "com.xiaohong.kulian.broadcast.DOWNLOAD";
    
    static final int NO_UPDATE = 0;
    static final String EXTRA_UPDATE_LEVEL = "update_level";
    static final String EXTRA_VERSION_CODE = "version_code";
    static final String EXTRA_VERSION_NAME = "version_name";
    static final String EXTRA_DESCRIPTION = "description";
    static final String EXTRA_URL = "apk_url";

    static final String PRODUCT_RESPONSE = "response";
    // product attribute
    static final String PRODUCT_PACKAGENAME = "package_name";

    static final String INSTALL_APP_LOGO = "logo";
    static final String INSTALL_APP_TITLE = "app_title";
    static final String INSTALL_APP_DESCRIPTION = "app_detail";
    static final String INSTALL_APP_CHECKED = "app_checked";
    static final String INSTALL_PLACE_HOLDER = "place_holder";
    static final String INSTALL_APP_IS_CHECKED = "is_checked";

    // BBS搜索结果Title
    static final String SEARCH_RESULT_TITLE = "search_result_title";

    static final String GROUP_1 = "装机必备";
    static final String GROUP_2 = "搜索";
    static final String GROUP_3 = "推荐位";
    static final String GROUP_4 = "首页";
    static final String GROUP_5 = "分类页";
    static final String GROUP_6 = "排行页";
    static final String GROUP_7 = "管理页";
    static final String GROUP_8 = "菜单";
    static final String GROUP_9 = "登录页";
    static final String GROUP_10 = "注册页";
    static final String GROUP_11 = "个人中心页";
    static final String GROUP_12 = "应用详情页";
    static final String GROUP_13 = "反馈页";
    static final String GROUP_14 = "产品列表页";

    // 装机必备模式提示次数
    static final String ALERT = "提醒";
    // 通过提示进入装机必备模式的次数
    static final String ALERT_CLICK = "点击提醒";
    
    // 搜索打开次数
    static final String OPEN_SEARCH = "点击进入搜索";
    // 搜索页点击搜索按钮次数
    static final String CLICK_SEARCH = "点击搜索按钮";
    // 点击热门关键词次数
    static final String CLICK_SEARCH_KEYWORDS = "点击热门关键词";
    // 搜索结果页点击论坛tab的次数
    static final String CLICK_SEARCH_BBS = "点击论坛TAB";
    // 搜索结果页附件下载次数
    static final String CLICK_SEARCH_BBS_APK = "附件下载";
    // 首页头部广告图9张，每一张的点击
    static final String CLICK_RECOMMEND_TOP = "点击顶部推荐位 -> ";
    // 分类菜单点击次数
    static final String CLICK_CATEGORY_TAB = "点击分类TAB";
    // 排行菜单点击次数
    static final String CLICK_RANK_TAB = "点击排行TAB";
    // 管理菜单点击次数
    static final String CLICK_MANAGER_TAB = "点击管理TAB";
    // 首页菜单点击次数
    static final String CLICK_HOME_TAB = "点击首页TAB";

    // static final String 首页30条应用已安装的比例
    // static final String 首页30条应用点击应用名称及图标区域的次数
    // 首页30条应用点击直接下载、更新的次数
    static final String DIRECT_DOWNLOAD = "直接下载、更新";

    // 装机必备入口点击数
    static final String ENTRY = "点击装机必备";
    // 专题入口点击次数
    static final String CLICK_TOPIC_ENTRY = "点击专题";
    // 每个专题点击次数
    static final String CLICK_SUB_TOPIC = "点击专题 -> ";
    // 每个分类、子分类点击次数
    static final String CLICK_CATEGORY_ITEM = "点击分类 -> ";
    // 每个子分类下“最新”tab点击次数
    static final String CLICK_SUB_CATEGORY_NEW_TAB = "点击最新TAB";
    // 每个子分类最热、最新产品列表的下一页读取次数
    static final String PRODUCT_LAZY_LOAD = "产品延迟加载";
    // 排行下各个tab点击次数
    static final String CLICK_RANK_APP = "点击应用排行";
    static final String CLICK_RANK_GAME = "点击游戏排行";
    static final String CLICK_RANK_BOOK = "点击电子书排行";
    static final String CLICK_RANK_POP = "点击风向标排行";

    // static final String 排行下各个tab下的下一页读取次数

    // 已下载软件管理点击次数
    static final String CLICK_FILE_MANAGER = "点击文件管理";
    // 应用管理界面，更新点击次数
    static final String CLICK_UPDATE = "点击更新";
    // 应用管理界面，卸载点击次数
    static final String CLICK_UNINSTALL = "点击卸载";
    // 应用管理界面，应用详情页点击次数
    static final String CLICK_DETAIL = "点击应用详情";
    // 详情页打开次数
    static final String OPEN_PRODUCT_DETAIL = "打开详情页";
    // 在详情页点击下载或更新次数
    static final String DETAIL_DOWNLOAD = "点击下载或更新";
    // 详情页介绍，“更多”点击次数
    static final String DETAIL_CLICK_MORE = "点击更多介绍";
    // 详情页，截图点击次数
    static final String DETAIL_CLICK_SNAPSHOT = "点击截图";
    // 详情页，评论tab点击次数
    static final String DETAIL_CLICK_COMMENT = "查看评论";
    // 详情页，发起评论点击次数
    static final String DETAIL_POST_COMMENT = "发表评论";
    // 账号点击次数
    static final String MENU_CLICK_ACCOUNT = "点击用户中心";
    // 登录次数
    static final String LOGIN = "点击登录";
    // 注册次数
    static final String REGISTER = "点击注册";
    // 登录成功次数
    static final String LOGIN_SUCCESS = "登录成功";
    // 注册成功次数
    static final String REGISTER_SUCCESS = "注册成功";
    // 云推送开启次数
    static final String OPEN_PUSH = "开启云推送";
    // 云推送关闭次数
    static final String CLOSE_PUSH = "关闭云推送";
    // 充值入口点击次数
    static final String CHARGE = "点击充值";

    // static final String 支付宝充值每个流程阶段进入次数
    // static final String 电话卡充值每个流程阶段进入次数

    // 反馈点击次数
    static final String MENU_CLICK_FEEDBACK = "打开反馈页";
    // 反馈发送次数
    static final String SEND_FEEDBACK = "发送反馈";
    // 设置点击次数
    static final String MENU_CLICK_SETTINGS = "打开设置";
    // 缓存清除次数
    static final String CLICK_CLEAR_CACHE = "清除缓存";
    // 搜索历史清除次数
    static final String CLICK_CLEAR_SEARCH_HISTORY = "清除搜索历史";
    // 应用过滤设置次数
    static final String OPEN_FILTER = "打开应用过滤";
    // 社区点击次数
    static final String MENU_CLICK_BBS = "打开社区";
    // 微信支付appid
    static final String APP_ID = "wx558a2f397b2b0be4";
    //打开所有推荐
    static final String BROADCAST_CATEGORY_TASK = "com.xiaohong.kulian.ui.connection.checkAllTask";
    //打开所有任务
    static final String BROADCAST_CATEGORY_RCMD = "com.xiaohong.kulian.ui.connection.checkAllApp";
}
