# 贡献指南

先说结论：**你不需要很懂技术也能为这个项目做贡献。**
改一个错别字、补一个单元的单词表、把界面截图拍得更好看，都是真贡献。

## 4 种贡献方式，任选一种

| 方式 | 难度 | 你需要会什么 |
|---|---|---|
| 1. 提 Issue（报 Bug / 提需求） | ⭐ | 会打字 |
| 2. 补词库（新增单元 / 修正字词） | ⭐ | 会照着格式改文字 |
| 3. 改文档 / README / 翻译 | ⭐⭐ | 会 Markdown |
| 4. 改代码（HTML/CSS/JS/Java） | ⭐⭐⭐ | 会看代码 |

---

## 方式 1：提 Issue

去 [Issues → New Issue](../../issues/new/choose)，选模板，把这三件事说清楚：

1. 你在做什么操作
2. 你期望发生什么
3. 实际发生了什么（有截图最好）

设备信息也请附上：平板型号 / Android 版本 / 用的是 APK 还是浏览器。

---

## 方式 2 & 3 & 4：提 Pull Request（完整新手流程）

### 第 1 步：Fork
仓库右上角点 **Fork**，把仓库复制到你自己的账号下。

### 第 2 步：clone 到你本地
```bash
git clone https://github.com/<你的用户名>/moxie.git
cd moxie
```

> 不会用命令行？也可以：Fork 之后，在网页上直接点某个文件 → 右上角铅笔图标 ✏️ → 改完 →
> 「Commit changes」→ 选「Create a new branch」→ 点「Propose changes」→「Create pull request」。
> **GitHub 网页就能完成整个 PR 流程，一行命令都不用敲。**

### 第 3 步：新建分支再改
```bash
git checkout -b fix/typo-in-unit3
```
分支名建议格式：`feat/xxx`（新功能）、`fix/xxx`（修 Bug）、`docs/xxx`（文档）、`data/xxx`（词库）。

### 第 4 步：改代码 / 改词库
- 语文默写：`web/yuwen-moxie/index.html`
- 英语默写：`web/english-moxie/index.html`
- 词库都在文件里的 `const BANK = [...]` / `units` 数据结构里，按同样格式往里加就行
- 改完用浏览器直接打开这个 HTML 自测一下

### 第 5 步：提交
```bash
git add .
git commit -m "data: add Unit 5 word list for english-moxie"
git push -u origin fix/typo-in-unit3
```
提交信息用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)：
`feat:` 新功能 / `fix:` 修复 / `docs:` 文档 / `data:` 词库 / `style:` 样式 / `chore:` 杂项。

### 第 6 步：发起 PR
GitHub 会提示你「Compare & pull request」，点它，填说明，提交。等维护者 Review。

---

## 我能改多代码风格的东西吗？

- 单文件 HTML 是本项目的**特性**，不是缺点（方便分发到平板）。请**不要**把它拆成几十个小文件。
- 不做大规模格式化（全文件重排缩进）—— 这类 PR 很难 Review。
- 新增依赖前先开 Issue 讨论。

## 词库内容规范

- 字词、古诗文：必须是公版内容或你自己整理、有权分发的内容
- 英语单词表：优先提交「义务教育课标词汇表」级别的通用词；**请勿直接上传教材扫描页、整册教材词表的原文排版**
- 每条数据请标注来源（教材版本 / 课标 / 自编），写在 PR 描述里即可

## 行为准则

对事不对人。这是给中学生和家长用的工具，请保持耐心与友善。
任何骚扰、人身攻击、广告行为都会被移除并封禁。

## 许可证

你提交的贡献默认以 **Apache License 2.0** 授权给本项目，即视为你同意上述条款。

---

有任何不清楚的，直接在 Issue 里问，或者在 PR 里说「我是第一次提 PR，请多指导」——没人会笑话你。