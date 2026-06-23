# Captcha Spring Boot Starter

基于 Spring Boot 的图形验证码组件，支持 **文字点选**、**滑动解锁**、**旋转验证** 三种图形验证码，以及 **OTP双因子认证** 功能。

## 特性

- 零配置开箱即用，Spring Boot AutoConfiguration 自动装配
- 三种图形验证码类型：文字点选 / 滑动解锁 / 旋转验证
- **OTP双因子认证**：基于RFC 6238标准，兼容Google Authenticator等应用
- 纯 Java AWT 绘制，无第三方图片库依赖
- 内存存储 + 定时清理过期数据，可自定义存储实现（如 Redis）
- 完善的配置属性，灵活调整各项参数
- 内置 Sample 示例工程，含完整前端交互页面

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.mingyang66</groupId>
    <artifactId>captcha-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 注入 Service

```java
@RestController
public class CaptchaController {

    private final ClickCaptchaService clickCaptchaService;   // 文字点选
    private final SliderCaptchaService sliderCaptchaService; // 滑动解锁
    private final RotateCaptchaService rotateCaptchaService; // 旋转验证
    private final OtpService otpService;                     // OTP双因子认证

    public CaptchaController(ClickCaptchaService clickCaptchaService,
                             SliderCaptchaService sliderCaptchaService,
                             RotateCaptchaService rotateCaptchaService,
                             OtpService otpService) {
        this.clickCaptchaService = clickCaptchaService;
        this.sliderCaptchaService = sliderCaptchaService;
        this.rotateCaptchaService = rotateCaptchaService;
        this.otpService = otpService;
    }
}
```

---

## 一、文字点选验证码

用户按照提示顺序点击图片中的指定汉字，验证点击坐标是否正确。

### API 接口

#### 生成验证码

```
GET /api/captcha/generate
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "image": "data:image/png;base64,iVBORw0KGgo...",
    "promptText": "请依次点击：明,月,光"
  }
}
```

| 字段 | 说明 |
|------|------|
| `captchaId` | 验证码唯一标识，校验时传入 |
| `image` | Base64 编码的图片（含 data URI 前缀），可直接作为 `<img src>` |
| `promptText` | 提示文本，告知用户需要按顺序点击哪些字 |

#### 校验验证码

```
POST /api/captcha/verify
Content-Type: application/json
```

请求体：

```json
{
  "captchaId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "clicks": [
    { "x": 120, "y": 80 },
    { "x": 200, "y": 60 },
    { "x": 260, "y": 90 }
  ]
}
```

| 字段 | 说明 |
|------|------|
| `captchaId` | 生成时返回的验证码 ID |
| `clicks` | 用户点击坐标列表，**必须与提示顺序一致** |

响应示例：

```json
{ "code": 200, "message": "验证通过" }
{ "code": 400, "message": "验证失败，请重新点击" }
```

### 后端使用示例

```java
@GetMapping("/api/captcha/generate")
public Map<String, Object> generate() {
    ClickCaptcha captcha = clickCaptchaService.generate();
    Map<String, Object> data = new HashMap<>();
    data.put("captchaId", captcha.getCaptchaId());
    data.put("image", captcha.getImage());
    data.put("promptText", captcha.getPromptText());
    return Map.of("code", 200, "data", data);
}

@PostMapping("/api/captcha/verify")
public Map<String, Object> verify(@RequestBody VerifyRequest request) {
    boolean success = clickCaptchaService.verify(request.getCaptchaId(), request.getClicks());
    return Map.of("code", success ? 200 : 400,
                  "message", success ? "验证通过" : "验证失败");
}
```

### 配置项

> **配置路径规则**：所有配置以 `spring.emily.captcha` 为前缀，通过 `.click` / `.slider` / `.rotate` 区分验证码类型。

#### 文字点选验证码配置

```properties
# ========== 图片尺寸 ==========
# 验证码图片宽度（像素），默认 320
spring.emily.captcha.click.width=320
# 验证码图片高度（像素），默认 160
spring.emily.captcha.click.height=160

# ========== 字符配置 ==========
# 图片中随机生成的字符总数，默认 5
spring.emily.captcha.click.char-count=5
# 需要用户按顺序点击的目标字符数量，默认 3（必须 <= char-count）
spring.emily.captcha.click.target-count=3

# ========== 字体配置 ==========
# 字体最小尺寸（像素），默认 22
spring.emily.captcha.click.font-size-min=22
# 字体最大尺寸（像素），默认 30（字符会在此范围内随机大小）
spring.emily.captcha.click.font-size-max=30

# ========== 干扰元素 ==========
# 干扰线数量，默认 6（用于增加识别难度）
spring.emily.captcha.click.noise-line-count=6
# 干扰点数量，默认 30（随机噪点）
spring.emily.captcha.click.noise-point-count=30

# ========== 验证参数 ==========
# 点击容差（像素），判定点击是否命中字符中心，默认 20
# 值越大越容易通过，值越小验证越严格
spring.emily.captcha.click.tolerance=20

# ========== 过期时间 ==========
# 验证码有效期（ISO-8601 Duration 格式），默认 PT120S（120秒）
# 常用值：PT60S（60秒）、PT2M（2分钟）、PT5M（5分钟）
spring.emily.captcha.click.expiry-time=PT120S
```

> **注意**：`font-size-max` 建议 >= `font-size-min`，否则字体大小会固定为 `font-size-max`。

---

## 二、滑动解锁验证码

用户直接拖拽图片中的拼图块，将其移动到背景图上的缺口位置完成验证。

### API 接口

#### 生成验证码

```
GET /api/captcha/slider/generate
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "backgroundImage": "data:image/png;base64,...",
    "sliderImage": "data:image/png;base64,...",
    "y": 45
  }
}
```

| 字段 | 说明 |
|------|------|
| `captchaId` | 验证码唯一标识 |
| `backgroundImage` | 带缺口的背景图 Base64 |
| `sliderImage` | 拼图块图片 Base64 |
| `y` | 拼图块在图片上的 Y 坐标（自然像素），用于前端定位 |

#### 校验验证码

```
POST /api/captcha/slider/verify
Content-Type: application/json
```

请求体：

```json
{
  "captchaId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "x": 156
}
```

| 字段 | 说明 |
|------|------|
| `captchaId` | 生成时返回的验证码 ID |
| `x` | 拼图块最终停留位置的 X 坐标（**图片自然像素坐标**） |

> **坐标换算提示**：前端显示尺寸可能与图片自然尺寸不同，提交时需换算：
> ```javascript
> const scaleX = bgImg.naturalWidth / bgImg.clientWidth;
> const actualX = Math.round(pieceLeft * scaleX);
> ```

响应示例：

```json
{ "code": 200, "message": "验证通过" }
{ "code": 400, "message": "验证失败，请重新滑动" }
```

### 后端使用示例

```java
@GetMapping("/api/captcha/slider/generate")
public Map<String, Object> sliderGenerate() {
    SliderCaptcha captcha = sliderCaptchaService.generate();
    Map<String, Object> data = new HashMap<>();
    data.put("captchaId", captcha.getCaptchaId());
    data.put("backgroundImage", captcha.getBackgroundImage());
    data.put("sliderImage", captcha.getSliderImage());
    data.put("y", captcha.getY());
    return Map.of("code", 200, "data", data);
}

@PostMapping("/api/captcha/slider/verify")
public Map<String, Object> sliderVerify(@RequestBody SliderVerifyRequest request) {
    boolean success = sliderCaptchaService.verify(request.getCaptchaId(), request.getX());
    return Map.of("code", success ? 200 : 400,
                  "message", success ? "验证通过" : "验证失败");
}
```

### 配置项

#### 滑动解锁验证码配置

```properties
# ========== 图片尺寸 ==========
# 背景图宽度（像素），默认 320
spring.emily.captcha.slider.width=320
# 背景图高度（像素），默认 160
spring.emily.captcha.slider.height=160

# ========== 干扰元素 ==========
# 干扰线数量，默认 6
spring.emily.captcha.slider.noise-line-count=6
# 干扰点数量，默认 30
spring.emily.captcha.slider.noise-point-count=30

# ========== 验证参数 ==========
# X 坐标容差（像素），判定拼图块是否对齐缺口，默认 5
# 值越大越容易通过，建议 3~10 之间
spring.emily.captcha.slider.tolerance=5

# ========== 过期时间 ==========
# 验证码有效期，默认 PT120S（120秒）
spring.emily.captcha.slider.expiry-time=PT120S
```

---

## 三、旋转验证码

用户拖拽圆形图片外围的手柄进行旋转，将图片旋转到正确方向完成验证。

### API 接口

#### 生成验证码

```
GET /api/captcha/rotate/generate
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "image": "data:image/png;base64,..."
  }
}
```

| 字段 | 说明 |
|------|------|
| `captchaId` | 验证码唯一标识 |
| `image` | 已随机旋转的圆形图片 Base64，图片内含方向刻度盘和"上"箭头标识 |

#### 校验验证码

```
POST /api/captcha/rotate/verify
Content-Type: application/json
```

请求体：

```json
{
  "captchaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "angle": 180
}
```

| 字段 | 说明 |
|------|------|
| `captchaId` | 生成时返回的验证码 ID |
| `angle` | 用户旋转的角度（顺时针，0~360） |

> **角度计算提示**：前端通过极坐标计算鼠标相对圆心的角度变化量：
> ```javascript
> function getMouseAngle(e, cx, cy) {
>     const dx = e.clientX - cx;
>     const dy = e.clientY - cy;
>     let angle = Math.atan2(dy, dx) * 180 / Math.PI + 90;
>     if (angle < 0) angle += 360;
>     return angle;
> }
> ```

响应示例：

```json
{ "code": 200, "message": "验证通过" }
{ "code": 400, "message": "验证失败，请重新旋转" }
```

### 后端使用示例

```java
@GetMapping("/api/captcha/rotate/generate")
public Map<String, Object> rotateGenerate() {
    RotateCaptcha captcha = rotateCaptchaService.generate();
    Map<String, Object> data = new HashMap<>();
    data.put("captchaId", captcha.getCaptchaId());
    data.put("image", captcha.getImage());
    return Map.of("code", 200, "data", data);
}

@PostMapping("/api/captcha/rotate/verify")
public Map<String, Object> rotateVerify(@RequestBody RotateVerifyRequest request) {
    boolean success = rotateCaptchaService.verify(request.getCaptchaId(), request.getAngle());
    return Map.of("code", success ? 200 : 400,
                  "message", success ? "验证通过" : "验证失败");
}
```

### 配置项

#### 旋转验证码配置

```properties
# ========== 图片尺寸 ==========
# 圆形图片尺寸（像素，宽高相同），默认 200
spring.emily.captcha.rotate.size=200

# ========== 角度配置 ==========
# 随机旋转的最小角度（度），默认 30
spring.emily.captcha.rotate.min-angle=30
# 随机旋转的最大角度（度），默认 330
# 图片会在此范围内随机旋转一个角度，用户需反向旋转回来
spring.emily.captcha.rotate.max-angle=330

# ========== 干扰元素 ==========
# 干扰线数量，默认 4
spring.emily.captcha.rotate.noise-line-count=4
# 干扰点数量，默认 20
spring.emily.captcha.rotate.noise-point-count=20

# ========== 验证参数 ==========
# 角度容差（度），判定用户旋转角度是否正确，默认 10
# 实际角度差值 <= tolerance 即验证通过
spring.emily.captcha.rotate.tolerance=10

# ========== 过期时间 ==========
# 验证码有效期，默认 PT120S（120秒）
spring.emily.captcha.rotate.expiry-time=PT120S
```

---

## 四、OTP 双因子认证

基于 **RFC 6238 (TOTP)** 标准实现的一次性密码验证，兼容 Google Authenticator、Microsoft Authenticator 等主流验证器应用。

### 功能特性

- ✅ 基于时间的一次性密码（TOTP），每30秒刷新
- ✅ 兼容 Google Authenticator、Microsoft Authenticator 等应用
- ✅ Base32 编码密钥，支持 QR 码扫描配置
- ✅ 时间窗口验证，处理时钟不同步问题
- ✅ 防重放攻击保护，同一时间窗口内OTP只能使用一次
- ✅ 可配置密码长度、时间步长、哈希算法等参数

### API 接口

#### 生成OTP密钥

为用户生成新的OTP密钥（仅在首次启用时调用）：

```
POST /api/captcha/otp/secret?account=user@example.com
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "account": "user@example.com",
    "secret": "JBSWY3DPEHPK3PXP",
    "otpAuthUri": "otpauth://totp/EmilyCaptcha:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=EmilyCaptcha&algorithm=SHA1&digits=6&period=30"
  }
}
```

| 字段 | 说明 |
|------|------|
| `account` | 账户标识 |
| `secret` | Base32 编码的密钥，用于手动输入到验证器应用 |
| `otpAuthUri` | OTP Auth URI，用于生成 QR 码 |

#### 检查OTP状态

检查用户是否已启用OTP：

```
GET /api/captcha/otp/enabled?account=user@example.com
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "account": "user@example.com",
    "enabled": true
  }
}
```

#### 验证OTP密码

验证用户输入的OTP密码：

```
POST /api/captcha/otp/verify
Content-Type: application/json
```

请求体：

```json
{
  "account": "user@example.com",
  "otp": "123456"
}
```

| 字段 | 说明 |
|------|------|
| `account` | 账户标识 |
| `otp` | 6位OTP密码（从验证器应用获取） |

响应示例：

```json
{ "code": 200, "message": "验证通过" }
{ "code": 400, "message": "验证失败" }
```

#### 删除OTP配置

删除用户的OTP配置：

```
POST /api/captcha/otp/remove?account=user@example.com
```

响应示例：

```json
{
  "code": 200,
  "message": "OTP已删除"
}
```

### 后端使用示例

```java
@RestController
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * 为用户启用OTP
     */
    @PostMapping("/api/otp/enable")
    public Map<String, Object> enableOtp(@RequestParam String account) {
        // 生成密钥
        String secret = otpService.generateSecret(account);
        
        // 生成OTP Auth URI（用于生成QR码）
        String otpAuthUri = otpService.generateOtpAuthUri(account, "YourApp");
        
        Map<String, Object> data = new HashMap<>();
        data.put("secret", secret);
        data.put("otpAuthUri", otpAuthUri);
        
        return Map.of("code", 200, "data", data);
    }

    /**
     * 验证OTP（用于登录时的双因子验证）
     */
    @PostMapping("/api/otp/verify")
    public Map<String, Object> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean success = otpService.verify(request.getAccount(), request.getOtp());
        
        return Map.of("code", success ? 200 : 400,
                      "message", success ? "验证通过" : "验证失败");
    }

    /**
     * 用户登录时结合OTP验证
     */
    @PostMapping("/api/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        // 1. 验证用户名密码
        if (!validatePassword(request.getAccount(), request.getPassword())) {
            return Map.of("code", 400, "message", "用户名或密码错误");
        }
        
        // 2. 如果启用了OTP，验证OTP密码
        if (otpService.isEnabled(request.getAccount())) {
            if (!otpService.verify(request.getAccount(), request.getOtp())) {
                return Map.of("code", 400, "message", "OTP验证失败");
            }
        }
        
        // 3. 登录成功
        return Map.of("code", 200, "message", "登录成功");
    }
}
```

### 配置项

#### OTP双因子认证配置

```properties
# ========== 密码配置 ==========
# OTP密码长度（位数），默认 6
# 常用值：6 或 8
spring.emily.captcha.otp.code-length=6

# ========== 时间配置 ==========
# 时间步长（秒），即OTP刷新周期，默认 30 秒
# ISO-8601 Duration 格式：PT30S（30秒）、PT1M（1分钟）
spring.emily.captcha.otp.time-step=PT30S

# 时间窗口大小，默认 1
# 允许前后 N 个时间窗口的OTP都有效，用于处理时钟不同步
# 设置为 1 表示：当前窗口 + 前1个窗口 + 后1个窗口 = 共3个窗口有效
spring.emily.captcha.otp.window-size=1

# ========== 密钥配置 ==========
# 密钥长度（字节），默认 20 字节（160位）
# 建议值：20（160位）或 32（256位）
spring.emily.captcha.otp.secret-key-length=20

# ========== 算法配置 ==========
# HMAC 哈希算法，默认 HmacSHA1
# 可选值：HmacSHA1、HmacSHA256、HmacSHA512
spring.emily.captcha.otp.algorithm=HmacSHA1
```

### 用户配置流程

#### 方式一：扫描二维码（推荐）

1. 调用 `/api/captcha/otp/secret` 接口生成密钥
2. 使用返回的 `otpAuthUri` 生成 QR 码
3. 用户打开 Google Authenticator 等应用
4. 点击"+" → "扫描二维码" → 扫描 QR 码
5. 验证器应用自动添加账户并开始生成OTP

#### 方式二：手动输入密钥

1. 调用 `/api/captcha/otp/secret` 接口生成密钥
2. 将返回的 `secret` 展示给用户
3. 用户打开验证器应用
4. 点击"+" → "手动输入密钥"
5. 输入账户标识和密钥字符串
6. 验证器应用开始生成OTP

### 生成QR码示例

#### 使用前端JavaScript

```javascript
// 使用 QR Server API 生成QR码
const otpAuthUri = 'otpauth://totp/YourApp:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=YourApp';
const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(otpAuthUri)}`;

// 在页面中显示
document.getElementById('qrCode').src = qrCodeUrl;
```

#### 使用Java后端（ZXing库）

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

```java
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public byte[] generateQrCode(String otpAuthUri, int size) throws WriterException, IOException {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUri, BarcodeFormat.QR_CODE, size, size);
    
    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    
    return pngOutputStream.toByteArray();
}
```

### 自定义存储实现

默认使用内存存储OTP会话。如需使用Redis等持久化方案：

```java
@Bean
public OtpStoreService otpStoreService(RedisTemplate<String, Object> redisTemplate) {
    return new RedisOtpStoreServiceImpl(redisTemplate);
}
```

### 安全建议

1. **密钥保护**：OTP密钥等同于密码，必须安全存储和传输
2. **HTTPS传输**：所有OTP相关接口必须使用HTTPS
3. **时钟同步**：服务器应使用NTP同步时间，确保时间准确
4. **备份方案**：为用户提供备份码，防止手机丢失无法登录
5. **速率限制**：对OTP验证接口实施速率限制，防止暴力破解
6. **日志审计**：记录OTP验证失败事件，监控异常行为

---

## 全局配置

```properties
# 验证码总开关（默认 true）
# 设置为 false 时，所有验证码 Bean 均不会注册，相当于禁用整个验证码模块
spring.emily.captcha.enabled=true
```

> **说明**：`enabled` 是全局控制参数，优先级高于各子类型配置。当 `enabled=false` 时，即使配置了 `.click` / `.slider` / `.rotate` 相关参数也不会生效。

## 自定义存储实现

默认使用内存 `ConcurrentHashMap` 存储验证码会话。如需替换为 Redis 等持久化方案，只需声明对应 StoreService 的 Bean：

```java
@Bean
public ClickStoreService clickStoreService() {
    return new RedisClickStoreServiceImpl(redisTemplate);
}

@Bean
public SliderStoreService sliderStoreService() {
    return new RedisSliderStoreServiceImpl(redisTemplate);
}

@Bean
public RotateStoreService rotateStoreService() {
    return new RedisRotateStoreServiceImpl(redisTemplate);
}

@Bean
public OtpStoreService otpStoreService() {
    return new RedisOtpStoreServiceImpl(redisTemplate);
}
```

框架通过 `@ConditionalOnMissingBean` 检测，存在自定义实现时自动跳过默认内存实现。

## 模块结构

```
captcha-buld-parent
├── captcha-spring-boot-starter          # 核心组件
│   └── com.emily.captcha
│       ├── CaptchaAutoConfiguration     # 自动配置
│       ├── CaptchaProperties            # 配置属性
│       ├── click/                       # 文字点选验证码
│       │   ├── model/                   #   ClickCaptcha, ClickPoint
│       │   ├── service/                 #   ClickCaptchaService
│       │   └── store/                   #   ClickStoreService + 默认实现
│       ├── slider/                      # 滑动解锁验证码
│       │   ├── model/                   #   SliderCaptcha
│       │   ├── service/                 #   SliderCaptchaService
│       │   └── store/                   #   SliderStoreService + 默认实现
│       └── rotate/                      # 旋转验证码
│           ├── model/                   #   RotateCaptcha
│           ├── service/                 #   RotateCaptchaService
│           └── store/                   #   RotateStoreService + 默认实现
└── captcha-spring-boot-sample           # 使用示例
    ├── controller/CaptchaController     # REST 接口
    └── resources/static/
        ├── index.html                   # 文字点选前端
        ├── slider.html                  # 滑动解锁前端（拖拽拼图块）
        └── rotate.html                  # 旋转验证前端
```

## 运行示例

```bash
cd captcha-spring-boot-sample
mvn spring-boot:run
```

访问以下页面体验四种验证方式：

- 文字点选：http://localhost:8080/index.html
- 滑动解锁：http://localhost:8080/slider.html
- 旋转验证：http://localhost:8080/rotate.html
- **OTP双因子认证**：http://localhost:8080/otp.html

## 环境要求

- Java 21+
- Spring Boot 4.x
