package com.emily.captcha.click.model;

import java.util.List;

/**
 * 验证码生成结果
 */
public class ClickCaptcha {

    /**
     * 验证码唯一标识
     */
    private String captchaId;

    /**
     * 验证码图片 Base64（PNG格式，含 data:image/png;base64, 前缀）
     */
    private String image;

    /**
     * 提示文字，如 "请依次点击: 春 夏 秋"
     */
    private String promptText;

    /**
     * 需要点击的目标字符列表（按顺序）
     */
    private List<String> targetChars;

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public List<String> getTargetChars() {
        return targetChars;
    }

    public void setTargetChars(List<String> targetChars) {
        this.targetChars = targetChars;
    }
}
