#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""PyMediaBox 各页面高保真 mockup 生成器（1080x2400, 还原真实配色与组件样式）"""
import os
from PIL import Image, ImageDraw, ImageFont

BASE = os.path.dirname(os.path.abspath(__file__))
FONT_REG = os.path.join(BASE, "fonts", "NotoSansSC-Regular.otf")
OUT = os.path.join(BASE, "out")
os.makedirs(OUT, exist_ok=True)

# ---------- 配色（与 res/values/colors.xml 一致） ----------
BLACK        = (13, 15, 26)        # #0D0F1A
CARD         = (30, 33, 48)        # #1E2130
CARD2        = (37, 42, 58)        # #252A3A
ACCENT       = (76, 201, 240)      # #4CC9F0
ACCENT_DIM   = (46, 139, 168)
WHITE        = (255, 255, 255)
SECONDARY    = (138, 143, 168)    # #8A8FA8
DANGER       = (255, 107, 107)

W, H = 1080, 2400
DPR = 1
f_title   = ImageFont.truetype(FONT_REG, 44)
f_tab     = ImageFont.truetype(FONT_REG, 30)
f_card_t  = ImageFont.truetype(FONT_REG, 34)
f_card_s  = ImageFont.truetype(FONT_REG, 24)
f_btn     = ImageFont.truetype(FONT_REG, 28)
f_chip    = ImageFont.truetype(FONT_REG, 26)
f_small   = ImageFont.truetype(FONT_REG, 22)
f_mono    = ImageFont.load_default()

def new_page():
    img = Image.new("RGB", (W, H), BLACK)
    d = ImageDraw.Draw(img)
    return img, d

def rrect(d, box, r, fill, outline=None, width=1):
    d.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=width)

def text_w(s, f):
    b = d_text_bbox(s, f)
    return b[2] - b[0]

def d_text_bbox(s, f):
    # 使用 font.getbbox
    return f.getbbox(s)

# ---------- 公共：顶部栏 + Tab（4 页通用） ----------
def draw_top(d, active_tab):
    # 顶栏
    rrect(d, (0, 0, W, 120), 0, BLACK)
    d.text((40, 38), "PyMediaBox", font=f_title, fill=WHITE)
    d.text((W - 140, 58), "v1.1.0", font=f_small, fill=SECONDARY)
    # 分隔线
    d.line((0, 120, W, 120), fill=CARD, width=2)
    # Tab 栏
    tabs = ["首页", "爬虫", "队列", "设置"]
    tab_w = W // 4
    for i, t in enumerate(tabs):
        cx = i * tab_w
        active = (i == active_tab)
        if active:
            d.rectangle((cx, 96, cx + tab_w, 168), fill=BLACK)
            d.rectangle((cx, 162, cx + tab_w, 168), fill=ACCENT, width=0)
            # 下划线
            d.rectangle((cx + tab_w//2 - 40, 160, cx + tab_w//2 + 40, 170), fill=ACCENT)
        d.text((cx + tab_w//2 - text_w(t, f_tab)//2, 118), t,
               font=f_tab, fill=ACCENT if active else SECONDARY)
    return 170

def icon_search(d, cx, cy, r=20, color=SECONDARY):
    d.ellipse((cx - r, cy - r, cx - r + int(r*0.8), cy - r + int(r*0.8)), outline=color, width=4)
    import math
    ex = int(r*0.55)
    d.line((cx - r + int(r*0.8) - ex//2, cy - r + int(r*0.8) - ex//2,
            cx - r + int(r*0.8) + ex, cy - r + int(r*0.8) + ex), fill=color, width=4)

def icon_folder(d, x, y, w, h, color=WHITE):
    rrect(d, (x, y + h//5, x + w, y + h), max(4, w//12), None, outline=color, width=3)
    d.line((x, y + h//5, x + int(w*0.12), y + h//5, ), fill=color, width=3)
    d.line((x, y + h//5, x + int(w*0.4), y), fill=color, width=3)
    d.line((x + int(w*0.4), y, x + int(w*0.45), y + h//5), fill=color, width=3)

def icon_play(d, cx, cy, r=22, color=BLACK):
    d.polygon([(cx - r//2, cy - r), (cx + r, cy), (cx - r//2, cy + r)], fill=color)

def icon_star(d, cx, cy, r, filled=False, color=SECONDARY):
    import math
    pts=[]
    for i in range(10):
        ang = math.pi/2 + i * math.pi/5
        rr = r if i%2==0 else r*0.45
        pts.append((cx + rr*math.cos(ang), cy - rr*math.sin(ang)))
    if filled:
        d.polygon(pts, fill=color, outline=color)
    else:
        d.polygon(pts, outline=color, width=3)

def status_bar(d):
    # 状态栏
    d.rectangle((0, 0, W, 60), fill=BLACK)
    d.text((40, 12), "9:41", font=f_small, fill=WHITE)
    # 右侧信号/电池
    d.text((W - 220, 12), "●●●●", font=f_small, fill=SECONDARY)
    d.rectangle((W - 120, 18, W - 40, 46), outline=SECONDARY, width=3)
    d.rectangle((W - 112, 24, W - 60, 40), fill=ACCENT)

def chip(d, x, y, label, selected=False, w=None):
    if w is None:
        w = text_w(label, f_chip) + 40
    h = 56
    if selected:
        rrect(d, (x, y, x + w, y + h), 28, ACCENT)
        d.text((x + 20, y + 14), label, font=f_chip, fill=BLACK)
    else:
        rrect(d, (x, y, x + w, y + h), 28, CARD, outline=CARD2, width=2)
        d.text((x + 20, y + 14), label, font=f_chip, fill=SECONDARY)
    return w

def btn(d, x, y, w, label, primary=True, h=64):
    if primary:
        rrect(d, (x, y, x + w, y + h), 12, ACCENT)
        d.text((x + w//2 - text_w(label, f_btn)//2, y + 16), label,
               font=f_btn, fill=BLACK)
    else:
        rrect(d, (x, y, x + w, y + h), 12, CARD, outline=CARD2, width=2)
        d.text((x + w//2 - text_w(label, f_btn)//2, y + 16), label,
               font=f_btn, fill=WHITE)

def card(d, x, y, w, h, radius=24):
    rrect(d, (x, y, x + w, y + h), radius, CARD)
    return y

def card_title(d, x, y, t, sub=None):
    d.text((x + 30, y + 26), t, font=f_card_t, fill=WHITE)
    yy = y + 76
    if sub:
        d.text((x + 30, yy), sub, font=f_small, fill=SECONDARY)
        yy += 40
    return yy

# =========================================================
# 1) 首页
# =========================================================
def page_home():
    img, d = new_page()
    status_bar(d)
    top = draw_top(d, 0)

    # 搜索栏
    y = top + 24
    rrect(d, (40, y, W - 40, y + 64), 16, CARD, outline=CARD2, width=2)
    d.text((110, y + 18), "搜索视频", font=f_card_s, fill=SECONDARY)
    icon_search(d, 80, y + 32, r=18, color=SECONDARY)
    btn(d, W - 40 - 120, y, 120, "搜索")
    y += 64 + 20

    # 快捷按钮行
    hw = (W - 80 - 20) // 2
    rrect(d, (40, y, 40 + hw, y + 64), 12, CARD, outline=CARD2, width=2)
    icon_folder(d, 40 + hw//2 - 90, y + 20, 28, 28, color=WHITE)
    d.text((40 + hw//2 - 40, y + 16), "本地", font=f_btn, fill=WHITE)

    rrect(d, (40 + hw + 20, y, W - 40, y + 64), 12, CARD, outline=CARD2, width=2)
    icon_play(d, 40 + hw + 20 + hw//2 - 60, y + 32, r=16, color=WHITE)
    d.text((40 + hw + 20 + hw//2 - 10, y + 16), "默认源", font=f_btn, fill=WHITE)

    y += 64 + 24

    # 推荐视频标题
    d.text((40, y), "推荐视频", font=f_small, fill=SECONDARY)
    y += 40

    # 卡片列表
    items = [
        ("在线示例 · Big Buck Bunny", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4", "在线", ACCENT),
        ("在线示例 · Sintel", "https://media.w3.org/2010/05/sintel/trailer.mp4", "在线", ACCENT),
        ("电影 · 分类", "", "API:内置演示源", CARD2),
        ("剧集 · 分类", "", "API:内置演示源", CARD2),
        ("综艺 · 分类", "", "API:内置演示源", CARD2),
        ("动画 · 分类", "", "API:内置演示源", CARD2),
    ]
    for title, link, tag, tagc in items:
        ch = 110
        card(d, 40, y, W - 80, ch)
        d.text((70, y + 28), title, font=f_card_s, fill=WHITE)
        # 标签
        tw = text_w(tag, f_small) + 24
        rrect(d, (W - 70 - tw, y + 34, W - 70, y + 34 + 32), 14, tagc)
        d.text((W - 70 - tw + 12, y + 38), tag, font=f_small, fill=BLACK)
        if link:
            d.text((70, y + 70), link, font=f_small, fill=SECONDARY)
        y += ch + 14

    img.save(os.path.join(OUT, "01_home.png"))
    print("home ok")

# =========================================================
# 2) 爬虫页
# =========================================================
def page_spider():
    img, d = new_page()
    status_bar(d)
    top = draw_top(d, 1)
    y = top + 24

    # 卡片1：Python 爬虫接口
    c1h = 320
    card(d, 40, y, W - 80, c1h)
    yy = card_title(d, 40, y, "Python 爬虫接口", "TVBox Spider 协议 · 选择接口即时调用")
    # chip
    cx = 70
    for label, sel in [("首页", True), ("分类", False), ("搜索", False), ("详情", False), ("播放器", False)]:
        w = chip(d, cx, yy, label, sel)
        cx += w + 12
    yy += 56 + 20
    # 参数输入 + 运行
    rrect(d, (70, yy, W - 200, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "参数（分类号 / 关键词 / ID）", font=f_small, fill=SECONDARY)
    btn(d, W - 180, yy, 110, "运行")
    y += c1h + 20

    # 卡片2：播放入口
    c2h = 220
    card(d, 40, y, W - 80, c2h)
    yy = card_title(d, 40, y, "播放入口")
    rrect(d, (70, yy, W - 70, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "https://sample-videos.com/.../big_buck_bunny_720p_10mb.mp4",
           font=f_small, fill=SECONDARY)
    yy += 64 + 16
    btn(d, 70, yy, W - 140, "播放")
    icon_play(d, 70 + 50, yy + 32, r=18, color=BLACK)
    y += c2h + 20

    # 卡片3：响应结果
    c3h = 260
    card(d, 40, y, W - 80, c3h)
    yy = card_title(d, 40, y, "响应结果")
    rrect(d, (70, yy, W - 70, yy + 150), 12, BLACK)
    lines = [
        '{',
        '  "class": [',
        '    {"type": "1", "name": "电影"},',
        '    {"type": "2", "name": "剧集"},',
        '    {"type": "3", "name": "综艺"}',
        '  ]',
        '}',
    ]
    for i, ln in enumerate(lines):
        d.text((90, yy + 14 + i * 20), ln, font=f_small,
               fill=ACCENT if i in (0, 6) else SECONDARY)
    y += c3h + 20

    # 卡片4：操作历史
    c4h = 180
    card(d, 40, y, W - 80, c4h)
    yy = card_title(d, 40, y, "操作历史")
    d.text((70, yy), "· 11:32:05 首页  → {\"class\": [4]}", font=f_small, fill=SECONDARY)
    d.text((70, yy + 34), "· 11:30:12 分类  → 1 条", font=f_small, fill=SECONDARY)
    d.text((70, yy + 68), "· 11:28:40 搜索  → 测试", font=f_small, fill=SECONDARY)

    img.save(os.path.join(OUT, "02_spider.png"))
    print("spider ok")

# =========================================================
# 3) 队列页
# =========================================================
def page_queue():
    img, d = new_page()
    status_bar(d)
    top = draw_top(d, 2)
    y = top + 24

    # 分段按钮
    hw = (W - 80 - 20) // 2
    # 历史 选中
    rrect(d, (40, y, 40 + hw, y + 64), 12, ACCENT)
    d.text((40 + hw//2 - text_w("播放历史", f_btn)//2, y + 16), "播放历史", font=f_btn, fill=BLACK)
    rrect(d, (40 + hw + 20, y, W - 40, y + 64), 12, CARD, outline=CARD2, width=2)
    d.text((40 + hw + 20 + hw//2 - text_w("收藏", f_btn)//2, y + 16), "收藏", font=f_btn, fill=WHITE)
    y += 64 + 24

    # 列表
    items = [
        ("在线示例 · Big Buck Bunny", "在线 · 2026-09-18 11:32", False),
        ("默认源", "在线 · 2026-09-18 11:28", True),
        ("Sintel", "在线 · 2026-09-18 11:20", False),
        ("测试", "在线 · 2026-09-18 11:15", True),
    ]
    for title, meta, fav in items:
        ch = 110
        card(d, 40, y, W - 80, ch)
        d.text((70, y + 30), title, font=f_card_s, fill=WHITE)
        d.text((70, y + 72), meta, font=f_small, fill=SECONDARY)
        d.text((W - 120, y + 30), "★" if fav else "☆", font=f_card_t,
               fill=ACCENT if fav else SECONDARY)
        y += ch + 14

    img.save(os.path.join(OUT, "03_queue.png"))
    print("queue ok")

# =========================================================
# 4) 设置页
# =========================================================
def page_settings():
    img, d = new_page()
    status_bar(d)
    top = draw_top(d, 3)
    y = top + 24

    # 卡片1 播放行为
    c1h = 380
    card(d, 40, y, W - 80, c1h)
    yy = card_title(d, 40, y, "播放行为")
    # switch 常亮（开）
    d.text((70, yy), "保持屏幕常亮", font=f_card_s, fill=WHITE)
    rrect(d, (W - 150, yy + 6, W - 70, yy + 54), 24, ACCENT)
    d.ellipse((W - 148, yy + 8, W - 108, yy + 48), fill=WHITE)
    yy += 60
    # switch 续播（开）
    d.text((70, yy), "断点续播（记住上次进度）", font=f_card_s, fill=WHITE)
    rrect(d, (W - 150, yy + 6, W - 70, yy + 54), 24, ACCENT)
    d.ellipse((W - 148, yy + 8, W - 108, yy + 48), fill=WHITE)
    yy += 70
    # 默认源输入
    d.text((70, yy), "默认播放源", font=f_small, fill=SECONDARY)
    yy += 36
    rrect(d, (70, yy, W - 70, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "https://sample-videos.com/.../big_buck_bunny.mp4",
           font=f_small, fill=SECONDARY)
    yy += 64 + 16
    btn(d, 70, yy, W - 140, "播放默认源")
    icon_play(d, 70 + 44, yy + 32, r=16, color=BLACK)
    y += c1h + 20

    # 卡片2 API 源管理
    c2_start = y
    c2h = 460
    card(d, 40, c2_start, W - 80, c2h)
    yy = card_title(d, 40, c2_start, "API 源管理", "TVBox 式 JSON 接口源")
    # 输入
    rrect(d, (70, yy, W - 70, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "接口 URL（TVBox JSON 格式）", font=f_small, fill=SECONDARY)
    yy += 64 + 16
    btn(d, 70, yy, W - 140, "＋ 添加 API 源", h=56)
    yy += 56 + 20
    # 源列表
    srcs = [("内置演示源", "pymediabox://builtin", "内置", True),
            ("API 源", "https://api.example.com/vod", "自定义", False)]
    for name, url, badge, builtin in srcs:
        ch = 90
        rrect(d, (70, yy, W - 70, yy + ch), 12, CARD2)
        d.text((94, yy + 16), name, font=f_card_s, fill=WHITE)
        d.text((94, yy + 56), url, font=f_small, fill=SECONDARY)
        bw = text_w(badge, f_small) + 24
        bx = W - 70 - bw - 20
        rrect(d, (bx, yy + 30, bx + bw, yy + 62), 14,
              ACCENT if builtin else CARD2)
        d.text((bx + 12, yy + 34), badge, font=f_small,
               fill=BLACK if builtin else SECONDARY)
        if not builtin:
            dw = text_w("删除", f_small)
            dx = bx - 12 - dw
            d.text((dx, yy + 34), "删除", font=f_small, fill=DANGER)
        yy += ch + 12
    y = c2_start + c2h + 20

    # 卡片3 数据管理
    c3_start = y
    c3h = 170
    card(d, 40, c3_start, W - 80, c3h)
    yy = card_title(d, 40, c3_start, "数据管理")
    btn(d, 70, yy, W - 140, "清除播放历史 / 收藏 / 断点", primary=False)
    y = c3_start + c3h + 20

    # 卡片4 关于
    c4h = 200
    card(d, 40, y, W - 80, c4h)
    yy = card_title(d, 40, y, "关于")
    d.text((70, yy), "PyMediaBox：参照 TVBoxOS + Chaquopy 设计，", font=f_small, fill=SECONDARY)
    d.text((70, yy + 34), "支持 Python 爬虫解析源与影音播放。", font=f_small, fill=SECONDARY)
    d.text((70, yy + 74), "版本 1.1.0", font=f_small, fill=ACCENT_DIM)

    img.save(os.path.join(OUT, "04_settings.png"))
    print("settings ok")

if __name__ == "__main__":
    page_home()
    page_spider()
    page_queue()
    page_settings()
    print("done ->", OUT)
