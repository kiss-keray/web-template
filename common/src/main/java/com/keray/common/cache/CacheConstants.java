package com.keray.common.cache;

/**
 * @author by keray
 * date:2019/8/30 13:01
 */
public final class CacheConstants {

    /**
     * "缓存类型1，大对象，实时性高，更新率高"
     */
    public static final String BIG_UP_UP = "cache:big_up_up";
    /**
     * "缓存类型1，大对象，实时性高，更新率低"
     */
    public static final String BIG_UP_LOW = "cache:big_up_low";
    /**
     * "缓存类型1，大对象，实时性低，更新率高"
     */
    public static final String BIG_LOW_UP = "cache:big_low_up";
    /**
     * "缓存类型1，大对象，实时性低，更新率低"
     */
    public static final String BIG_LOW_LOW = "cache:big_low_low";

    /**
     * "缓存类型1，小对象，实时性高，更新率高"
     */
    public static final String SMALL_UP_UP = "cache:small_up_up";
    /**
     * "缓存类型1，小对象，实时性高，更新率低"
     */
    public static final String SMALL_UP_LOW = "cache:small_up_low";
    /**
     * "缓存类型1，小对象，实时性低，更新率高"
     */
    public static final String SMALL_LOW_UP = "cache:small_low_up";
    /**
     * "缓存类型1，小对象，实时性低，更新率低"
     */
    public static final String SMALL_LOW_LOW = "cache:small_low_low";




    /**
     * 意向科目缓存
     */
    public static final String STUDENT_IDEA_SUBJECT = "cache:student_idea_subject";

    /**
     * 意向缓存
     */
    public static final String STUDENT_IDEA = "cache:student_idea_subject";

    /**
     * 获取用户是否具有video权限
     */
    public static final String USER_VIDEO_AUTH = "cache:user_video_auth";

}
