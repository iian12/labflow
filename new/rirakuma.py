import turtle
import math

# =========================
# 기본 설정
# =========================
screen = turtle.Screen()
screen.setup(600, 500)
screen.bgcolor("white")

t = turtle.Turtle()
t.speed(0)
t.hideturtle()

# 색상
BROWN = "#C96A00"       # 얼굴
DARK_BROWN = "#55210F"  # 테두리 / 눈 / 코
YELLOW = "#FFD21F"      # 귀 안쪽
WHITE = "#FFFFFF"
BLUSH = "#F04A22"       # 볼터치


# =========================
# 이동 함수
# =========================
def move(x, y):
    t.penup()
    t.goto(x, y)
    t.pendown()


# =========================
# 타원 그리기
# =========================
def ellipse(cx, cy, rx, ry, fill_color,
            outline_color=DARK_BROWN,
            outline_width=5):

    t.pensize(outline_width)
    t.pencolor(outline_color)
    t.fillcolor(fill_color)

    move(cx + rx, cy)

    t.begin_fill()

    for degree in range(361):
        rad = math.radians(degree)

        x = cx + rx * math.cos(rad)
        y = cy + ry * math.sin(rad)

        t.goto(x, y)

    t.end_fill()


# =========================
# 귀
# =========================

# 왼쪽 귀
ellipse(
    -115, 105,
    48, 45,
    BROWN,
    DARK_BROWN,
    5
)

# 오른쪽 귀
ellipse(
    115, 105,
    48, 45,
    BROWN,
    DARK_BROWN,
    5
)


# =========================
# 귀 안쪽 노란색
# =========================

# 왼쪽
ellipse(
    -125, 100,
    30, 20,
    YELLOW,
    DARK_BROWN,
    4
)

# 오른쪽
ellipse(
    125, 100,
    30, 20,
    YELLOW,
    DARK_BROWN,
    4
)


# =========================
# 얼굴
# =========================
ellipse(
    0, 15,
    145, 110,
    BROWN,
    DARK_BROWN,
    6
)


# =========================
# 눈
# =========================

# 레퍼런스처럼 가로로 작은 눈
ellipse(
    -65, 25,
    16, 7,
    DARK_BROWN,
    DARK_BROWN,
    1
)

ellipse(
    65, 25,
    16, 7,
    DARK_BROWN,
    DARK_BROWN,
    1
)


# =========================
# 입 주변 흰색 부분
# =========================
ellipse(
    0, -5,
    38, 29,
    WHITE,
    DARK_BROWN,
    3
)


# =========================
# 코
# =========================
ellipse(
    0, 7,
    11, 8,
    DARK_BROWN,
    DARK_BROWN,
    1
)


# =========================
# 입
# =========================
t.pencolor(DARK_BROWN)
t.pensize(4)

# 코에서 아래로
move(0, 0)
t.goto(0, -9)


# 왼쪽 입
move(0, -9)
t.setheading(215)
t.circle(15, 70)


# 오른쪽 입
move(0, -9)
t.setheading(-35)
t.circle(-15, 70)


# =========================
# 볼터치
# =========================
t.pencolor(BLUSH)
t.pensize(4)

# 왼쪽 볼
for x in [-84, -75, -66]:
    move(x, -3)
    t.setheading(75)
    t.forward(18)


# 오른쪽 볼
for x in [66, 75, 84]:
    move(x, -3)
    t.setheading(75)
    t.forward(18)


turtle.done()