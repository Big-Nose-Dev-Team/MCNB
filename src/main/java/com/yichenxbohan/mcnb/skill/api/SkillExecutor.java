package com.yichenxbohan.mcnb.skill.api;

@FunctionalInterface
public interface SkillExecutor {
    void execute(SkillExecutionContext context);
}

