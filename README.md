# Moxie · 语文 / 英语默写工具

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE) [![在线体验](https://img.shields.io/badge/%E5%9C%A8%E7%BA%BF%E4%BD%93%E9%AA%8C-GitHub%20Pages-orange)](https://zsssal1012-design.github.io/moxie/) [![good first issue](https://img.shields.io/badge/contributions-welcome-brightgreen)](https://github.com/zsssal1012-design/moxie/labels/good%20first%20issue)

> 面向中学生（尤其是**平板 / 大屏设备**）的离线默写练习工具。
> 两个纯前端单文件 Web App + 零依赖 Android WebView 外壳，**不用装任何东西，双击 HTML 就能用**。

English README: [docs/README.en.md](docs/README.en.md)

---

## ✨ 它是什么

| 应用 | 说明 | 技术形态 |
|---|---|---|
| **语文默写** | 古诗文 / 字词默写练习，横屏大屏优化，玻璃拟态 UI，支持翻页震动反馈、在线朗读 | 单文件 HTML + Android 外壳 |
| **英语默写** | 单元词库导入（`英文,中文` 一行一条）、随机出题、短语模块在线翻译、localStorage 持久化 | 单文件 HTML + Android 外壳 |

设计出发点很朴素：孩子晚上要默写，家长手上只有平板，装 App 麻烦、纸质默写效率低。
于是把「默写」这件事做成了**一个 HTML 文件**——放桌面、发微信、丢浏览器，随处可跑。

## 🚀 30 秒上手

### 方式 1：直接用网页版（推荐，零安装）
下载 `web/yuwen-moxie/index.html` 或 `web/english-moxie/index.html`，用任意现代浏览器打开即可。

**🎧 在线体验（无需安装，点开即用）**：https://zsssal1012-design.github.io/moxie/
`

### 方式 2：下载 APK
[Releases](https://github.com/zsssal1012-design/moxie/releases) 页面下载已签名的 `语文默写.apk` / `英语默写.apk`，安装到 Android 平板（minSdk 21）。

### 方式 3：自己构建 APK
```bash
# 环境：Ubuntu/Debian + Android SDK(platforms/android-23, build-tools/29.0.3) + JDK 8
bash scripts/build-apk.sh yuwen     # 构建语文默写
bash scripts/build-apk.sh english   # 构建英语默写
```
构建链：`javac → dx → aapt → zipalign → apksigner`（**不需要 Gradle / Android Studio**），CI 配置见 `.github/workflows/build-apk.yml`。

## 📁 目录结构

```
moxie/
├── web/
│   ├── yuwen-moxie/index.html      # 语文默写（单文件，含全部逻辑与词库）
│   └── english-moxie/index.html    # 英语默写（单文件）
├── android/
│   ├── yuwen-moxie/                # 语文 APK 外壳（Manifest / Java / assets / res）
│   └── english-moxie/              # 英语 APK 外壳
├── scripts/build-apk.sh            # 零 Gradle 构建脚本
├── .github/workflows/build-apk.yml # CI 自动构建 + 发 Release
└── docs/                           # 英文 README、截图、图标脚本
```

## 🧭 项目路线图（Issues 里欢迎认领）

- [ ] 词库与代码分离：支持导入自定义 JSON / CSV 词库
- [ ] 错词本 / 复习队列（艾宾浩斯间隔重复）
- [ ] 语音听写模式：TTS 读词，学生盲写
- [ ] 默写结果导出 PDF（家校打印）
- [ ] 多教材版本词库（按单元组织，社区共建）
- [ ] 国际化（英文界面）
- [ ] PWA：离线安装到桌面

## 🤝 参与贡献

任何规模的贡献都欢迎 —— 修一个错别字、加一个单元词库、补一段文档、报一个 Bug 都算。
请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)（含"第一次提 PR"手把手流程）。

新手友好任务已打好标签：[`good first issue`](https://github.com/zsssal1012-design/moxie/labels/good%20first%20issue)。

## ⚠️ 内容与版权须知

- **代码**采用 Apache License 2.0 授权，可商用，请保留版权声明。
- **内置词库 / 古诗文文本**：古诗文本身属于公版作品；但**教材单元编排、现代文选段、英语教材单词表**的汇编可能涉及出版社权利。
  因此本仓库**不承诺内置词库可自由再分发**。如需商用，请使用自建词库，或参见 [SECURITY.md / 内容说明](docs/content-license.md)。
  欢迎提交 PR 把内置词库改造成"可选下载的数据包"。

## 📄 License

- Code: [Apache-2.0](LICENSE)
- Bundled content: see [docs/content-license.md](docs/content-license.md)

## ⭐ Star History

如果它帮到了你家的默写作业，给个 Star 就是最大的支持。
