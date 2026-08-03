/*
 * useShareImage.js — 分享图片生成
 *
 * 使用 Canvas 将照片 + 诗句 + 作者 + 日期合成一张分享卡片，
 * 效果类似朋友圈分享图，支持保存到本地相册。
 *
 * 用法:
 *   const { generate, download } = useShareImage()
 *   const blob = await generate({ photoSrc, poemLine, poemSource, date })
 *   await download(blob, '拍遇存_小池.png')
 */

const CARD_WIDTH = 750
const RATIO = 4 / 3 // 卡片宽高比（调整为竖向卡片）
const CARD_HEIGHT = Math.round(CARD_WIDTH * RATIO)

// 配色（与 App 主题一致）
const COLORS = {
  bg: '#fef9ed',
  bgBottom: '#fdf5e6',
  text: '#4a3520',
  textSecondary: '#8c7a6b',
  textLight: '#b8a48c',
  accent: '#e8a440',
  white: '#ffffff',
  border: 'rgba(200, 180, 150, 0.2)'
}

export function useShareImage() {
  /**
   * 生成分享图片 Blob
   * @param {{
   *   photoSrc: string,       // 照片 URL 或 Base64
   *   poemLine: string,       // 诗句
   *   poemSource: string,     // 出处，如 "杨万里《小池》"
   *   date: string,           // 日期，如 "2026年8月3日"
   * }} data
   * @returns {Promise<Blob>}
   */
  async function generate(data) {
    const canvas = document.createElement('canvas')
    canvas.width = CARD_WIDTH
    canvas.height = CARD_HEIGHT
    const ctx = canvas.getContext('2d')

    // ① 背景
    drawBackground(ctx)

    // ② 照片（上半部分）
    const photoH = Math.round(CARD_HEIGHT * 0.52)
    await drawPhoto(ctx, data.photoSrc, 0, 0, CARD_WIDTH, photoH)

    // 照片底部渐变过渡
    drawPhotoGradient(ctx, photoH)

    // ③ 文字区域（下半部分）
    const textY = photoH + 40
    let curY = textY

    // 诗句
    curY = drawPoemText(ctx, data.poemLine, curY)

    // 出处
    curY += 16
    curY = drawSource(ctx, data.poemSource, curY)

    // 日期
    curY += 24
    curY = drawDate(ctx, data.date, curY)

    // ④ 底部水印
    drawWatermark(ctx)

    // ⑤ 装饰线
    drawOrnament(ctx, photoH + 20)

    // 导出 Blob
    return new Promise((resolve) => {
      canvas.toBlob((blob) => resolve(blob), 'image/png')
    })
  }

  /**
   * 触发下载保存
   * @param {Blob} blob
   * @param {string} filename
   */
  function download(blob, filename = '拍遇存.png') {
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  /**
   * 优先使用 Web Share API，降级到直接下载
   * @param {Blob} blob
   * @param {string} title
   */
  async function share(blob, title = '拍遇存') {
    const file = new File([blob], `${title}.png`, { type: 'image/png' })

    if (navigator.canShare && navigator.canShare({ files: [file] })) {
      try {
        await navigator.share({
          title,
          files: [file]
        })
        return // 分享成功
      } catch {
        // 用户取消或失败，降级下载
      }
    }

    // 降级：直接下载
    download(blob, `${title}.png`)
  }

  return { generate, download, share }
}

// ===== 绘制函数 =====

/** 背景渐变 */
function drawBackground(ctx) {
  const grad = ctx.createLinearGradient(0, 0, 0, CARD_HEIGHT)
  grad.addColorStop(0, COLORS.white)
  grad.addColorStop(0.5, COLORS.bg)
  grad.addColorStop(1, COLORS.bgBottom)
  ctx.fillStyle = grad
  ctx.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT)
}

/** 绘制照片 */
async function drawPhoto(ctx, src, x, y, w, h) {
  // 先画一个灰色占位
  ctx.fillStyle = '#e8e0d5'
  ctx.fillRect(x, y, w, h)

  if (!src) return

  try {
    const img = await loadImage(src)
    // 以 cover 方式裁剪居中
    const scale = Math.max(w / img.width, h / img.height)
    const sw = w / scale
    const sh = h / scale
    const sx = (img.width - sw) / 2
    const sy = (img.height - sh) / 2
    ctx.drawImage(img, sx, sy, sw, sh, x, y, w, h)
  } catch {
    // 加载失败，保留占位色
  }
}

/** 照片底部渐变遮罩 */
function drawPhotoGradient(ctx, photoH) {
  const grad = ctx.createLinearGradient(0, photoH - 60, 0, photoH + 20)
  grad.addColorStop(0, 'rgba(255,255,255,0)')
  grad.addColorStop(0.5, COLORS.bg)
  grad.addColorStop(1, COLORS.bgBottom)
  ctx.fillStyle = grad
  ctx.fillRect(0, photoH - 60, CARD_WIDTH, 80)
}

/** 诗句（大号居中） */
function drawPoemText(ctx, text, y) {
  if (!text) return y

  ctx.fillStyle = COLORS.text
  ctx.font = 'bold 44px "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'

  // 自动换行
  const maxWidth = CARD_WIDTH - 80
  const lines = wrapText(ctx, text, maxWidth)
  const lineHeight = 62

  for (const line of lines) {
    ctx.fillText(line, CARD_WIDTH / 2, y)
    y += lineHeight
  }

  return y
}

/** 出处 */
function drawSource(ctx, text, y) {
  if (!text) return y

  ctx.fillStyle = COLORS.textSecondary
  ctx.font = '28px "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText(`—— ${text}`, CARD_WIDTH / 2, y)

  return y + 40
}

/** 日期 */
function drawDate(ctx, text, y) {
  if (!text) return y

  ctx.fillStyle = COLORS.textLight
  ctx.font = '24px "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText(text, CARD_WIDTH / 2, y)

  return y + 32
}

/** 底部水印 */
function drawWatermark(ctx) {
  const y = CARD_HEIGHT - 50
  ctx.fillStyle = COLORS.textLight
  ctx.font = '22px "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
  ctx.textAlign = 'right'
  ctx.fillText('📖 拍遇存', CARD_WIDTH - 40, y)
}

/** 装饰元素 */
function drawOrnament(ctx, y) {
  // 一条细横线 + 中间小装饰
  const cx = CARD_WIDTH / 2
  ctx.strokeStyle = COLORS.border
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(cx - 80, y)
  ctx.lineTo(cx + 80, y)
  ctx.stroke()

  // 中间小菱形
  ctx.fillStyle = COLORS.accent
  ctx.beginPath()
  ctx.moveTo(cx, y - 5)
  ctx.lineTo(cx + 5, y)
  ctx.lineTo(cx, y + 5)
  ctx.lineTo(cx - 5, y)
  ctx.closePath()
  ctx.fill()
}

// ===== 工具函数 =====

/** 加载图片 */
function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = src
  })
}

/** Canvas 文字自动换行 */
function wrapText(ctx, text, maxWidth) {
  const lines = []
  let current = ''

  for (const char of text) {
    const test = current + char
    if (ctx.measureText(test).width > maxWidth && current.length > 0) {
      lines.push(current)
      current = char
    } else {
      current = test
    }
  }
  if (current) lines.push(current)

  return lines
}
