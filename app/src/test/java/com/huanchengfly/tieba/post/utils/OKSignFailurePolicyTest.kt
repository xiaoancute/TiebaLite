package com.huanchengfly.tieba.post.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OKSignFailurePolicyTest {
    @Test
    fun stopOnFailurePropagatesSignErrors() = runBlocking {
        val thrown = runCatching {
            listOf(1).asFlow()
                .flatMapConcat {
                    wrapOKSignFlowForFailurePolicy(
                        stopOnFailure = true,
                        source = flow<Int> {
                            throw IllegalStateException("boom")
                        },
                    ) {
                        fail("stop-on-failure should not swallow forum sign errors")
                    }
                }
                .toList()
        }.exceptionOrNull()

        assertTrue(thrown is IllegalStateException)
        assertEquals("boom", thrown?.message)
    }

    @Test
    fun keepGoingAfterFailureSwallowsErrorAndContinues() = runBlocking {
        val failures = mutableListOf<String>()

        val values = listOf(1, 2).asFlow()
            .flatMapConcat { value ->
                wrapOKSignFlowForFailurePolicy(
                    stopOnFailure = false,
                    source = flow {
                        if (value == 1) {
                            throw IllegalStateException("boom-1")
                        }
                        emit(value)
                    },
                ) { throwable ->
                    failures += throwable.message ?: "unknown"
                }
            }
            .toList()

        assertEquals(listOf("boom-1"), failures)
        assertEquals(listOf(2), values)
    }
}
