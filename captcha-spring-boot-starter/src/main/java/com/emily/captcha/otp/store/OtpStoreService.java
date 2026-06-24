package com.emily.captcha.otp.store;

/**
 * OTP会话存储服务接口
 * <p>
 * 定义OTP会话的存储操作，支持自定义实现（如Redis、数据库等）
 */
public interface OtpStoreService {

    /**
     * 存储OTP会话
     * <p>
     * 用于首次生成OTP密钥时存储会话数据
     *
     * @param account 账户标识
     * @param session OTP会话数据
     */
    void put(String account, OtpSession session);

    /**
     * 获取OTP会话
     *
     * @param account 账户标识
     * @return OTP会话数据，如果不存在则返回null
     */
    OtpSession get(String account);

    /**
     * 更新OTP会话
     * <p>
     * 用于验证成功后更新会话状态（如记录最后使用时间）
     *
     * @param account 账户标识
     * @param session OTP会话数据
     */
    void update(String account, OtpSession session);

    /**
     * 删除OTP会话
     * <p>
     * 用于用户禁用OTP时删除会话数据
     *
     * @param account 账户标识
     */
    void remove(String account);

    /**
     * 清理过期的OTP会话
     * <p>
     * 定时任务调用，清理长时间未使用的会话数据
     * 默认实现为空，可选实现
     */
    default void cleanExpired() {
        // 默认不做清理，可选实现
    }
}
