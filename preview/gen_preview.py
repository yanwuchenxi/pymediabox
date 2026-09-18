#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""PyMediaBox 各页面高保真 mockup 生成器（2 Tab 布局，1080x2400）"""
import math, os
from PIL import Image, ImageDraw, ImageFont

BASE = os.path.dirname(os.path.abspath(__file__))
FONT_REG = os.path.join(BASE, "fonts", "NotoSansSC-Regular.otf")
OUT = os.path.join(BASE, "out")
os.makedirs(OUT, exist_ok=True)

BLACK        = (13, 15, 26)
CARD         = (30, 33, 48)
CARD2        = (37, 42, 58)
ACCENT       = (76, 201, 240)
ACCENT_DIM   = (46, 139, 168)
WHITE        = (255, 255, 255)
SECONDARY    = (138, 143, 168)
DANGER       = (255, 107, 107)

W, H = 1080, 2400
f_title   = ImageFont.truetype(FONT_REG, 44)
f_tab     = ImageFont.truetype(FONT_REG, 30)
f_card_t  = ImageFont.truetype(FONT_REG, 34)
f_card_s  = ImageFont.truetype(FONT_REG, 24)
f_btn     = ImageFont.truetype(FONT_REG, 28)
f_chip    = ImageFont.truetype(FONT_REG, 26)
f_small   = ImageFont.truetype(FONT_REG, 22)

def new_page():
    img = Image.new("RGB", (W, H), BLACK)
    d = ImageDraw.Draw(img)
    return img, d

def rrect(d, box, r, fill, outline=None, width=1):
    d.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=width)

def text_w(s, f):
    b = f.getbbox(s)
    return b[2] - b[0]

def status_bar(d):
    d.rectangle((0, 0, W, 60), fill=BLACK)
    d.text((40, 12), "9:41", font=f_small, fill=WHITE)
    d.text((W - 220, 12), "5G", font=f_small, fill=SECONDARY)
    d.rectangle((W - 120, 18, W - 40, 46), outline=SECONDARY, width=3)
    d.rectangle((W - 112, 24, W - 60, 40), fill=ACCENT)

def draw_top(d, active_tab):
    d.text((40, 66), "PyMediaBox", font=f_title, fill=WHITE)
    d.text((W - 160, 86), "v1.1.0", font=f_small, fill=SECONDARY)
    d.line((0, 120, W, 120), fill=CARD, width=2)
    tabs = ["首页", "设置"]
    tab_w = W // 2
    for i, t in enumerate(tabs):
        cx = i * tab_w
        active = (i == active_tab)
        if active:
            d.rectangle((cx + tab_w//2 - 40, 160, cx + tab_w//2 + 40, 170), fill=ACCENT)
        d.text((cx + tab_w//2 - text_w(t, f_tab)//2, 118), t,
               font=f_tab, fill=ACCENT if active else SECONDARY)
    return 170

def icon_search(d, cx, cy, r=18, color=SECONDARY):
    d.ellipse((cx - r, cy - r, cx - r + int(r*0.85), cy - r + int(r*0.85)), outline=color, width=4)
    ex = int(r*0.5)
    d.line((cx - r + int(r*0.8) - 2, cy - r + int(r*0.8) - 2,
            cx + ex + 4, cy + ex + 4), fill=color, width=4)

def icon_play(d, cx, cy, r=18, color=BLACK):
    d.polygon([(cx - r//2, cy - r), (cx + r, cy), (cx - r//2, cy + r)], fill=color)

def icon_star(d, cx, cy, r, filled=False, color=SECONDARY):
    pts=[]
    for i in range(10):
        ang = math.pi/2 + i * math.pi/5
        rr = r if i%2==0 else r*0.45
        pts.append((cx + rr*math.cos(ang), cy - rr*math.sin(ang)))
    if filled:
        d.polygon(pts, fill=color, outline=color)
    else:
        d.polygon(pts, outline=color, width=3)

def btn(d, x, y, w, label, primary=True, h=64):
    if primary:
        rrect(d, (x, y, x + w, y + h), 12, ACCENT)
        d.text((x + w//2 - text_w(label, f_btn)//2, y + 16), label, font=f_btn, fill=BLACK)
    else:
        rrect(d, (x, y, x + w, y + h), 12, CARD, outline=CARD2, width=2)
        d.text((x + w//2 - text_w(label, f_btn)//2, y + 16), label, font=f_btn, fill=WHITE)

def seg_row(d, y, labels, active_idx, x0=40, x1=None):
    """分段按钮行"""
    if x1 is None: x1 = W - 40
    n = len(labels)
    gap = 12
    bw = (x1 - x0 - gap*(n-1)) // n
    for i, lab in enumerate(labels):
        bx = x0 + i * (bw + gap)
        sel = (i == active_idx)
        rrect(d, (bx, y, bx + bw, y + 56), 12, ACCENT if sel else CARD,
              outline=None if sel else CARD2, width=0 if sel else 2)
        d.text((bx + bw//2 - text_w(lab, f_btn)//2, y + 12), lab,
               font=f_btn, fill=BLACK if sel else WHITE)

def card(d, x, y, w, h, radius=24):
    rrect(d, (x, y, x + w, y + h), radius, CARD)

def card_title(d, x, y, t, sub=None):
    d.text((x + 30, y + 26), t, font=f_card_t, fill=WHITE)
    yy = y + 76
    if sub:
        d.text((x + 30, yy), sub, font=f_small, fill=SECONDARY)
        yy += 40
    return yy

def switch(d, x, y, on=True):
    rrect(d, (x, y, x + 80, y + 44), 22, ACCENT if on else CARD2)
    d.ellipse((x + 4 if on else x + 40, y + 4, x + 4 + 36 if on else x + 40 + 36, y + 40),
              fill=WHITE)

# =========================================================
# 1) 首页（含推荐/历史/收藏分段）
# =========================================================
def page_home():
    img, d = new_page()
    status_bar(d)
    top = draw_top(d, 0)
    y = top + 24

    # 搜索栏
    rrect(d, (40, y, W - 40, y + 64), 16, CARD, outline=CARD2, width=2)
    icon_search(d, 80, y + 32, r=18, color=SECONDARY)
    d.text((110, y + 18), "搜索视频", font=f_card_s, fill=SECONDARY)
    btn(d, W - 40 - 120, y, 120, "搜索")
    y += 64 + 20

    # 快捷按钮
    hw = (W - 80 - 20) // 2
    btn(d, 40, y, hw, "本地", primary=False, h=56)
    btn(d, 40 + hw + 20, y, hw, "默认源", primary=False, h=56)
    y += 56 + 16

    # 分段筛选
    seg_row(d, y, ["推荐", "历史", "收藏"], 0)
    y += 56 + 24

    # 列表
    items = [
        ("在线示例 · Big Buck Bunny", "在线", ACCENT, "https://sample-videos.com/...mp4"),
        ("在线示例 · Sintel", "在线", ACCENT, "https://media.w3.org/2010/05/sintel/trailer.mp4"),
        ("电影 · 分类", "API:内置演示源", CARD2, None),
        ("剧集 · 分类", "API:内置演示源", CARD2, None),
        ("综艺 · 分类", "API:内置演示源", CARD2, None),
    ]
    for title, tag, tagc, link in items:
        ch = 110
        card(d, 40, y, W - 80, ch)
        d.text((70, y + 28), title, font=f_card_s, fill=WHITE)
        tw = text_w(tag, f_small) + 24
        rrect(d, (W - 70 - tw, y + 34, W - 70, y + 34 + 32), 14, tagc)
        d.text((W - 70 - tw + 12, y + 38), tag, font=f_small, fill=BLACK if tagc==ACCENT else SECONDARY)
        if link:
            d.text((70, y + 70), link, font=f_small, fill=SECONDARY)
        y += ch + 14

    img.save(os.path.join(OUT, "01_home.png"))

# =========================================================
# 2) 设置页（含 API 源管理 + 爬虫调试器）
# =========================================================
def page_settings():
    img, d = new_page()
    status_bar(d)
    top = draw_top(d, 1)
    y = top + 24

    # 卡片1 播放行为
    c1h = 380
    card(d, 40, y, W - 80, c1h)
    yy = card_title(d, 40, y, "播放行为")
    d.text((70, yy), "保持屏幕常亮", font=f_card_s, fill=WHITE)
    switch(d, W - 150, yy + 6)
    yy += 60
    d.text((70, yy), "断点续播（记住上次进度）", font=f_card_s, fill=WHITE)
    switch(d, W - 150, yy + 6)
    yy += 70
    d.text((70, yy), "默认播放源", font=f_small, fill=SECONDARY)
    yy += 36
    rrect(d, (70, yy, W - 70, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "https://sample-videos.com/.../big_buck_bunny.mp4",
           font=f_small, fill=SECONDARY)
    yy += 64 + 16
    btn(d, 70, yy, W - 140, "播放默认源")
    y += c1h + 20

    # 卡片2 API 源管理
    c2h = 400
    card(d, 40, y, W - 80, c2h)
    yy = card_title(d, 40, y, "API 源管理", "TVBox 式 JSON 接口源")
    rrect(d, (70, yy, W - 70, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "接口 URL（TVBox JSON 格式）", font=f_small, fill=SECONDARY)
    yy += 64 + 12
    btn(d, 70, yy, W - 140, "＋ 添加 API 源", h=56)
    yy += 56 + 16
    srcs = [("内置演示源", "pymediabox://builtin", "内置", True),
            ("API 源", "https://api.example.com/vod", "自定义", False)]
    for name, url, badge, builtin in srcs:
        ch = 88
        rrect(d, (70, yy, W - 70, yy + ch), 12, CARD2)
        d.text((94, yy + 14), name, font=f_card_s, fill=WHITE)
        d.text((94, yy + 52), url, font=f_small, fill=SECONDARY)
        bw = text_w(badge, f_small) + 24
        bx = W - 70 - bw - 20
        rrect(d, (bx, yy + 28, bx + bw, yy + 60), 14, ACCENT if builtin else CARD2)
        d.text((bx + 12, yy + 32), badge, font=f_small,
               fill=BLACK if builtin else SECONDARY)
        if not builtin:
            dw = text_w("删除", f_small)
            d.text((bx - 12 - dw, yy + 32), "删除", font=f_small, fill=DANGER)
        yy += ch + 12
    y += c2h + 20

    # 卡片3 爬虫调试器
    c3h = 480
    card(d, 40, y, W - 80, c3h)
    yy = card_title(d, 40, y, "Python 爬虫调试器", "TVBox Spider 协议接口即时调用")
    # chips
    cx = 70
    for label, sel in [("首页", True), ("分类", False), ("搜索", False), ("详情", False), ("播放器", False)]:
        cw = text_w(label, f_chip) + 40
        if sel:
            rrect(d, (cx, yy, cx + cw, yy + 52), 26, ACCENT)
            d.text((cx + 20, yy + 12), label, font=f_chip, fill=BLACK)
        else:
            rrect(d, (cx, yy, cx + cw, yy + 52), 26, CARD, outline=CARD2, width=2)
            d.text((cx + 20, yy + 12), label, font=f_chip, fill=SECONDARY)
        cx += cw + 10
    yy += 52 + 18
    # 参数 + 运行
    rrect(d, (70, yy, W - 200, yy + 64), 14, CARD2, outline=ACCENT_DIM, width=2)
    d.text((94, yy + 20), "参数（分类号 / 关键词 / ID）", font=f_small, fill=SECONDARY)
    btn(d, W - 180, yy, 110, "运行")
    yy += 64 + 18
    # 结果
    d.text((70, yy), "响应结果", font=f_small, fill=SECONDARY)
    yy += 34
    rrect(d, (70, yy, W - 70, yy + 150), 12, BLACK)
    lines = ['{', '  "class": [', '    {"type": "1", "name": "电影"},',
             '    {"type": "2", "name": "剧集"},', '    {"type": "3", "name": "综艺"}', '  ]', '}']
    for i, ln in enumerate(lines):
        d.text((90, yy + 12 + i * 19), ln, font=f_small,
               fill=ACCENT if i in (0, 6) else SECONDARY)
    y += c3h + 20

    # 卡片4 数据管理
    c4h = 160
    card(d, 40, y, W - 80, c4h)
    yy = card_title(d, 40, y, "数据管理")
    btn(d, 70, yy, W - 140, "清除播放历史 / 收藏 / 断点", primary=False)
    y += c4h + 20

    img.save(os.path.join(OUT, "02_settings.png"))

if __name__ == "__main__":
    page_home()
    page_settings()
    print("done ->", OUT)
