#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""PyMediaBox 各页面高保真 mockup（影视仓/OK影视/蜂蜜式）1080x2400"""
import math, os
from PIL import Image, ImageDraw, ImageFont

BASE = os.path.dirname(os.path.abspath(__file__))
FONT_REG = os.path.join(BASE, "fonts", "NotoSansSC-Regular.otf")
OUT = os.path.join(BASE, "out")
os.makedirs(OUT, exist_ok=True)

BLACK    = (13, 15, 26)
CARD     = (30, 33, 48)
CARD2    = (37, 42, 58)
ACCENT   = (76, 201, 240)
ACCENT_D = (46, 139, 168)
WHITE    = (255, 255, 255)
SECOND   = (138, 143, 168)
DANGER   = (255, 107, 107)
NAV_BG   = (242, 20, 31, 31)

W, H = 1080, 2400
f_logo  = ImageFont.truetype(FONT_REG, 36)
f_tab   = ImageFont.truetype(FONT_REG, 26)
f_hero1 = ImageFont.truetype(FONT_REG, 44)
f_hero2 = ImageFont.truetype(FONT_REG, 26)
f_sec   = ImageFont.truetype(FONT_REG, 32)
f_card  = ImageFont.truetype(FONT_REG, 24)
f_sm    = ImageFont.truetype(FONT_REG, 20)
f_xs    = ImageFont.truetype(FONT_REG, 18)
f_tag   = ImageFont.truetype(FONT_REG, 22)
f_nav   = ImageFont.truetype(FONT_REG, 22)

def new_page():
    img = Image.new("RGB", (W, H), BLACK)
    d = ImageDraw.Draw(img)
    return img, d

def rrect(d, box, r, fill, outline=None, width=1):
    d.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=width)

def tw(s, f):
    b = f.getbbox(s); return b[2]-b[0]

def status_bar(d):
    d.rectangle((0, 0, W, 60), fill=BLACK)
    d.text((40, 14), "9:41", font=f_sm, fill=WHITE)
    d.text((W-160, 14), "5G  100%", font=f_xs, fill=SECOND)

def top_bar(d, active_tab):
    # 顶栏 Logo + 源 + 搜索
    d.text((40, 66), "PyMediaBox", font=f_logo, fill=WHITE)
    # 源选择
    sw = tw("源：演示", f_xs)+30
    rrect(d, (W-40-sw, 80, W-40, 80+48), 12, CARD)
    d.text((W-40-sw+15, 92), "源：演示", font=f_xs, fill=ACCENT)
    d.line((0, 140, W, 140), fill=CARD, width=2)
    # Tab
    tabs = ["首页","设置"]
    tab_w = W//2
    for i,t in enumerate(tabs):
        cx=i*tab_w
        active=(i==active_tab)
        if active:
            d.rectangle((cx+tab_w//2-40, 166, cx+tab_w//2+40, 176), fill=ACCENT)
        d.text((cx+tab_w//2-tw(t,f_tab)//2, 148), t, font=f_tab,
               fill=ACCENT if active else SECOND)
    return 176

def ic_home(d, cx, cy, r, color):
    d.polygon([(cx-r, cy-r*0.1),(cx, cy-r*0.9),(cx+r, cy-r*0.1)], outline=color, width=4)
    d.line((cx-r, cy-r*0.1, cx+r, cy-r*0.1), fill=color, width=4)
    d.rectangle((cx-r*0.6, cy-r*0.1, cx+r*0.6, cy+r*0.8), outline=color, width=4)
    d.line((cx-r*0.25, cy+r*0.8, cx-r*0.25, cy+r*0.1), fill=color, width=4)
    d.line((cx+r*0.25, cy+r*0.8, cx+r*0.25, cy+r*0.1), fill=color, width=4)

def ic_search(d, cx, cy, r, color):
    d.ellipse((cx-r, cy-r, cx-r*0.4, cy-r*0.4), outline=color, width=4)
    ex=int(r*0.35)
    d.line((cx-r*0.4+4, cy-r*0.4+4, cx+r*0.7, cy+r*0.7), fill=color, width=5)

def ic_star(d, cx, cy, r, color):
    pts=[]
    for i in range(10):
        ang = math.pi/2 + i*math.pi/5
        rr = r if i%2==0 else r*0.45
        pts.append((cx+rr*math.cos(ang), cy-rr*math.sin(ang)))
    d.polygon(pts, outline=color, width=3)

def ic_gear(d, cx, cy, r, color):
    d.ellipse((cx-r*0.5, cy-r*0.5, cx+r*0.5, cy+r*0.5), outline=color, width=4)
    for i in range(8):
        ang = i*math.pi/4
        x1=cx+r*0.5*math.cos(ang); y1=cy+r*0.5*math.sin(ang)
        x2=cx+r*math.cos(ang);     y2=cy+r*math.sin(ang)
        d.line((x1,y1,x2,y2), fill=color, width=4)

def bottom_nav(d, active):
    d.rectangle((0, H-72, W, H), fill=(18, 20, 32))
    d.line((0, H-72, W, H-72), fill=CARD, width=2)
    labels = ["首页","搜索","历史/收藏","设置"]
    n=4
    bw = W//n
    for i in range(n):
        cx = i*bw + bw//2
        act = (i==active)
        col = ACCENT if act else SECOND
        if i==0: ic_home(d, cx, H-48, 16, col)
        elif i==1: ic_search(d, cx, H-48, 16, col)
        elif i==2: ic_star(d, cx, H-48, 18, col)
        else: ic_gear(d, cx, H-48, 16, col)
        d.text((cx - tw(labels[i],f_xs)//2, H-16), labels[i], font=f_xs, fill=col)

# =========================================================
# 首页
# =========================================================
def page_home():
    img, d = new_page()
    status_bar(d)
    y = top_bar(d, 0)

    # ① Hero 横幅
    hy = y + 16
    hh = 260
    rrect(d, (16, hy, W-16, hy+hh), 24, CARD2)
    d.text((40, hy+40), "推荐精选", font=f_xs, fill=ACCENT)
    d.text((40, hy+72), "今日焦点 · 在线示例影片", font=f_hero1, fill=WHITE)
    d.text((40, hy+140), "Big Buck Bunny · 10MB · H.264", font=f_xs, fill=SECOND)
    d.rectangle((W-120, hy+hh-50, W-50, hy+hh-14), fill=CARD)
    d.text((W-108, hy+hh-44), "1/3", font=f_xs, fill=WHITE)
    y = hy + hh + 16

    # ② 分类标签横排
    classes = ["全部","电影","剧集","综艺","动画"]
    cx = 16
    for c in classes:
        sel = c=="全部"
        cw = tw(c, f_tag)+30
        rrect(d, (cx, y, cx+cw, y+50), 25, ACCENT if sel else CARD,
              outline=None if sel else CARD2, width=0 if sel else 2)
        d.text((cx+15, y+12), c, font=f_tag, fill=BLACK if sel else SECOND)
        cx += cw + 12
    y += 50 + 16

    # ③ 网格标题 + 页码
    d.text((20, y), "影片列表", font=f_sec, fill=WHITE)
    d.text((W-140, y+8), "第 1 页", font=f_xs, fill=SECOND)
    y += 50

    # ④ 双列封面网格
    videos = [("Big Buck Bunny","10MB","在线"),("Sintel","52MB","在线"),
              ("Tears of Steel","108MB","在线"),("Elephants Dream","151MB","在线")]
    col_w = (W-48)//2
    row_h = 300
    for i,(title,dur,tag) in enumerate(videos):
        col = i%2
        row = i//2
        gx = 16 + col*(col_w+16)
        gy = y + row*(row_h+12)
        rrect(d, (gx, gy, gx+col_w, gy+row_h), 16, CARD)
        # 封面
        rrect(d, (gx, gy, gx+col_w, gy+180), 16, CARD2)
        d.rectangle((gx, gy+160, gx+col_w, gy+180), fill=CARD2)
        d.text((gx+col_w//2 - tw(title[0],f_card)//2, gy+70), title[0],
               font=f_card, fill=ACCENT)
        # 时长角标
        d.rectangle((gx+col_w-90, gy+150, gx+col_w-8, gy+176), fill=(0,0,0))
        d.text((gx+col_w-82, gy+154), dur, font=f_xs, fill=WHITE)
        # 文字区
        d.text((gx+12, gy+196), title, font=f_card, fill=WHITE)
        d.text((gx+12, gy+236), tag, font=f_xs, fill=SECOND)
        # 标签
        tagw = tw(tag,f_xs)+20
        rrect(d, (gx+12, gy+264, gx+12+tagw, gy+292), 10, ACCENT)
        d.text((gx+12+10, gy+268), tag, font=f_xs, fill=BLACK)
    y = y + 2*row_h + 2*12 + 16

    # ⑤ 历史/收藏分段
    hw = (W-40-12)//2
    rrect(d, (16, y, 16+hw, y+56), 12, ACCENT)
    d.text((16+hw//2 - tw("观看历史",f_card)//2, y+14), "观看历史", font=f_card, fill=BLACK)
    rrect(d, (16+hw+12, y, 16+hw+12+hw, y+56), 12, CARD, outline=CARD2, width=2)
    d.text((16+hw+12+hw//2 - tw("影视收藏",f_card)//2, y+14), "影视收藏", font=f_card, fill=WHITE)
    y += 56 + 16

    # ⑥ 历史卡片横排（示意 3 张）
    for i in range(3):
        cw2 = 200
        gx = 16 + i*(cw2+12)
        rrect(d, (gx, y, gx+cw2, y+150), 12, CARD)
        rrect(d, (gx, y, gx+cw2, y+90), 12, CARD2)
        d.rectangle((gx, y+80, gx+cw2, y+90), fill=CARD2)
        d.text((gx+cw2//2-10, y+30), "影", font=f_card, fill=ACCENT)
        d.text((gx+10, y+102), "Big Buck Bunny", font=f_xs, fill=WHITE)
        d.text((gx+10, y+124), "在线 · 09-18 21:50", font=f_xs, fill=SECOND)
    y += 150 + 20

    # ⑦ 快捷入口
    kw = (W-40-12)//2
    rrect(d, (16, y, 16+kw, y+64), 12, CARD, outline=CARD2, width=2)
    d.text((16+kw//2 - tw("本地媒体",f_card)//2, y+18), "本地媒体", font=f_card, fill=WHITE)
    rrect(d, (16+kw+12, y, 16+kw+12+kw, y+64), 12, CARD, outline=CARD2, width=2)
    d.text((16+kw+12+kw//2 - tw("默认源播放",f_card)//2, y+18), "默认源播放", font=f_card, fill=WHITE)

    bottom_nav(d, 0)
    img.save(os.path.join(OUT, "01_home.png"))
    print("home ok")

# =========================================================
# 设置页
# =========================================================
def page_settings():
    img, d = new_page()
    status_bar(d)
    y = top_bar(d, 1)

    cards = [
        ("播放行为", 300),
        ("API 源管理", 340),
        ("Python 爬虫调试器", 460),
        ("播放器设置", 460),
        ("数据管理", 150),
        ("关于", 200),
    ]
    for title, ch in cards:
        rrect(d, (16, y, W-16, y+ch), 20, CARD)
        d.text((40, y+24), title, font=f_sec, fill=WHITE)
        # 占位内容线
        for k in range(3):
            rrect(d, (40, y+80+k*56, W-40, y+80+k*56+40), 10, CARD2)
        y += ch + 16

    bottom_nav(d, 3)
    img.save(os.path.join(OUT, "02_settings.png"))
    print("settings ok")

if __name__ == "__main__":
    page_home()
    page_settings()
    print("done ->", OUT)
