package com.yichenxbohan.mcnb.examples;

import com.yichenxbohan.mcnb.skill.SoulBowSkill;

/**
 * 射魂长弓属性设置示例
 *
 * 新机制：按下 X 键激活技能，下一次射箭时自动触发技能效果
 */
public class SoulBowConfigExample {

    /**
     * 示例1: 设置超高速箭矢
     */
    public static void setupFastArrow() {
        SoulBowSkill.setArrowSpeed(10.0);  // 设置速度为10（默认是3.0）
        SoulBowSkill.setArrowDamage(15.0); // 设置伤害为15（默认是10.0）
    }

    /**
     * 示例2: 设置强力穿透箭
     */
    public static void setupPiercingArrow() {
        SoulBowSkill.setArrowSpeed(5.0);      // 中等速度
        SoulBowSkill.setArrowDamage(20.0);    // 高伤害
        SoulBowSkill.setArrowPierceLevel(10); // 超高穿透
        SoulBowSkill.setArrowCrit(true);      // 必定暴击
    }

    /**
     * 示例3: 设置慢速重击箭
     */
    public static void setupHeavyArrow() {
        SoulBowSkill.setArrowSpeed(1.5);     // 慢速
        SoulBowSkill.setArrowDamage(50.0);   // 超高伤害
        SoulBowSkill.setArrowPierceLevel(0); // 不穿透
        SoulBowSkill.setArrowCrit(false);    // 不暴击
    }

    /**
     * 示例4: 获取当前设置
     */
    public static void printCurrentSettings() {
        System.out.println("当前箭矢速度: " + SoulBowSkill.getArrowSpeed());
        System.out.println("当前箭矢伤害: " + SoulBowSkill.getArrowDamage());
        System.out.println("当前穿透等级: " + SoulBowSkill.getArrowPierceLevel());
        System.out.println("当前是否暴击: " + SoulBowSkill.isArrowCrit());
    }

    /**
     * 示例5: 在主类初始化时设置
     * 将这段代码添加到 Mcnb.java 的 commonSetup 方法中
     */
    public static void initializeInMainClass() {
        // 设置你想要的默认值
        SoulBowSkill.setArrowSpeed(5.0);   // 改成你想要的速度
        SoulBowSkill.setArrowDamage(25.0); // 改成你想要的伤害
        SoulBowSkill.setArrowPierceLevel(5); // 改成你想要的穿透等级
    }

    /**
     * 使用说明：
     * 1. 按下 X 键激活技能（会显示"射魂长弓已就绪"）
     * 2. 拉弓射箭
     * 3. 箭矢会自动应用技能效果（高速、高伤害、粒子轨迹）
     * 4. 技能效果只对下一次射箭有效，之后需要重新激活
     * 5. 激活状态下再次按 X 键可以取消激活
     */
}
