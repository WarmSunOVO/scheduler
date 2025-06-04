# 智能AI高校排课系统

本项目是一个基于 Spring Boot 和 Vue 3 的智能排课系统，旨在通过 AI 辅助，提升排课效率和课表质量。

## 项目技术栈

*   **后端**: Spring Boot, Java, Spring Data JPA, Spring Security, JWT, MySQL, Spring AI (集成 DeepSeek)
*   **前端**: Vue 3, Vite, Element Plus, Pinia, Axios, Vue Router
*   **构建工具**: Maven (后端), npm/yarn (前端)
*   **数据库**: MySQL

## Windows 用户运行指南

以下步骤指导您如何在 Windows 系统上下载、配置并运行本项目。

### 一、环境准备

在开始之前，请确保您的计算机上已安装并配置好以下软件：

1.  **Git**:
    *   用于从 GitHub 克隆项目。
    *   下载地址: [https://git-scm.com/download/win](https://git-scm.com/download/win)
    *   安装时，建议选择允许在命令提示符 (CMD) 和 Git Bash 中使用。

2.  **Java Development Kit (JDK)**:
    *   **版本**: 建议使用 Java 17 或更高版本 (请与项目开发者确认具体版本)。
    *   **推荐**: Adoptium Temurin OpenJDK ([https://adoptium.net/](https://adoptium.net/))。
    *   **环境变量**:
        *   设置 `JAVA_HOME` 指向您的 JDK 安装目录。
        *   将 JDK 的 `bin` 目录 (例如 `%JAVA_HOME%\bin`) 添加到系统的 `Path` 环境变量中。
        *   验证: 打开命令提示符，输入 `java -version` 和 `javac -version`。

3.  **Apache Maven**:
    *   用于构建和管理后端 Java 项目。
    *   **版本**: 建议使用 Maven 3.6.x 或更高版本。
    *   下载地址: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) (下载 binary zip archive)。
    *   解压到本地目录 (例如 `C:\dev-tools\apache-maven-3.9.x`)。
    *   **环境变量**:
        *   设置 `M2_HOME` 指向您的 Maven 安装目录。
        *   设置 `MAVEN_HOME` 指向您的 Maven 安装目录 (有些工具可能需要这个)。
        *   将 `%M2_HOME%\bin` 添加到系统的 `Path` 环境变量中。
        *   验证: 打开命令提示符，输入 `mvn -version`。

4.  **Node.js 和 npm**:
    *   用于运行和构建前端 Vue 项目。npm 会随 Node.js 一起安装。
    *   **版本**: 建议使用 Node.js LTS (长期支持) 版本 ([https://nodejs.org/](https://nodejs.org/))。
    *   验证: 打开命令提示符，输入 `node -v` 和 `npm -v`。

5.  **MySQL 数据库**:
    *   **版本**: MySQL 5.7 或 8.0+。
    *   下载并安装 MySQL Community Server: [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/)。
    *   在安装过程中，请记下您为 `root` 用户设置的密码。
    *   **数据库管理工具 (可选但推荐)**：MySQL Workbench, DBeaver, Navicat, HeidiSQL 等，方便您管理数据库和执行 SQL 脚本。

6.  **IDE (集成开发环境)**:
    *   **后端 (Java/Spring Boot)**：推荐 IntelliJ IDEA (Community 或 Ultimate版均可) 或 Eclipse IDE for Enterprise Java and Web Developers。
    *   **前端 (Vue)**：推荐 Visual Studio Code (VS Code) 或 WebStorm。

7.  **(可选) Ollama (如果使用本地AI模型)**:
    *   如果项目的 Spring AI 配置为使用本地运行的 Ollama 模型（例如 Mistral, Llama2 等），您需要安装 Ollama for Windows: [https://ollama.com/download/windows](https://ollama.com/download/windows)。
    *   安装后，通过命令提示符运行 `ollama pull <model_name>` (例如 `ollama pull mistral`) 来下载项目中使用的模型。确保 Ollama 服务正在运行。

### 二、项目设置与配置

1.  **克隆项目仓库**:
    打开 Git Bash 或命令提示符，导航到您希望存放项目的目录，然后执行：
    ```bash
    git clone git@github.com:WarmSunOVO/scheduler.git 
    ```
    (如果您没有配置 SSH Key，可能需要使用 HTTPS URL: `https://github.com/WarmSunOVO/scheduler.git`)
    这将创建一个名为 `scheduler` 的项目文件夹。

2.  **后端项目配置 (`scheduler/intelligent-course-scheduler` 目录)**:
    *   **导入项目到IDE**: 使用 IntelliJ IDEA 或 Eclipse 将 `intelligent-course-scheduler` 文件夹作为 Maven 项目导入。IDE 通常会自动下载所需的依赖。
    *   **数据库配置**:
        *   在本地 MySQL 中创建一个新的数据库，例如 `intelligent_scheduling_db`。确保使用 `utf8mb4` 字符集和 `utf8mb4_unicode_ci` 排序规则。
            ```sql
            CREATE DATABASE IF NOT EXISTS intelligent_scheduling_db 
            CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            ```
        *   打开后端项目的配置文件 `src/main/resources/application.properties` (或 `application.yml`)。
        *   修改以下数据库连接属性以匹配您的本地 MySQL 设置：
            ```properties
            spring.datasource.url=jdbc:mysql://localhost:3306/intelligent_scheduling_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
            spring.datasource.username=your_mysql_username # 例如 root
            spring.datasource.password=your_mysql_password # 您MySQL的密码
            ```
    *   **Spring AI (DeepSeek) 配置**:
        *   如果您需要测试AI功能，并且项目配置为使用 DeepSeek (通过 OpenAI 兼容模式)，您需要获取自己的 DeepSeek API Key。
        *   在 `application.properties` 中，找到并设置 `spring.ai.openai.api-key`。
        *   确保 `spring.ai.openai.base-url` 和 `spring.ai.openai.chat.options.model` 配置正确。
    *   **执行数据库初始化SQL脚本**:
        *   项目根目录下应该提供一个名为 `database_initial_data.sql` (或类似名称) 的SQL文件。**[请您将我之前提供的包含清空和插入完整测试数据的SQL脚本保存为此文件名，并放在项目根目录或一个易于找到的位置]**
        *   使用您的数据库管理工具（如 MySQL Workbench, DBeaver）连接到您创建的 `intelligent_scheduling_db` 数据库。
        *   打开并**完整执行**这个 `database_initial_data.sql` 脚本。这将创建所有必要的表结构并填充初始/测试数据。
        *   **注意**: 如果项目的 `spring.jpa.hibernate.ddl-auto` 属性设置为 `create` 或 `create-drop` (通常仅用于开发)，则表结构会自动创建，您可能只需要执行脚本中插入数据的部分。但为了确保一致性，建议执行包含 `CREATE TABLE IF NOT EXISTS`（如果脚本中有的话）和所有 `INSERT` 语句的完整脚本。

3.  **前端项目配置 (`scheduler/intelligent-course-scheduler-ui` 目录，假设这是您的前端项目名)**:
    *   使用 VS Code 或 WebStorm 打开 `intelligent-course-scheduler-ui` 文件夹。
    *   **安装依赖**: 在此目录下打开终端或命令提示符，运行：
        ```bash
        npm install
        ```
        (如果您使用 yarn，则运行 `yarn install`)
    *   **API基础URL配置 (通常不需要修改)**:
        前端代码通常配置为通过相对路径 `/api` 访问后端。Vite 开发服务器会配置代理将 `/api` 请求转发到后端运行的端口（默认为8080）。检查 `.env` 文件或 `vite.config.js` 中的 `VITE_API_BASE_URL` 和代理配置，确保其正确。

### 三、运行项目

1.  **启动后端 Spring Boot 应用**:
    *   在 IntelliJ IDEA 或 Eclipse 中，找到 `IntelligentCourseSchedulerApplication.java` 主类，右键点击并选择 "Run" 或 "Debug"。
    *   或者，在 `intelligent-course-scheduler` 目录下打开终端/命令提示符，执行：
        ```bash
        mvn spring-boot:run
        ```
    *   观察控制台输出，等待后端应用成功启动（通常会在 8080 端口监听）。

2.  **启动前端 Vue 应用**:
    *   在 `intelligent-course-scheduler-ui` 目录下打开新的终端/命令提示符，执行：
        ```bash
        npm run dev
        ```
    *   观察控制台输出，前端开发服务器通常会启动在 `http://localhost:5173` (Vite 默认) 或其他指定的端口。

3.  **访问应用**:
    *   打开您的网页浏览器 (推荐 Chrome, Edge, Firefox)。
    *   访问前端应用的地址，例如: `http://localhost:5173`。
    *   您应该能看到应用的登录页面。使用测试账号（例如 `admin` / `123456`，请与项目开发者确认测试账号）进行登录。

### 四、常见问题与排查

*   **端口冲突**: 如果 8080 或 5173 端口已被占用，您需要在后端 (`application.properties`) 或前端 (`vite.config.js`) 中修改端口号。
*   **数据库连接失败**: 仔细检查 `application.properties` 中的数据库 URL、用户名、密码是否正确，以及 MySQL 服务是否正在运行。
*   **依赖安装问题**: 如果 `npm install` 或 Maven 构建失败，检查网络连接和错误信息，可能需要清理缓存或配置代理。
*   **环境变量**: 确保 `JAVA_HOME` 和 Maven 的环境变量已正确设置并且在新的命令提示符窗口中生效。

如果遇到其他问题，请根据错误信息进行搜索，或与项目其他成员讨论。