package com.emily.captcha.otp.store;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的OTP会话存储默认实现
 * <p>
 * 使用ConcurrentHashMap提供线程安全的内存存储
 */
public class DefaultOtpStoreServiceImpl implements OtpStoreService {

    /**
     * 会话存储
     */
    private final ConcurrentHashMap<String, OtpSession> store = new ConcurrentHashMap<>();

    @Override
    public void put(String account, OtpSession session) {
        store.put(account, session);
    }

    @Override
    public OtpSession get(String account) {
        return store.get(account);
    }

    @Override
    public void update(String account, OtpSession session) {
        store.put(account, session);
    }

    @Override
    public void remove(String account) {
        store.remove(account);
    }

    /**
     * 清理过期的OTP会话
     * <p>
     * 清理24小时以上未使用的会话
     */
    @Override
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        long expiredThreshold = 24 * 60 * 60 * 1000; // 24小时

        store.entrySet().removeIf(entry -> {
            OtpSession session = entry.getValue();
            long lastUsed = session.getLastUsedAt() > 0 ? session.getLastUsedAt() : now;
            return (now - lastUsed) > expiredThreshold;
        });
    }
}
