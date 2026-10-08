/**
 * 轻量 Markdown 渲染器（零依赖）。
 *
 * 后端的 AI 回复是 Markdown 文本（含 ### 标题、**加粗**、有序/无序列表、
 * 行内代码等）。直接以纯文本插值展示会把符号原样显示，可读性很差。
 * 这里只支持 AI 回复中实际出现的子集，避免引入完整 markdown 库。
 *
 * 安全：所有文本先做 HTML 转义，再拼接我们自己的标签，因此不存在 XSS 注入面。
 */

const ESCAPE_MAP = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;',
}

export function escapeHtml(text) {
  return String(text == null ? '' : text).replace(/[&<>"']/g, (c) => ESCAPE_MAP[c])
}

/** 行内元素：粗体、斜体、行内代码。输入必须是已转义的文本。 */
function renderInline(escaped) {
  return escaped
    .replace(/`([^`]+)`/g, '<code class="md-code">$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>')
}

/**
 * 将 Markdown 文本渲染为 HTML 字符串。
 *
 * 支持的语法子集：
 *  - `# ` ~ `###### ` 标题（渲染为 div.md-h1 ~ div.md-h6）
 *  - `- ` / `* ` / `+ ` 无序列表，`1.` / `1)` 有序列表
 *  - 缩进（2 空格 = 一级）产生嵌套列表，嵌套的 <ul>/<ol> 直接放在父 <li> 内部
 *  - `---` 水平线
 *  - `**粗体**`、`*斜体*`、`` `代码` ``
 *  - 其余非空行按段落处理
 *
 * 实现：用「列表栈」逐行推进。栈元素 { type, indent }。
 * 缩进变深 -> 开新列表（嵌在上一个未闭合的 <li> 里）；
 * 缩进变浅或类型变化 -> 逐层补 </li></ul> 收栈；
 * 同类同层 -> 只补上一个 </li>。
 * 最后 closeAll() 把剩余的 </li></ul> 全部补齐。
 */
export function renderMarkdown(text) {
  if (text == null) return ''
  const lines = String(text).replace(/\r\n/g, '\n').split('\n')
  const out = []
  /** @type {{ type: 'ul'|'ol', indent: number, liOpen: boolean }[]} */
  const stack = []

  // 收栈：把 indent > level 的所有层关掉（先 </li> 再 </ul>）。
  // 注意是严格大于——同级的父列表要保留，以便下一个同级项继续追加。
  const closeTo = (level) => {
    while (stack.length && stack[stack.length - 1].indent > level) {
      const top = stack.pop()
      if (top.liOpen) out.push('</li>')
      out.push(`</${top.type}>`)
    }
  }
  const closeAll = () => closeTo(-1) // -1 => 全部关闭

  for (const raw of lines) {
    const line = raw.replace(/\t/g, '  ').replace(/\s+$/, '')
    const trimmed = line.trim()

    // 空行：结束所有列表
    if (trimmed === '') {
      closeAll()
      continue
    }

    // 水平线
    if (/^(-{3,}|\*{3,}|_{3,})$/.test(trimmed)) {
      closeAll()
      out.push('<hr class="md-hr">')
      continue
    }

    // 标题
    const heading = trimmed.match(/^(#{1,6})\s+(.*)$/)
    if (heading) {
      closeAll()
      const level = heading[1].length
      out.push(`<div class="md-h${level}">${renderInline(escapeHtml(heading[2]))}</div>`)
      continue
    }

    // 引用块：> 文本
    const quote = trimmed.match(/^>\s?(.*)$/)
    if (quote) {
      closeAll()
      out.push(`<blockquote class="md-quote">${renderInline(escapeHtml(quote[1]))}</blockquote>`)
      continue
    }

    // 缩进层级（2 空格 = 1 级，最多 4 级）
    const indent = Math.min(Math.floor((line.length - line.replace(/^\s+/, '').length) / 2), 4)

    // 列表项
    const ulM = trimmed.match(/^[-*+]\s+(.*)$/)
    const olM = trimmed.match(/^\d+[.)]\s+(.*)$/)
    const content = ulM ? ulM[1] : (olM ? olM[1] : null)

    if (content !== null) {
      const type = ulM ? 'ul' : 'ol'
      let top = stack[stack.length - 1]

      // 1) 缩进回退：先收掉比当前行更深的层
      if (top && indent < top.indent) {
        closeTo(indent)
        top = stack[stack.length - 1]
      }

      // 2) 顶层、更深一层、或同级但类型不同 -> 新开列表
      if (!top || indent > top.indent || top.type !== type) {
        out.push(`<${type} class="md-${type}">`)
        stack.push({ type, indent, liOpen: false })
      } else if (top.liOpen) {
        // 3) 同级同类型：关掉上一个 li，继续同一列表
        out.push('</li>')
      }

      out.push(`<li>${renderInline(escapeHtml(content))}`)
      stack[stack.length - 1].liOpen = true
      continue
    }

    // 普通段落
    closeAll()
    out.push(`<p class="md-p">${renderInline(escapeHtml(trimmed))}</p>`)
  }

  closeAll()
  return out.join('')
}