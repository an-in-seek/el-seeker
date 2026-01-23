package com.elseeker.common

import org.springframework.test.context.ActiveProfilesResolver

class TestProfileResolver : ActiveProfilesResolver {

    companion object {
        // 절대 변경하지 말 것!
        private const val DEFAULT_PROFILE = "test"
    }

    override fun resolve(testClass: Class<*>): Array<String> {
        return arrayOf(DEFAULT_PROFILE)
    }
}