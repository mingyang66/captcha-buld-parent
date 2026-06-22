package com.emily.captcha.click.store;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultClickStoreServiceImpl implements ClickStoreService {
    /**
     * 存储验证码会话数据：captchaId -> CaptchaSession
     */
    private final ConcurrentHashMap<String, ClickCaptchaSession> store = new ConcurrentHashMap<>();

    @Override
    public void put(String key, ClickCaptchaSession value) {
        store.put(key, new ClickCaptchaSession(value.getTargetChars(), value.getTargetPoints(), value.getExpireAt()));
    }

    @Override
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(entry -> entry.getValue().getExpireAt() < now);
    }

    @Override
    public ClickCaptchaSession remove(String key) {
        return store.remove(key);
    }
}
